/* __  __      _ _            
  |  \/  |    | (_)           
  | \  / | ___| |_  ___  _ __ 
  | |\/| |/ _ \ | |/ _ \| '__|
  | |  | |  __/ | | (_) | |   
  |_|  |_|\___|_|_|\___/|_|   
        Service Harness
*/
package org.melior.client.web.rest;
import com.ctc.wstx.api.WstxInputProperties;
import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import javax.xml.stream.XMLResolver;
import org.codehaus.stax2.XMLInputFactory2;

/**
 * Create object mappers for REST requests where the request body is JSON or XML.
 * @author Melior
 * @since 2.3
 * @see ObjectMapper
 * @see XmlMapper
 */
public interface RestObjectMapper {

    /**
     * Create an object mapper with functionality for reading and writing JSON
     * to and from basic POJOs (Plain Old Java Objects).
     * <p>
     * The new object mapper will fail when encountering unknown properties.
     * @return The object mapper
     */
    public static ObjectMapper ofJSON() {
        return ofJSON(true);
    }

    /**
     * Create an object mapper with functionality for reading and writing JSON
     * to and from basic POJOs (Plain Old Java Objects).
     * @param failOnUnknownProperties {@code true} if the object mapper should
     * fail when encountering unknown properties, {@code false} otherwise
     * @return The object mapper
     */
    public static ObjectMapper ofJSON(
        final boolean failOnUnknownProperties) {

        ObjectMapper objectMapper;

        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, failOnUnknownProperties);

        return objectMapper;
    }

    /**
     * Create an object mapper with functionality for reading and writing XML
     * to and from basic POJOs (Plain Old Java Objects).
     * <p>
     * The new object mapper will be namespace aware and will fail when encountering
     * unknown properties or unresolved (general) entities.
     * @return The object mapper
     */
    public static ObjectMapper ofXML() {
        return ofXML(true, true, false);
    }

    /**
     * Create an object mapper with functionality for reading and writing XML
     * to and from basic POJOs (Plain Old Java Objects).
     * @param namespaceAware {@code true} if the object mapper should be
     * namespace aware, {@code false} otherwise
     * @param failOnUnknownProperties {@code true} if the object mapper should
     * fail when encountering unknown properties, {@code false} otherwise
     * @param deleteUnresolvedEntities {@code true} if unresolved (general) entities
     * should be deleted from the XML during processing, {@code false} otherwise
     * @return The object mapper
     */
    public static ObjectMapper ofXML(
        final boolean namespaceAware,
        final boolean failOnUnknownProperties,
        final boolean deleteUnresolvedEntities) {

        XMLInputFactory2 inputFactory;
        ObjectMapper objectMapper;

        inputFactory = new WstxInputFactory();
        inputFactory.setProperty(XMLInputFactory2.IS_NAMESPACE_AWARE, Boolean.valueOf(namespaceAware));

        if (deleteUnresolvedEntities == true) {
            inputFactory.setProperty(WstxInputProperties.P_UNDECLARED_ENTITY_RESOLVER,
                new XMLResolver() {public Object resolveEntity(String publicId, String systemId, String baseUri, String namespace)
                    {return ("nbsp".equals(namespace) == true) ? " " : "";}});
        }

        objectMapper = new XmlMapper(new XmlFactory(inputFactory, new WstxOutputFactory()));
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, failOnUnknownProperties);

        return objectMapper;
    }

}
