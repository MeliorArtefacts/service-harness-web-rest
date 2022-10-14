/* __  __    _ _      
  |  \/  |  | (_)       
  | \  / | ___| |_  ___  _ __ 
  | |\/| |/ _ \ | |/ _ \| '__|
  | |  | |  __/ | | (_) | |   
  |_|  |_|\___|_|_|\___/|_|   
    Service Harness
*/
package org.melior.client.web.rest;
import org.apache.http.protocol.HttpContext;

/**
 * TODO
 * @author Melior
 * @since 2.0
 */
public class UserTokenHandler implements org.apache.http.client.UserTokenHandler{

  /**
   * Get user token.
   * @param context The HTTP context
   * @return The user token
   */
  public Object getUserToken(
    final HttpContext context){
    return null;
  }

}
