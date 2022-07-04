/* __  __    _ _      
  |  \/  |  | (_)       
  | \  / | ___| |_  ___  _ __ 
  | |\/| |/ _ \ | |/ _ \| '__|
  | |  | |  __/ | | (_) | |   
  |_|  |_|\___|_|_|\___/|_|   
    Service Harness
*/
package org.melior.client.web.rest;
import java.net.URI;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

/**
 * TODO
 * @author Melior
 * @since 2.2
 */
public class RequestFactoryBuilder{
    private ConnectionManager connectionManager;

    private URI uri;

    private int maximumConnections = 1000;

    private int connectionTimeout = 0;

    private int requestTimeout = 60000;

  /**
   * Constructor.
   */
  private RequestFactoryBuilder(){
        super();
  }

  /**
   * Create request factory builder.
   * @return The request factory builder
   */
  public static RequestFactoryBuilder create(){
        return new RequestFactoryBuilder();
  }

  /**
   * Build request factory.
   * @return The request factory
   */
  public ClientHttpRequestFactory build(){
        RequestConfig requestConfig;
    CloseableHttpClient httpClient;
    HttpComponentsClientHttpRequestFactory requestFactory;

        if (connectionManager == null){
      throw new RuntimeException("Connection manager must be provided.");
    }

        if (uri != null){
            connectionManager.setMaxPerRoute(new HttpRoute(new HttpHost(uri.getHost(), uri.getPort())), maximumConnections);
    }
    else{
            connectionManager.setDefaultMaxPerRoute(maximumConnections);
    }

        requestConfig =  RequestConfig.custom()
      .setConnectionRequestTimeout(connectionTimeout)
      .setConnectTimeout(requestTimeout)
      .setSocketTimeout(requestTimeout)
      .build();

        httpClient = HttpClientBuilder.create()
      .setConnectionManager(connectionManager)
      .setConnectionManagerShared(true)
      .setKeepAliveStrategy(new ConnectionKeepAliveStrategy(connectionManager))
      .setDefaultRequestConfig(requestConfig)
      .build();

        requestFactory = new HttpComponentsClientHttpRequestFactory();
    requestFactory.setHttpClient(httpClient);

    return requestFactory;
  }

  /**
   * Set connection manager.
   * @param connectionManager The connection manager
   * @return The request factory builder
   */
  public RequestFactoryBuilder connectionManager(
    final ConnectionManager connectionManager){
        this.connectionManager = connectionManager;

    return this;
  }

  /**
   * Set URI.
   * @param uri The URI
   * @return The request factory builder
   */
  public RequestFactoryBuilder uri(
    final URI uri){
        this.uri = uri;

    return this;
  }

  /**
   * Set maximum number of connections.
   * @param maximumConnections The maximum number of connections
   * @return The request factory builder
   */
  public RequestFactoryBuilder maximumConnections(
    final int maximumConnections){
        this.maximumConnections = maximumConnections;

    return this;
  }

  /**
   * Set connection timeout.
   * @param connectionTimeout The connection timeout
   * @return The request factory builder
   */
  public RequestFactoryBuilder connectionTimeout(
    final int connectionTimeout){
        this.connectionTimeout = connectionTimeout;

    return this;
  }

  /**
   * Set request timeout.
   * @param requestTimeout The request timeout
   * @return The request factory builder
   */
  public RequestFactoryBuilder requestTimeout(
    final int requestTimeout){
        this.requestTimeout = requestTimeout;

    return this;
  }

}
