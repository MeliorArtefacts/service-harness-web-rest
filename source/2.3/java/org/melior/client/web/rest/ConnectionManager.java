/* __  __    _ _      
  |  \/  |  | (_)       
  | \  / | ___| |_  ___  _ __ 
  | |\/| |/ _ \ | |/ _ \| '__|
  | |  | |  __/ | | (_) | |   
  |_|  |_|\___|_|_|\___/|_|   
    Service Harness
*/
package org.melior.client.web.rest;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpHost;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.apache.http.protocol.HttpContext;
import org.melior.client.ssl.ClientSSLContext;
import org.melior.logging.core.Logger;
import org.melior.logging.core.LoggerFactory;

/**
 * TODO
 * @author Melior
 * @since 2.0
 */
public class ConnectionManager implements HttpClientConnectionManager{
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private PoolingHttpClientConnectionManager connectionManager;

    private ConnectionPool connectionPool;

  /**
   * Constructor.
   * @param configuration The client configuration
   * @param ssl The SSL indicator
   * @param sslContext The SSL context
   */
  public ConnectionManager(
    final RestClientConfig configuration,
    final boolean ssl,
    final SSLContext sslContext){
        super();

        RegistryBuilder<ConnectionSocketFactory> socketFactoryRegistryBuilder;

        socketFactoryRegistryBuilder = RegistryBuilder.<ConnectionSocketFactory>create()
      .register("http", PlainConnectionSocketFactory.getSocketFactory());

        if (ssl == true){

            if (sslContext != null){
                socketFactoryRegistryBuilder.register("https", new SSLConnectionSocketFactory(
          sslContext, NoopHostnameVerifier.INSTANCE));
      }
            else if ((configuration.getKeyStore() != null) || (configuration.getTrustStore() != null)){
                socketFactoryRegistryBuilder.register("https", new SSLConnectionSocketFactory(
          ClientSSLContext.ofKeyStore("TLS", configuration), NoopHostnameVerifier.INSTANCE));
      }
      else{
                socketFactoryRegistryBuilder.register("https", new SSLConnectionSocketFactory(
          ClientSSLContext.ofLenient("TLS"), NoopHostnameVerifier.INSTANCE));
      }

    }
    else{
            socketFactoryRegistryBuilder.register("https", SSLConnectionSocketFactory.getSocketFactory());
    }

        connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistryBuilder.build(),
      null, null, null, -1, TimeUnit.MILLISECONDS);
    connectionManager.setMaxTotal(1000);
    connectionManager.setDefaultMaxPerRoute(1000);
    connectionManager.setValidateAfterInactivity(Integer.MAX_VALUE);

        connectionPool = new ConnectionPool(configuration, connectionManager);
  }

  /**
   * Set maximum number of connections per route.
   * @param route The route
   * @param max The maximum number of connections
   */
  void setMaxPerRoute(
    final HttpRoute route,
    final int max){
        connectionManager.setMaxPerRoute(route, max);
  }

  /**
   * Set inactivity timeout for connections.
   * @param inactivityTimeout The inactivity timeout
   */
  void setValidateAfterInactivity(
    final int inactivityTimeout){
        connectionManager.setValidateAfterInactivity(Math.min(connectionManager.getValidateAfterInactivity(), inactivityTimeout));
  }

  /**
   * Request connection.
   * @param route The route
   * @param state The state
   * @return The connection request
   */
  public ConnectionRequest requestConnection(
    final HttpRoute route,
    final Object state){
    return getConnection(route, state);
  }

  /**
   * Get connection.
   * @param route The route
   * @param state The state
   * @return The connection request
   */
  public ConnectionRequest getConnection(
    final HttpRoute route,
    final Object state){
        String methodName = "getConnection";
    HttpHost host;
    PoolStats poolStats;
    final ConnectionRequest connectionRequest;

        host = route.getTargetHost();

        poolStats = connectionManager.getStats(route);

        logger.debug(methodName, "Connection pool [", host.getHostName(), ":", host.getPort(), "]: total=", (poolStats.getAvailable() + poolStats.getLeased()),
      ", active=", poolStats.getLeased(), ", deficit=", poolStats.getPending());

        connectionRequest = connectionPool.getConnection(route, state);

    return connectionRequest;
  }

  /**
   * Release connection.
   * @param connection The connection
   * @param newState The new state
   * @param validDuration The valid duration
   * @param timeUnit The time unit
   */
  public void releaseConnection(
    final HttpClientConnection connection,
    final Object newState,
    final long validDuration,
    final TimeUnit timeUnit){
        connectionPool.releaseConnection(connection, newState, validDuration, timeUnit);
  }

  public void connect(
    final HttpClientConnection connection,
    final HttpRoute route,
    final int connectTimeout,
    final HttpContext context) throws IOException{
    connectionManager.connect(connection, route, connectTimeout, context);
  }

  public void upgrade(
    final HttpClientConnection connection,
    final HttpRoute route,
    final HttpContext context) throws IOException{
    connectionManager.upgrade(connection, route, context);
  }

  public void routeComplete(
    final HttpClientConnection connection,
    final HttpRoute route,
    final HttpContext context) throws IOException{
    connectionManager.routeComplete(connection, route, context);
  }

  public void closeIdleConnections(
    final long idletime,
    final TimeUnit timeUnit){
    connectionManager.closeIdleConnections(idletime, timeUnit);
  }

  public void closeExpiredConnections(){
    connectionManager.closeExpiredConnections();
  }

  public void shutdown(){
    connectionManager.shutdown();
  }

}
