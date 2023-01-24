/* __  __      _ _            
  |  \/  |    | (_)           
  | \  / | ___| |_  ___  _ __ 
  | |\/| |/ _ \ | |/ _ \| '__|
  | |  | |  __/ | | (_) | |   
  |_|  |_|\___|_|_|\___/|_|   
        Service Harness
*/
package org.melior.client.web.rest;
import java.net.URI;
import org.apache.http.HttpHost;
import org.melior.client.exception.RemotingException;
import org.melior.service.exception.ExceptionType;

/**
 * Utility functions that apply to {@code HttpHost} objects.
 * @author Melior
 * @since 2.3
 */
public interface HttpHostUtil {

    /**
     * Convert URL to HTTP host.
     * @param url The URL
     * @return The HTTP host
     * @throws RemotingException if unable to convert the URL
     */
    static HttpHost urlToHost(
        final String url) throws RemotingException {

        URI uri;

        try {

            uri = new URI(url);
        }
        catch (Exception exception) {
            throw new RemotingException(ExceptionType.LOCAL_APPLICATION, "Failed to parse URL: " + exception.getMessage(), exception);
        }

        return new HttpHost(uri.getHost(), uri.getPort());
    }

}
