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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * TODO
 * @author Melior
 * @since 2.0
 */
@ControllerAdvice
public class RestServiceAutoConfiguration implements WebMvcConfigurer{
    private ServiceContext serviceContext;

  /**
   * Constructor.
   * @param serviceContext The service context
   */
  public RestServiceAutoConfiguration(
    final ServiceContext serviceContext){
        super();

        this.serviceContext = serviceContext;
  }

  /**
   * Add request interceptors.
   * @param interceptorRegistry The interceptor registry
   */
  public void addInterceptors(
    final InterceptorRegistry interceptorRegistry){
        interceptorRegistry.addInterceptor(new RestRequestInterceptor(serviceContext));
  }

  /**
   * Create exception handler for interface exceptions.
   * @param exception The interface exception
   * @return The response entity
   */
  @ExceptionHandler({RestInterfaceException.class})
  public ResponseEntity<ExceptionPayload> handleRestInterfaceError(
    final RestInterfaceException exception){
    return exception.getResponse();
  }

}
