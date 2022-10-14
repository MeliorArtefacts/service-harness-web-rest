package org.melior.client.web.rest.patch;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.lang.Nullable;

/**
 * Copy of Spring Boot MappingJackson2HttpMessageConverter with patches to improve SSL performance.
 * Will be dropped when Spring Boot is patched.
 */
public class MappingJackson2HttpMessageConverter extends AbstractJackson2HttpMessageConverter {

  @Nullable
  private String jsonPrefix;


  /**
   * Construct a new {@link MappingJackson2HttpMessageConverter} using default configuration
   * provided by {@link Jackson2ObjectMapperBuilder}.
   */
  public MappingJackson2HttpMessageConverter() {
    this(Jackson2ObjectMapperBuilder.json().build());
  }

  /**
   * Construct a new {@link MappingJackson2HttpMessageConverter} with a custom {@link ObjectMapper}.
   * You can use {@link Jackson2ObjectMapperBuilder} to build it easily.
   * @see Jackson2ObjectMapperBuilder#json()
   * @param objectMapper the
   */
  public MappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
    super(objectMapper, MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));
  }


  /**
   * Specify a custom prefix to use for this view's JSON output.
   * Default is none.
   * @see #setPrefixJson
   * @param jsonPrefix the
   */
  public void setJsonPrefix(String jsonPrefix) {
    this.jsonPrefix = jsonPrefix;
  }

  /**
   * Indicate whether the JSON output by this view should be prefixed with ")]}', ". Default is false.
   * <p>Prefixing the JSON string in this manner is used to help prevent JSON Hijacking.
   * The prefix renders the string syntactically invalid as a script so that it cannot be hijacked.
   * This prefix should be stripped before parsing the string as JSON.
   * @see #setJsonPrefix
   * @param prefixJson the
   */
  public void setPrefixJson(boolean prefixJson) {
    this.jsonPrefix = (prefixJson ? ")]}', " : null);
  }


  @Override
  protected void writePrefix(JsonGenerator generator, Object object) throws IOException {
    if (this.jsonPrefix != null) {
      generator.writeRaw(this.jsonPrefix);
    }
  }

}
