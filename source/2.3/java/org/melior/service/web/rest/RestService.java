/* __  __      _ _            
  |  \/  |    | (_)           
  | \  / | ___| |_  ___  _ __ 
  | |\/| |/ _ \ | |/ _ \| '__|
  | |  | |  __/ | | (_) | |   
  |_|  |_|\___|_|_|\___/|_|   
        Service Harness
*/
package org.melior.service.web.rest;
import org.melior.client.web.rest.RestClient;
import org.melior.context.service.ServiceContext;
import org.melior.service.core.AbstractService;
import org.melior.service.exception.ApplicationException;
import org.springframework.web.bind.annotation.RestController;

/**
 * A base class for service implementations that handle HTTP REST requests.
 * Any service implementation class which derives from this class will be
 * discovered by the Spring Boot component scanner as a {@code RestController}.
 * <p>
 * The service implementation class is also furnished with important constructs
 * like the service context, the service configuration and a logger.
 * <p>
 * Any HTTP REST requests which are received by the application are routed
 * past the configured {@code WorkManager} to allow the {@code WorkManager} to
 * control the flow of requests through the application.
 * <p>
 * Any request id or correlation id which is present in the headers of an HTTP
 * REST request is captured in the transaction context and logged in the application
 * logs.   The request id or correlation id is also forwarded to other applications
 * when using a {@code RestClient}. This provides a measure of tracing between
 * applications.
 * @author Melior
 * @since 2.0
 * @see RestRequestInterceptor
 * @see AbstractService
 * @see RestClient
 */
@RestController
public abstract class RestService extends AbstractService {

    /**
     * Bootstrap service.
     * @param serviceClass The service class
     * @param args The command line arguments
     */
    public static void run(
        final Class<?> serviceClass,
        final String[] args) {

        AbstractService.run(serviceClass, args, true);
    }

    /**
     * Constructor.
     * @param serviceContext The service context
     * @throws ApplicationException if an error occurs during the construction
     */
    public RestService(
        final ServiceContext serviceContext) throws ApplicationException {

        super(serviceContext, true);
    }

}
