package fusion.developer.web.cavidades.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import fusion.developer.web.cavidades.repository.CatalogoRepository;
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
 * Grava os arquivos em /assets/... e os metadados no SQLite.
 */
public class CadastrarMoldeServlet extends HttpServlet {

    private final ObjectMapper json = new ObjectMapper();
    private final CatalogoRepository catalogo = new CatalogoRepository();

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
            String realPath = req.getServletContext().getRealPath("/");
            if (realPath == null) {
                throw new IllegalStateException("A aplicacao precisa estar explodida no Tomcat para gravar arquivos.");
            }
            Path baseWebapp = Paths.get(realPath);
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
            String modelo3dId = Files.isRegularFile(dirModels.resolve(nomeStl))
                    ? "molde-" + codigoNorm.toLowerCase().replace(" ", "-")
                    : null;

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

            String layoutId = "layout-generico-" + cavidadesParaLayout(cavidades);
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
            if (imgBytes != null) {
                moldEntry.put("desenhoId", "desenho-" + codigoNorm + "-peca");
            }
            if (modelo3dId != null) moldEntry.put("modelo3dId", modelo3dId);
            Map<String, Object> padroes = new LinkedHashMap<>();
            padroes.put("produto", nomePeca.trim().toUpperCase());
            padroes.put("moldeMod", "Molde " + codigoNorm);
            padroes.put("material", material.isBlank() ? "PP" : material.trim());
            padroes.put("maquina", maquina.isBlank() ? "" : maquina.trim());
            moldEntry.put("padroes", padroes);
            moldEntry.put("origem", "cadastro via app web");

            moldId = catalogo.salvarMolde(moldEntry);

            if (modelo3dId != null) {
                Map<String, Object> modelo = new LinkedHashMap<>();
                modelo.put("id", modelo3dId);
                modelo.put("arquivo", "assets/models/" + nomeStl);
                modelo.put("nome", nomePeca.trim());
                if (!revisao.isBlank()) modelo.put("revisao", revisao.trim());
                modelo.put("fallbackProcedural", false);
                catalogo.salvarModelo3d(modelo);
            }

            if (imgBytes != null) {
                Map<String, Object> peca = new LinkedHashMap<>();
                peca.put("id", "desenho-" + codigoNorm + "-peca");
                peca.put("moldeId", moldId);
                peca.put("papel", "peca");
                peca.put("imagem", "assets/images/pecas/" + nomeImagem);
                peca.put("origem", "cadastro via app web");
                catalogo.salvarDesenho(peca);

                Map<String, Object> grade = new LinkedHashMap<>();
                grade.put("id", "desenho-" + codigoNorm + "-grade");
                grade.put("moldeId", moldId);
                grade.put("papel", "grade");
                grade.put("imagem", "assets/desenhos/" + nomeSvg);
                grade.put("origem", "cadastro via app web");
                catalogo.salvarDesenho(grade);
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
}
