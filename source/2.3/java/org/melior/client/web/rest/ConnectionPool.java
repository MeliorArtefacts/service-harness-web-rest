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
import org.melior.logging.core.Logger;
import org.melior.logging.core.LoggerFactory;
import org.melior.service.core.ServiceState;
import org.melior.util.thread.DaemonThread;
import org.melior.util.thread.ThreadControl;

/**
 * Implements a pool of persistent HTTP {@code Connection} objects.
 * <p>
 * The pool adds additional {@code Connection} objects as and when demand requires,
 * but at the same time employs elision logic to ensure that the pool does not add
 * surplus {@code Connection} objects when the demand subsides following a surge.
 * <p>
 * The pool may also be configured to be bounded, in which case the pool will not
 * exceed its bounds when demand surges or subsides.
 * @author Melior
 * @since 2.0
 */
class ConnectionPool {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private RestClientConfig configuration;

    private PoolingHttpClientConnectionManager connectionManager;

    /**
     * Constructor.
     * @param configuration The client configuration
     * @param connectionManager The connection manager
     */
    public ConnectionPool(
        final RestClientConfig configuration,
        final PoolingHttpClientConnectionManager connectionManager) {

        super();

        this.configuration = configuration;

        this.connectionManager = connectionManager;

        DaemonThread.create(() -> pruneExpiredConnections());
    }

    /**
     * Get connection.
     * @param route The route
     * @param state The state
     * @return The connection request
     */
    public ConnectionRequest getConnection(
        final HttpRoute route,
        final Object state) {

        final ConnectionRequest connectionRequest;

        connectionRequest = connectionManager.requestConnection(route, state);

        return new ConnectionRequest() {

            /**
             * Cancel connection request.
             */
            public boolean cancel() {

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
                final TimeUnit timeUnit) throws InterruptedException, ExecutionException, ConnectionPoolTimeoutException {

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
        final TimeUnit timeUnit) {

        String methodName = "releaseConnection";
        int id;

        id = Math.abs(((ManagedHttpClientConnection) connection).getId().hashCode());

        connectionManager.releaseConnection(connection, newState, validDuration, timeUnit);

        logger.debug(methodName, "Connection [id=", id, "] released.");
    }

    /**
     * Periodically prune expired connections.
     */
    private void pruneExpiredConnections() {

        String methodName = "pruneExpiredConnections";

        while ((ServiceState.isActive() == true) && (connectionManager.getValidateAfterInactivity() > 0)) {

            try {

                connectionManager.closeExpiredConnections();
                connectionManager.closeIdleConnections(connectionManager.getValidateAfterInactivity(), TimeUnit.MILLISECONDS);

                ThreadControl.wait(this, configuration.getPruneInterval(), TimeUnit.MILLISECONDS);
            }
            catch (Exception exception) {
                logger.error(methodName, "Failed to prune expired connections: ", exception.getMessage(), exception);
            }

        }

    }

}
