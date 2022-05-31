/* __  __      _ _            
  |  \/  |    | (_)           
  | \  / | ___| |_  ___  _ __ 
  | |\/| |/ _ \ | |/ _ \| '__|
  | |  | |  __/ | | (_) | |   
  |_|  |_|\___|_|_|\___/|_|   
        Service Harness
*/
package org.melior.client.web.rest;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpHost;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.apache.http.protocol.HttpContext;
import org.melior.component.core.ServiceComponent;
import org.melior.context.service.ServiceContext;
import org.melior.service.exception.ApplicationException;
import org.melior.util.thread.DaemonThread;
import org.melior.util.thread.ThreadControl;
import org.springframework.stereotype.Component;

/**
 * TODO
 * @author Melior
 * @since 2.0
 */
@Component
public class ConnectionManager extends ServiceComponent implements HttpClientConnectionManager
{
		private PoolingHttpClientConnectionManager connectionManager;

		private ConnectionPool connectionPool;

		private int inactivityTimeout;

		private int pruneInterval;

	/**
	 * Constructor.
	 * @param serviceContext The service context
	 */
	public ConnectionManager(
		final ServiceContext serviceContext) throws ApplicationException
	{
				super(serviceContext);

				connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(1000);
		connectionManager.setValidateAfterInactivity(inactivityTimeout);

				connectionPool = new ConnectionPool(serviceContext, connectionManager);

				DaemonThread.create(() -> pruneExpiredConnections());
	}

	/**
	 * Get inactivity timeout.
	 * @return The inactivity timeout
	 */
	int getInactivityTimeout()
	{
		return inactivityTimeout;
	}

	/**
	 * Set maximum number of connections in total.
	 * @param max The maximum number of connections
	 */
    void setMaxTotal(
    	final int max)
    {
    	    	connectionManager.setMaxTotal(max);
    }

	/**
	 * Set default maximum number of connections per route.
	 * @param max The maximum number of connections
	 */
    void setDefaultMaxPerRoute(
    	final int max)
    {
    	    	connectionManager.setDefaultMaxPerRoute(max);
    }

	/**
	 * Set maximum number of connections per route.
	 * @param route The route
	 * @param max The maximum number of connections
	 */
    void setMaxPerRoute(
    	final HttpRoute route,
    	final int max)
    {
    	    	connectionManager.setMaxPerRoute(route, max);
    }

	/**
	 * Request connection.
	 * @param route The route
	 * @param state The state
	 * @return The connection request
	 */
	public ConnectionRequest requestConnection(
		final HttpRoute route,
		final Object state)
	{
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
		final Object state)
	{
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
		final TimeUnit timeUnit)
	{
				connectionPool.releaseConnection(connection, newState, validDuration, timeUnit);
	}

    /**
	 * Periodically prune expired connections.
     */
	private void pruneExpiredConnections()
	{
				String methodName = "pruneExpiredConnections";

				while ((isActive() == true) && (inactivityTimeout > 0) && (pruneInterval > 0))
		{

			try
			{
								connectionManager.closeExpiredConnections();
				connectionManager.closeIdleConnections(inactivityTimeout, TimeUnit.MILLISECONDS);

								ThreadControl.wait(this, pruneInterval);
			}
			catch (java.lang.Exception exception)
			{
				logger.error(methodName, "Failed to prune expired connections: ", exception.getMessage(), exception);
			}

		}

	}

	public void connect(
		final HttpClientConnection connection,
		final HttpRoute route,
		final int connectTimeout,
		final HttpContext context) throws IOException
	{
		connectionManager.connect(connection, route, connectTimeout, context);
	}

	public void upgrade(
		final HttpClientConnection connection,
		final HttpRoute route,
		final HttpContext context) throws IOException
	{
		connectionManager.upgrade(connection, route, context);
	}

	public void routeComplete(
		final HttpClientConnection connection,
		final HttpRoute route,
		final HttpContext context) throws IOException
	{
		connectionManager.routeComplete(connection, route, context);
	}

	public void closeIdleConnections(
		final long idletime,
		final TimeUnit timeUnit)
	{
		connectionManager.closeIdleConnections(idletime, timeUnit);
	}

	public void closeExpiredConnections()
	{
		connectionManager.closeExpiredConnections();
	}

	public void shutdown()
	{
		connectionManager.shutdown();
	}

	/**
	 * Configure connection manager.
	 * @throws ApplicationException when unable to configure the connection manager
	 */
	protected void configure() throws ApplicationException
	{
				inactivityTimeout = Integer.parseInt(configuration.getProperty("remoting.connection.inactivity-timeout", "30")) * 1000;

				pruneInterval = Integer.parseInt(configuration.getProperty("remoting.connection.prune-interval", "5")) * 1000;
	}

}
