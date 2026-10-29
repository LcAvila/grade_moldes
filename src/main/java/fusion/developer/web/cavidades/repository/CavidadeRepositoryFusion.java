package fusion.developer.web.cavidades.repository;

import fusion.developer.web.cavidades.config.AppConfig;
import fusion.developer.web.cavidades.model.Cavidade;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Grava as cavidades no formulário do Fusion Platform (app.persistencia=fusion).
 *
 * Estratégia de upsert: carrega todos os registros do formulário e casa por
 * (molde, numero). Funciona bem com o volume de 2 controladores por turno;
 * se crescer, trocar por consulta nativa (PersistEngine.getConnection).
 *
 * Os nomes internos dos campos do formulário precisam ser EXATAMENTE os abaixo
 * (ver spec em docs/arquitetura-fusion.md):
 *   molde, numero, zona, desvio, desvio_texto, data, vedado, controlador
 */
public class CavidadeRepositoryFusion {

    public static final String FORMULARIO_PADRAO = "CavidadeNaoConforme";

    public static final String F_MOLDE = "molde";
    public static final String F_NUMERO = "numero";
    public static final String F_ZONA = "zona";
    public static final String F_DESVIO = "desvio";
    public static final String F_DESVIO_TEXTO = "desvio_texto";
    public static final String F_DATA = "data";
    public static final String F_VEDADO = "vedado";
    public static final String F_CONTROLADOR = "controlador";

    public void salvarLote(String codigoMolde, List<Cavidade> lista, String usuario) throws Exception {
        String entidade = AppConfig.get("app.fusion.formulario-cavidade");
        if (entidade == null || entidade.isBlank()) {
            entidade = FORMULARIO_PADRAO;
        }

        // indice dos registros ja existentes deste molde
        Map<Integer, FormularioFusion.Registro> existentes = new HashMap<>();
        for (FormularioFusion.Registro reg : FormularioFusion.buscarTodos(entidade)) {
            Object molde = FormularioFusion.lerValor(reg, F_MOLDE);
            if (molde == null || !codigoMolde.equals(String.valueOf(molde))) {
                continue;
            }
            Object numero = FormularioFusion.lerValor(reg, F_NUMERO);
            if (numero == null) {
                continue;
            }
            try {
                existentes.put(Integer.parseInt(String.valueOf(numero).trim()), reg);
            } catch (NumberFormatException ignore) {
                // registro legado com numero invalido -> ignora
            }
        }

        for (Cavidade c : lista) {
            Map<String, Object> campos = new LinkedHashMap<>();
            campos.put(F_MOLDE, codigoMolde);
            campos.put(F_NUMERO, c.numero);
            campos.put(F_ZONA, c.zona);
            campos.put(F_DESVIO, c.desvio);
            campos.put(F_DESVIO_TEXTO, c.desvioTexto);
            campos.put(F_DATA, c.data != null ? c.data.toString() : null);
            campos.put(F_VEDADO, c.vedado != null ? c.vedado : "N");
            campos.put(F_CONTROLADOR,
                    usuario != null && !usuario.isBlank() ? usuario : c.controlador);

            FormularioFusion.Registro reg = existentes.get(c.numero);
            if (reg == null) {
                reg = FormularioFusion.criar(entidade);
            }
            FormularioFusion.setarCampos(reg, campos);
            FormularioFusion.persistir(reg);
        }
    }
}
