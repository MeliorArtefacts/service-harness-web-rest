/* __  __      _ _            
  |  \/  |    | (_)           
  | \  / | ___| |_  ___  _ __ 
  | |\/| |/ _ \ | |/ _ \| '__|
  | |  | |  __/ | | (_) | |   
  |_|  |_|\___|_|_|\___/|_|   
        Service Harness
*/
package org.melior.service.web.rest;
import org.melior.service.exception.ExceptionType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * An application should raise this exception in the top-level methods
 * of a {@code Controller} that handles HTTP REST requests from client
 * applications.  The configured {@code ExceptionHandler} will then
 * return the standard exception payload to the client application with
 * the exception details and the appropriate HTTP status code automatically
 * populated.
 * @author Melior
 * @since 2.0
 * @see ExceptionPayload
 */
public class RestInterfaceException extends Exception {

    private HttpStatus status;

    private ExceptionType type;

    private String code;

    /**
     * Constructor.
     */
    public RestInterfaceException() {

        super();
    }

    /**
     * Constructor.
     * @param type The exception type
     * @param code The exception code
     * @param message The exception message
     */
    public RestInterfaceException(
        final ExceptionType type,
        final String code,
        final String message) {

        this(null, type, code, message);
    }

    /**
     * Constructor.
     * @param status The response status
     * @param type The exception type
     * @param code The exception code
     * @param message The exception message
     */
    public RestInterfaceException(
        final HttpStatus status,
        final ExceptionType type,
        final String code,
        final String message) {

        super(message);

        this.status = (status == null) ? getStatus(type) : status;

        this.type = type;

        this.code = code;
    }

    /**
     * Get response status.
     * @return The response status
     */
    public HttpStatus getStatus() {
        return status;
    }

    /**
     * Get exception type.
     * @return The exception type
     */
    public ExceptionType getType() {
        return type;
    }

    /**
     * Get exception code.
     * @return The exception code
     */
    public final String getCode() {
        return code;
    }

    /**
     * Get response status.
     * @param type The exception type
     * @return The response status
     */
    private HttpStatus getStatus(
        final ExceptionType type) {

        switch (type) {
        case NO_DATA:
            return HttpStatus.NOT_FOUND;
        case SERVICE_OVERLOAD:
            return HttpStatus.TOO_MANY_REQUESTS;
        case SERVICE_UNAVAILABLE:
        case DATAACCESS_COMMUNICATION:
            return HttpStatus.SERVICE_UNAVAILABLE;
        default:
            return HttpStatus.BAD_REQUEST;
        }

    }

    /**
     * Get response.
     * @return The response
     */
    public ResponseEntity<ExceptionPayload> getResponse() {

        return new ResponseEntity<ExceptionPayload>(new ExceptionPayload(type.name(), code, getMessage()), status);
    }

    /**
     * Throw checked exception as runtime exception.
     * @param exception The checked exception
     */
    public static void throwRuntimeException(
        final Exception exception) {
        RestInterfaceException.<RuntimeException> throwException(exception);
    }

    /**
     * Throw exception.
     * @param exception The exception
     * @throws E The exception
     */
    @SuppressWarnings("unchecked")
    private static <E extends Exception> void throwException(
        final Exception exception) throws E {
        throw (E) exception;
    }

}
