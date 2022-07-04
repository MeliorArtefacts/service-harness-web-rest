/* __  __    _ _      
  |  \/  |  | (_)       
  | \  / | ___| |_  ___  _ __ 
  | |\/| |/ _ \ | |/ _ \| '__|
  | |  | |  __/ | | (_) | |   
  |_|  |_|\___|_|_|\___/|_|   
    Service Harness
*/
package org.melior.client.web.rest;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

/**
 * TODO
 * @author Melior
 * @since 2.0
 */
class ConnectionKeepAliveStrategy implements org.apache.http.conn.ConnectionKeepAliveStrategy{
    private int inactivityTimeout;

  /**
   * Constructor.
   * @param inactivityTimeout The connection inactivity timeout
   */
  public ConnectionKeepAliveStrategy(
    final int inactivityTimeout){
        super();

        this.inactivityTimeout = inactivityTimeout;
  }

  /**
   * Get keep alive duration.
   * @param response The HTTP response
   * @param context The HTTP context
   * @return The keep alive duration
   */
  public long getKeepAliveDuration(
    final HttpResponse response,
    final HttpContext context){
        HeaderElementIterator iterator;
    HeaderElement element;
    String name;
    String value;

        for (iterator = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE)); iterator.hasNext() == true;){
      element = iterator.nextElement();

            name = element.getName();
      value = element.getValue();

            if ((value != null) && (name.equalsIgnoreCase("timeout") == true)){
                return Long.parseLong(value) * 1000;
      }

    }

        return inactivityTimeout;
  }

}
