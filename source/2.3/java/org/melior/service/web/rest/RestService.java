/* __  __    _ _      
  |  \/  |  | (_)       
  | \  / | ___| |_  ___  _ __ 
  | |\/| |/ _ \ | |/ _ \| '__|
  | |  | |  __/ | | (_) | |   
  |_|  |_|\___|_|_|\___/|_|   
    Service Harness
*/
package org.melior.service.web.rest;
import org.melior.context.service.ServiceContext;
import org.melior.service.core.AbstractService;
import org.melior.service.exception.ApplicationException;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO
 * @author Melior
 * @since 2.0
 */
@RestController
public abstract class RestService extends AbstractService{

  /**
   * Bootstrap service.
   * @param serviceClass The service class
   * @param args The command line arguments
   */
  public static void run(
    final Class<?> serviceClass,
    final String[] args){
        AbstractService.run(serviceClass, args, true);
  }

  /**
   * Constructor.
   * @param serviceContext The service context
   * @throws ApplicationException if an error occurs during the construction
   */
  public RestService(
    final ServiceContext serviceContext) throws ApplicationException{
        super(serviceContext, true);
  }

}
