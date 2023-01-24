package org.melior.client.web.rest.patch;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.Assert;

/**
 * Copy of Spring Boot MappingJackson2XmlHttpMessageConverter with patches to improve SSL performance.
 * Will be dropped when Spring Boot is patched.
 */
public class MappingJackson2XmlHttpMessageConverter extends AbstractJackson2HttpMessageConverter {

    /**
     * Construct a new {@code MappingJackson2XmlHttpMessageConverter} using default configuration
     * provided by {@code Jackson2ObjectMapperBuilder}.
     */
    public MappingJackson2XmlHttpMessageConverter() {
        this(Jackson2ObjectMapperBuilder.xml().build());
    }

    /**
     * Construct a new {@code MappingJackson2XmlHttpMessageConverter} with a custom {@link ObjectMapper}
     * (must be a {@link XmlMapper} instance).
     * You can use {@link Jackson2ObjectMapperBuilder} to build it easily.
     * @see Jackson2ObjectMapperBuilder#xml()
     * @param objectMapper the
     */
    public MappingJackson2XmlHttpMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper, new MediaType("application", "xml", StandardCharsets.UTF_8),
                new MediaType("text", "xml", StandardCharsets.UTF_8),
                new MediaType("application", "*+xml", StandardCharsets.UTF_8));
        Assert.isInstanceOf(XmlMapper.class, objectMapper, "XmlMapper required");
    }


    /**
     * {@inheritDoc}
     * The {@code ObjectMapper} parameter must be a {@link XmlMapper} instance.
     */
    @Override
    public void setObjectMapper(ObjectMapper objectMapper) {
        Assert.isInstanceOf(XmlMapper.class, objectMapper, "XmlMapper required");
        super.setObjectMapper(objectMapper);
    }

}
