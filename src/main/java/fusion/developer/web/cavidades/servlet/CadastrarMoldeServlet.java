package fusion.developer.web.cavidades.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cadastro de molde via app web.
 *
 * POST /api/moldes/cadastrar (multipart/form-data):
 *   campos: codigo, nomePeca, familia, cavidades (int), material, maquina, revisaoCadastro
 *   partes: cadFile (STL, opcional), imagem (PNG/JPG, opcional)
 *
 * Grava os arquivos em /assets/... e atualiza config/moldes.json e config/desenhos.json.
 */
public class CadastrarMoldeServlet extends HttpServlet {

    private final ObjectMapper json = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");

        try {
            String codigo = param(req, "codigo");
            String nomePeca = param(req, "nomePeca");
            String familia = param(req, "familia");
            int cavidades = parseInt(param(req, "cavidades"), 0);
            String material = param(req, "material");
            String maquina = param(req, "maquina");
            String revisao = param(req, "revisaoCadastro");

            if (codigo.isBlank() || nomePeca.isBlank() || cavidades < 1) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                json.writeValue(resp.getWriter(), Map.of("ok", false, "erro", "Campos obrigatorios: codigo, nomePeca, cavidades."));
                return;
            }

            String codigoNorm = codigo.trim().toUpperCase();
            Path baseWebapp = Paths.get(req.getServletContext().getRealPath("/"));
            Path dirModels = baseWebapp.resolve("assets/models");
            Path dirPecas = baseWebapp.resolve("assets/images/pecas");
            Path dirDesenhos = baseWebapp.resolve("assets/desenhos");
            Files.createDirectories(dirModels);
            Files.createDirectories(dirPecas);
            Files.createDirectories(dirDesenhos);

            // --- CAD (STL) ---
            Part cadPart = part(req, "cadFile");
            String nomeStl = "molde-" + codigoNorm + ".stl";
            if (cadPart != null && cadPart.getSize() > 0) {
                Path destino = dirModels.resolve(nomeStl);
                try (InputStream in = cadPart.getInputStream()) {
                    Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            // --- Imagem da peca ---
            Part imgPart = part(req, "imagem");
            String extImagem = "png";
            String nomeImagem = codigoNorm + ".png";
            byte[] imgBytes = null;
            if (imgPart != null && imgPart.getSize() > 0) {
                String ct = imgPart.getContentType();
                if (ct != null && ct.contains("jpeg")) extImagem = "jpg";
                else if (ct != null && ct.contains("svg")) extImagem = "svg";
                nomeImagem = codigoNorm + "." + extImagem;
                try (InputStream in = imgPart.getInputStream()) {
                    imgBytes = in.readAllBytes();
                }
                Files.write(dirPecas.resolve(nomeImagem), imgBytes);
            }

            // --- SVG da grade (imagem embutida em base64) ---
            String nomeSvg = codigoNorm + "-grade.svg";
            if (imgBytes != null) {
                String mime = "image/" + ("jpg".equals(extImagem) ? "jpeg" : extImagem);
                String b64 = Base64.getEncoder().encodeToString(imgBytes);
                String svg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" "
                        + "width=\"100%\" height=\"100%\" viewBox=\"0 0 800 600\">\n"
                        + "  <image x=\"0\" y=\"0\" width=\"100%\" height=\"100%\" xlink:href=\"data:" + mime + ";base64," + b64 + "\"/>\n"
                        + "</svg>\n";
                Files.writeString(dirDesenhos.resolve(nomeSvg), svg, StandardCharsets.UTF_8);
            }

            // --- layout generico mais proximo ---
            String layoutId = "layout-generico-" + cavidadesParaLayout(cavidades);

            // --- cavidades list ---
            List<String> cavList = new ArrayList<>();
            for (int i = 1; i <= cavidades; i++) cavList.add(String.valueOf(i));

            String moldId = "molde-" + codigoNorm.toLowerCase().replace(" ", "-");

            Map<String, Object> moldEntry = new LinkedHashMap<>();
            moldEntry.put("id", moldId);
            moldEntry.put("codigo", codigoNorm);
            moldEntry.put("nomePeca", nomePeca.trim());
            moldEntry.put("familia", familia.isBlank() ? "Geral" : familia.trim());
            moldEntry.put("revisaoCadastro", revisao.isBlank() ? "1" : revisao.trim());
            moldEntry.put("cavidades", cavList);
            moldEntry.put("layoutId", layoutId);
            moldEntry.put("desenhoId", "desenho-" + codigoNorm + "-peca");
            Map<String, Object> padroes = new LinkedHashMap<>();
            padroes.put("produto", nomePeca.trim().toUpperCase());
            padroes.put("moldeMod", "Molde " + codigoNorm);
            padroes.put("material", material.isBlank() ? "PP" : material.trim());
            padroes.put("maquina", maquina.isBlank() ? "" : maquina.trim());
            moldEntry.put("padroes", padroes);
            moldEntry.put("origem", "cadastro via app web");

            // --- atualiza moldes.json ---
            Path moldesPath = baseWebapp.resolve("config/moldes.json");
            Map<String, Object> moldesRoot = readJson(moldesPath);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> moldes = (List<Map<String, Object>>) moldesRoot.get("moldes");
            if (moldes == null) { moldes = new ArrayList<>(); moldesRoot.put("moldes", moldes); }
            // remove duplicata por codigo
            moldes.removeIf(m -> codigoNorm.equals(String.valueOf(m.get("codigo")).toUpperCase()));
            moldes.add(moldEntry);
            json.writerWithDefaultPrettyPrinter().writeValue(moldesPath.toFile(), moldesRoot);

            // --- atualiza desenhos.json ---
            Path desenhosPath = baseWebapp.resolve("config/desenhos.json");
            if (Files.isRegularFile(desenhosPath)) {
                Map<String, Object> desenhosRoot = readJson(desenhosPath);
                // estrutura: { desenhos: [ { id, molde, tipo, imagem, ... } ] } ou array?
                // tenta array direto
                Object node = desenhosRoot.get("desenhos");
                if (node instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> lista = (List<Map<String, Object>>) node;
                    if (imgBytes != null) {
                        Map<String, Object> peca = new LinkedHashMap<>();
                        peca.put("id", "desenho-" + codigoNorm + "-peca");
                        peca.put("molde", codigoNorm);
                        peca.put("tipo", "peca");
                        peca.put("imagem", "assets/images/pecas/" + nomeImagem);
                        lista.add(peca);
                    }
                    if (imgBytes != null) {
                        Map<String, Object> grade = new LinkedHashMap<>();
                        grade.put("id", "desenho-" + codigoNorm + "-grade");
                        grade.put("molde", codigoNorm);
                        grade.put("tipo", "grade");
                        grade.put("imagem", "assets/desenhos/" + nomeSvg);
                        lista.add(grade);
                    }
                    json.writerWithDefaultPrettyPrinter().writeValue(desenhosPath.toFile(), desenhosRoot);
                }
            }

            json.writeValue(resp.getWriter(), Map.of("ok", true, "codigo", codigoNorm, "moldId", moldId));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                json.writeValue(resp.getWriter(), Map.of("ok", false, "erro", e.getMessage() != null ? e.getMessage() : "erro"));
            } catch (IOException ignored) {
            }
        }
    }

    private static String param(HttpServletRequest req, String nome) {
        try {
            Part p = req.getPart(nome);
            if (p != null) {
                return new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            }
        } catch (Exception ignored) {
        }
        String v = req.getParameter(nome);
        return v == null ? "" : v.trim();
    }

    private static Part part(HttpServletRequest req, String nome) {
        try { return req.getPart(nome); } catch (Exception e) { return null; }
    }

    private static int parseInt(String v, int def) {
        try { return Integer.parseInt(v.trim()); } catch (Exception e) { return def; }
    }

    private static int cavidadesParaLayout(int n) {
        int[] opts = {1, 2, 4, 8, 16, 24, 32, 48, 64, 96};
        for (int o : opts) if (n <= o) return o;
        return 96;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> readJson(Path p) throws IOException {
        if (!Files.isRegularFile(p)) return new LinkedHashMap<>();
        Object o = new ObjectMapper().readValue(p.toFile(), Object.class);
        if (o instanceof Map) return (Map<String, Object>) o;
        return new LinkedHashMap<>();
    }
}
