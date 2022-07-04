/* __  __    _ _      
  |  \/  |  | (_)       
  | \  / | ___| |_  ___  _ __ 
  | |\/| |/ _ \ | |/ _ \| '__|
  | |  | |  __/ | | (_) | |   
  |_|  |_|\___|_|_|\___/|_|   
    Service Harness
*/
package org.melior.client.web.rest;
import org.melior.client.core.ClientConfig;

/**
 * TODO
 * @author Melior
 * @since 2.3
 */
public class RestClientConfig extends ClientConfig{
    private String proxyUrl;

    private String proxyUsername;

    private String proxyPassword;

  /**
   * Constructor.
   */
  protected RestClientConfig(){
        super();
  }

  /**
   * Configure client.
   * @param clientConfig The new client configuration parameters
   * @return The client configuration parameters
   */
  public RestClientConfig configure(
    final RestClientConfig clientConfig){
    super.configure(clientConfig);
    this.proxyUrl = clientConfig.proxyUrl;
    this.proxyUsername = clientConfig.proxyUsername;
    this.proxyPassword = clientConfig.proxyPassword;

    return this;
  }

  /**
   * Get proxy URL.
   * @return The proxy URL
   */
  public String getProxyUrl(){
    return proxyUrl;
  }

  /**
   * Set proxy URL.
   * @param proxyUrl The proxy URL
   */
  public void setProxyUrl(
    final String proxyUrl){
    this.proxyUrl = proxyUrl;
  }

  /**
   * Get proxy user name.
   * @return The proxy user name
   */
  public String getProxyUsername(){
    return proxyUsername;
  }

  /**
   * Set proxy user name.
   * @param proxyUsername The proxy user name
   */
  public void setProxyUsername(
    final String proxyUsername){
    this.proxyUsername = proxyUsername;
  }

  /**
   * Get proxy password.
   * @return The proxy password
   */
  public String getProxyPassword(){
    return proxyPassword;
  }

  /**
   * Set proxy password.
   * @param proxyPassword The proxy password
   */
  public void setProxyPassword(
    final String proxyPassword){
    this.proxyPassword = proxyPassword;
  }
 
}
