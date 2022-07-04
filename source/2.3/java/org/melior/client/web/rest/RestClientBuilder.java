/* __  __    _ _      
  |  \/  |  | (_)       
  | \  / | ___| |_  ___  _ __ 
  | |\/| |/ _ \ | |/ _ \| '__|
  | |  | |  __/ | | (_) | |   
  |_|  |_|\___|_|_|\___/|_|   
    Service Harness
*/
package org.melior.client.web.rest;
import javax.net.ssl.SSLContext;
import org.springframework.http.MediaType;

/**
 * TODO
 * @author Melior
 * @since 2.0
 */
public class RestClientBuilder{
    private MediaType mediaType = MediaType.APPLICATION_JSON;

    private boolean basicAuth = false;

    private boolean ssl = false;

    private SSLContext sslContext;

  /**
   * Constructor.
   */
  private RestClientBuilder(){
        super();
  }

  /**
   * Create REST client builder.
   * @return The REST client builder
   */
  public static RestClientBuilder create(){
        return new RestClientBuilder();
  }

  /**
   * Build REST client.
   * @return The REST client
   */
  public RestClient build(){
        return new RestClient(mediaType, basicAuth, ssl, sslContext);
  }

  /**
   * Set media type.
   * @param mediaType The media type
   * @return The REST client builder
   */
  public RestClientBuilder mediaType(
    final MediaType mediaType){
        this.mediaType = mediaType;

    return this;
  }

  /**
   * Enable basic auth.
   * @return The REST client builder
   */
  public RestClientBuilder basicAuth(){
        this.basicAuth = true;

    return this;
  }

  /**
   * Enable SSL.
   * @return The REST client builder
   */
  public RestClientBuilder ssl(){
        this.ssl = true;

    return this;
  }

  /**
   * Set SSL context.
   * @param sslContext The SSL context
   * @return The REST client builder
   */
  public RestClientBuilder sslContext(
    final SSLContext sslContext){
        this.sslContext = sslContext;

    return this;
  }

}
