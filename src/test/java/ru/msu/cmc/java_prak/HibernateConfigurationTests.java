package ru.msu.cmc.java_prak;

import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.stream.IntStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.hibernate.cfg.Configuration;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;


// Тесты отдельного Hibernate-конфига.
public class HibernateConfigurationTests {

    @Test
    public void hibernateConfigurationShouldExistContainMappingsAndBeReadableByHibernate() throws Exception {
        InputStream resourceStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("hibernate.cfg.xml");

        assertNotNull(resourceStream, "Файл hibernate.cfg.xml должен присутствовать в resources");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));

        Document document;
        try (InputStream inputStream = resourceStream) {
            document = builder.parse(inputStream);
        }

        NodeList propertyNodes = document.getElementsByTagName("property");
        NodeList mappingNodes = document.getElementsByTagName("mapping");

        assertEquals(propertyNodes.getLength(), 8);
        assertEquals(mappingNodes.getLength(), 4);

        List<String> mappedClasses = IntStream.range(0, mappingNodes.getLength())
                .mapToObj(index -> mappingNodes.item(index).getAttributes().getNamedItem("class").getNodeValue())
                .toList();

        assertTrue(mappedClasses.contains("ru.msu.cmc.java_prak.model.Person"));
        assertTrue(mappedClasses.contains("ru.msu.cmc.java_prak.model.Company"));
        assertTrue(mappedClasses.contains("ru.msu.cmc.java_prak.model.Vacancy"));
        assertTrue(mappedClasses.contains("ru.msu.cmc.java_prak.model.WorkExperience"));

        Configuration configuration = new Configuration().configure("hibernate.cfg.xml");

        assertEquals(configuration.getProperty("hibernate.connection.url"), "jdbc:postgresql://localhost:5432/hr_agency");
        assertEquals(configuration.getProperty("hibernate.connection.username"), "postgres");
        assertEquals(configuration.getProperty("hibernate.hbm2ddl.auto"), "validate");
    }
}
