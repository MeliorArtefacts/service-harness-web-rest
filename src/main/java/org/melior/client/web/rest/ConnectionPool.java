/* __  __      _ _            
  |  \/  |    | (_)           
  | \  / | ___| |_  ___  _ __ 
  | |\/| |/ _ \ | |/ _ \| '__|
  | |  | |  __/ | | (_) | |   
  |_|  |_|\___|_|_|\___/|_|   
        Service Harness
*/
package org.melior.client.web.rest;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpClientConnection;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.melior.component.core.ServiceComponent;
import org.melior.context.service.ServiceContext;
import org.melior.service.exception.ApplicationException;

/**
 * TODO
 * @author Melior
 * @since 2.0
 */
class ConnectionPool extends ServiceComponent
{
		private PoolingHttpClientConnectionManager connectionManager;

	/**
	 * Constructor.
	 * @param serviceContext The service context
	 * @param connectionManager The connection manager
	 */
	public ConnectionPool(
		final ServiceContext serviceContext,
		final PoolingHttpClientConnectionManager connectionManager) throws ApplicationException
	{
				super(serviceContext);

				this.connectionManager = connectionManager;
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
				final ConnectionRequest connectionRequest;

				connectionRequest = connectionManager.requestConnection(route, state);

				return new ConnectionRequest()
		{

			/**
			 * Cancel connection request.
			 */
			public boolean cancel()
			{
								return connectionRequest.cancel();
			}

			/**
			 * Get connection.
			 * @param timeout The timeout
			 * @param timeUnit The time unit
			 * @return The connection
			 */
			public HttpClientConnection get(
				final long timeout,
				final TimeUnit timeUnit) throws InterruptedException, ExecutionException, ConnectionPoolTimeoutException
			{
								String methodName = "getConnection";
				HttpClientConnection connection;
				int id;

								connection = connectionRequest.get(timeout, timeUnit);

								id = Math.abs(((ManagedHttpClientConnection) connection).getId().hashCode());

				logger.debug(methodName, "Connection [id=", id, "] allocated.");

				return connection;
			}

		};

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
				String methodName = "releaseConnection";
		int id;

				id = Math.abs(((ManagedHttpClientConnection) connection).getId().hashCode());

				connectionManager.releaseConnection(connection, newState, validDuration, timeUnit);

		logger.debug(methodName, "Connection [id=", id, "] released.");
	}

}
