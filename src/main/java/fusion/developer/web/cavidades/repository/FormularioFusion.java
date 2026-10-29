package fusion.developer.web.cavidades.repository;

import fusion.developer.web.cavidades.config.AppConfig;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adapter reflexivo para a API de persistência do Fusion Platform:
 *   AdapterUtils.createNewEntityInstance(entidade)
 *   new EntityWrapper(neoObject) / findField(...).setValue(...) / findValue(...)
 *   PersistEngine.persist(neoObject) / getObjects(classeDaEntidade)
 *
 * O SDK do Fusion não está no classpath deste projeto (ele compila fora do
 * Fusion), então as classes são resolvidas em tempo de execução. Configure os
 * FQN em application.properties:
 *
 *   app.fusion.adapter-utils-class=
 *   app.fusion.entity-wrapper-class=
 *   app.fusion.persist-engine-class=
 *
 * Se vazios, são tentados os candidatos abaixo. Se nenhum for encontrado, a
 * exceção indica exatamente o que configurar (veja docs/arquitetura-fusion.md).
 */
public final class FormularioFusion {

    // FQN reais confirmados pelos imports do projeto irmão CustonFusion (que compila
    // contra este Fusion): com.neomind.fusion.workflow.adapter.AdapterUtils,
    // com.neomind.fusion.entity.EntityWrapper, com.neomind.fusion.persist.PersistEngine.
    // "br.com.neomind.*" (versões antigas) fica por último só como fallback.
    private static final String[] CANDIDATOS_ADAPTER_UTILS = {
        "com.neomind.fusion.workflow.adapter.AdapterUtils",
        "com.neomind.fusion.utils.AdapterUtils",
        "com.neomind.fusion.core.AdapterUtils",
        "br.com.neomind.fusion.utils.AdapterUtils"
    };
    private static final String[] CANDIDATOS_ENTITY_WRAPPER = {
        "com.neomind.fusion.entity.EntityWrapper",
        "com.neomind.fusion.entities.EntityWrapper",
        "com.neomind.fusion.core.EntityWrapper",
        "br.com.neomind.fusion.entities.EntityWrapper"
    };
    private static final String[] CANDIDATOS_PERSIST_ENGINE = {
        "com.neomind.fusion.persist.PersistEngine",
        "com.neomind.fusion.core.PersistEngine",
        "com.neomind.fusion.PersistEngine",
        "br.com.neomind.fusion.persist.PersistEngine"
    };

    /** Par neoObject + wrapper de um registro do formulário. */
    public static final class Registro {
        public final Object neoObject;
        public final Object wrapper;

        Registro(Object neoObject, Object wrapper) {
            this.neoObject = neoObject;
            this.wrapper = wrapper;
        }
    }

    private FormularioFusion() {}

    // ------------------------------------------------------------------
    // Classes do SDK (cacheadas na primeira resolução)
    // ------------------------------------------------------------------

    private static volatile Class<?> classeAdapterUtils;
    private static volatile Class<?> classeEntityWrapper;
    private static volatile Class<?> classePersistEngine;

    private static Class<?> resolverClasse(String chaveConfig, String[] candidatos, String nomeSimples) {
        String configurado = AppConfig.get(chaveConfig);
        if (configurado != null && !configurado.isBlank()) {
            try {
                return Class.forName(configurado.trim());
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(nomeSimples + ": classe configurada em "
                        + chaveConfig + " não encontrada: " + configurado.trim(), e);
            }
        }
        for (String nome : candidatos) {
            try {
                return Class.forName(nome);
            } catch (ClassNotFoundException | NoClassDefFoundError ignore) {
                // tenta próximo candidato
            }
        }
        throw new IllegalStateException(nomeSimples + " do Fusion não encontrado no classpath. "
                + "Descubra o FQN no projeto Fusion (Eclipse: F3 na classe) e configure "
                + chaveConfig + " em application.properties. "
                + "Candidatos testados: " + String.join(", ", candidatos));
    }

    private static Class<?> adapterUtils() {
        Class<?> c = classeAdapterUtils;
        if (c == null) {
            synchronized (FormularioFusion.class) {
                c = classeAdapterUtils;
                if (c == null) {
                    c = resolverClasse("app.fusion.adapter-utils-class", CANDIDATOS_ADAPTER_UTILS, "AdapterUtils");
                    classeAdapterUtils = c;
                }
            }
        }
        return c;
    }

    private static Class<?> entityWrapper() {
        Class<?> c = classeEntityWrapper;
        if (c == null) {
            synchronized (FormularioFusion.class) {
                c = classeEntityWrapper;
                if (c == null) {
                    c = resolverClasse("app.fusion.entity-wrapper-class", CANDIDATOS_ENTITY_WRAPPER, "EntityWrapper");
                    classeEntityWrapper = c;
                }
            }
        }
        return c;
    }

    private static Class<?> persistEngine() {
        Class<?> c = classePersistEngine;
        if (c == null) {
            synchronized (FormularioFusion.class) {
                c = classePersistEngine;
                if (c == null) {
                    c = resolverClasse("app.fusion.persist-engine-class", CANDIDATOS_PERSIST_ENGINE, "PersistEngine");
                    classePersistEngine = c;
                }
            }
        }
        return c;
    }

    // ------------------------------------------------------------------
    // Operações
    // ------------------------------------------------------------------

    /** AdapterUtils.createNewEntityInstance(entidade) + new EntityWrapper(neo). */
    public static Registro criar(String entidade) throws Exception {
        Method criar;
        try {
            criar = adapterUtils().getMethod("createNewEntityInstance", String.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("AdapterUtils.createNewEntityInstance(String) não encontrado.", e);
        }
        Object neo = invocar(criar, null, entidade);
        return empacotar(neo);
    }

    private static Registro empacotar(Object neo) throws Exception {
        for (Constructor<?> ctor : entityWrapper().getConstructors()) {
            Class<?>[] params = ctor.getParameterTypes();
            if (params.length == 1 && params[0].isInstance(neo)) {
                return new Registro(neo, ctor.newInstance(neo));
            }
        }
        throw new IllegalStateException(
                "EntityWrapper não tem construtor de 1 parâmetro compatível com " + neo.getClass().getName());
    }

    /** findField(nome).setValue(valor) para cada campo. Valores nulos são ignorados. */
    public static void setarCampos(Registro reg, Map<String, Object> campos) throws Exception {
        Method findField = reg.wrapper.getClass().getMethod("findField", String.class);
        for (Map.Entry<String, Object> e : campos.entrySet()) {
            if (e.getValue() == null) {
                continue;
            }
            Object campo = invocar(findField, reg.wrapper, e.getKey());
            if (campo == null) {
                throw new IllegalStateException("Campo '" + e.getKey()
                        + "' não existe no formulário do Fusion (confira o nome interno do campo).");
            }
            Method setValue = metodoSetValue(campo.getClass());
            if (setValue == null) {
                throw new IllegalStateException("Campo '" + e.getKey() + "' não expõe setValue(...).");
            }
            Class<?> tipo = setValue.getParameterTypes()[0];
            Object valor = e.getValue();
            if (!tipo.isPrimitive() && valor != null && !tipo.isInstance(valor)) {
                if (tipo == String.class) {
                    valor = String.valueOf(valor);
                } else {
                    throw new IllegalStateException("Campo '" + e.getKey() + "' espera " + tipo.getName()
                            + ", veio " + valor.getClass().getName());
                }
            }
            invocar(setValue, campo, valor);
        }
    }

    private static Method metodoSetValue(Class<?> classe) {
        Method generico = null;
        for (Method m : classe.getMethods()) {
            if (m.getName().equals("setValue") && m.getParameterCount() == 1) {
                if (m.getParameterTypes()[0] == Object.class) {
                    return m;
                }
                if (generico == null) {
                    generico = m;
                }
            }
        }
        return generico;
    }

    /** findValue(nome) — tolera a variante "fiendValue" documentada na doc do Fusion. */
    public static Object lerValor(Registro reg, String campo) throws Exception {
        Method findValue;
        try {
            findValue = reg.wrapper.getClass().getMethod("findValue", String.class);
        } catch (NoSuchMethodException e) {
            try {
                findValue = reg.wrapper.getClass().getMethod("fiendValue", String.class);
            } catch (NoSuchMethodException e2) {
                throw new IllegalStateException("EntityWrapper não expõe findValue(String).", e2);
            }
        }
        return invocar(findValue, reg.wrapper, campo);
    }

    /** PersistEngine.persist(neoObject). */
    public static void persistir(Registro reg) throws Exception {
        for (Method m : persistEngine().getMethods()) {
            if (m.getName().equals("persist") && m.getParameterCount() == 1
                    && m.getParameterTypes()[0].isInstance(reg.neoObject)) {
                invocar(m, null, reg.neoObject);
                return;
            }
        }
        throw new IllegalStateException("PersistEngine.persist(NeoObject) não encontrado nesta versão do Fusion.");
    }

    /** PersistEngine.getObjects(AdapterUtils.getEntityClass(entidade)) — todos os registros. */
    @SuppressWarnings("unchecked")
    public static List<Registro> buscarTodos(String entidade) throws Exception {
        Method getEntityClass;
        try {
            getEntityClass = adapterUtils().getMethod("getEntityClass", String.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("AdapterUtils.getEntityClass(String) não encontrado.", e);
        }
        Object classeEntidade = invocar(getEntityClass, null, entidade);

        Method getObjects = null;
        for (Method m : persistEngine().getMethods()) {
            if (m.getName().equals("getObjects") && m.getParameterCount() == 1
                    && m.getParameterTypes()[0] == Class.class) {
                getObjects = m;
                break;
            }
        }
        if (getObjects == null) {
            throw new IllegalStateException("PersistEngine.getObjects(Class) não encontrado nesta versão "
                    + "do Fusion — ajuste FormularioFusion.buscarTodos para a versão instalada.");
        }

        Object resultado = invocar(getObjects, null, classeEntidade);
        List<Registro> registros = new ArrayList<>();
        if (resultado instanceof List<?> lista) {
            for (Object neo : lista) {
                if (neo != null) {
                    registros.add(empacotar(neo));
                }
            }
        } else if (resultado != null) {
            registros.add(empacotar(resultado));
        }
        return registros;
    }

    // ------------------------------------------------------------------

    private static Object invocar(Method m, Object alvo, Object... args) throws Exception {
        try {
            return m.invoke(alvo, args);
        } catch (InvocationTargetException e) {
            Throwable causa = e.getCause() != null ? e.getCause() : e;
            if (causa instanceof Exception ex) {
                throw ex;
            }
            throw new IllegalStateException(causa);
        }
    }
}
