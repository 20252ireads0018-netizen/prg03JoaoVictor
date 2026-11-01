package br.com.ifba.infrastructure.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Versionamento do banco com Flyway. As migrations ficam em
 * src/main/resources/db/migration (V1__..., V2__..., V3__...) e sao aplicadas
 * em sequencia no inicio da aplicacao, antes de o Hibernate validar o schema.
 *
 * A conexao (URL, usuario e senha) e lida do mesmo persistence.xml usado pelo
 * JPA, para nao duplicar credenciais em outro arquivo.
 */
public final class MigracaoBanco {

    private static final String LOCAL_MIGRATIONS = "classpath:db/migration";
    private static final String PERSISTENCE_XML = "META-INF/persistence.xml";

    private static final String PROP_URL = "jakarta.persistence.jdbc.url";
    private static final String PROP_USUARIO = "jakarta.persistence.jdbc.user";
    private static final String PROP_SENHA = "jakarta.persistence.jdbc.password";

    private MigracaoBanco() {
    }

    /** Aplica as migrations pendentes na unidade de persistencia em uso. */
    public static MigrateResult migrar() {
        return migrar(JPAUtil.getUnidade());
    }

    /** Aplica as migrations pendentes usando a conexao da unidade informada. */
    public static MigrateResult migrar(String unidade) {
        Map<String, String> jdbc = lerPropriedadesJdbc(unidade);
        Flyway flyway = Flyway.configure()
                .dataSource(jdbc.get(PROP_URL), jdbc.get(PROP_USUARIO), jdbc.getOrDefault(PROP_SENHA, ""))
                .locations(LOCAL_MIGRATIONS)
                .load();
        return flyway.migrate();
    }

    /**
     * Le as propriedades jakarta.persistence.jdbc.* da unidade de persistencia,
     * procurando em todos os META-INF/persistence.xml do classpath.
     */
    static Map<String, String> lerPropriedadesJdbc(String unidade) {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            for (URL url : Collections.list(loader.getResources(PERSISTENCE_XML))) {
                Map<String, String> props = lerUnidade(url, unidade);
                if (props != null) {
                    if (props.get(PROP_URL) == null) {
                        throw new IllegalStateException(
                                "A unidade '" + unidade + "' nao define " + PROP_URL + " em " + url);
                    }
                    return props;
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Nao foi possivel ler " + PERSISTENCE_XML, e);
        }
        throw new IllegalStateException("Unidade de persistencia '" + unidade + "' nao encontrada em nenhum "
                + PERSISTENCE_XML + ". Copie persistence.xml.exemplo para persistence.xml e preencha as credenciais.");
    }

    private static Map<String, String> lerUnidade(URL url, String unidade) {
        try (InputStream in = url.openStream()) {
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            fabrica.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            fabrica.setNamespaceAware(true);
            Document doc = fabrica.newDocumentBuilder().parse(in);
            NodeList unidades = doc.getElementsByTagNameNS("*", "persistence-unit");
            for (int i = 0; i < unidades.getLength(); i++) {
                Element pu = (Element) unidades.item(i);
                if (!unidade.equals(pu.getAttribute("name"))) {
                    continue;
                }
                Map<String, String> props = new HashMap<>();
                NodeList propriedades = pu.getElementsByTagNameNS("*", "property");
                for (int j = 0; j < propriedades.getLength(); j++) {
                    Element p = (Element) propriedades.item(j);
                    props.put(p.getAttribute("name"), p.getAttribute("value"));
                }
                return props;
            }
            return null;
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao ler " + url, e);
        }
    }
}
