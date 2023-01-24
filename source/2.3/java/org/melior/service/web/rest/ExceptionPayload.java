/* __  __      _ _            
  |  \/  |    | (_)           
  | \  / | ___| |_  ___  _ __ 
  | |\/| |/ _ \ | |/ _ \| '__|
  | |  | |  __/ | | (_) | |   
  |_|  |_|\___|_|_|\___/|_|   
        Service Harness
*/
package org.melior.service.web.rest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * The standard payload which is returned to a client application by the
 * configured {@code ExceptionHandler} when an exception has occurred and
 * the exception has propagated out of the {@code Controller} that handled
 * the HTTP REST request from the client application.
 * @author Melior
 * @since 2.0
 * @see ExceptionHandler
 */
@JacksonXmlRootElement(localName = "ExceptionPayload")
@JsonPropertyOrder({"type", "code", "message"})
public class ExceptionPayload {

    @JacksonXmlProperty(localName = "type")
    private String type;

    @JacksonXmlProperty(localName = "code")
    private String code;

    @JacksonXmlProperty(localName = "message")
    private String message;

    /**
     * Constructor.
     */
    public ExceptionPayload() {

        super();
    }

    /**
     * Constructor.
     * @param type The exception type
     * @param code The exception code
     * @param message The exception message
     */
    public ExceptionPayload(
        final String type,
        final String code,
        final String message) {

        super();

        this.type = type;

        this.code = code;

        this.message = message;
    }

    /**
     * Get exception type.
     * @return The exception type
     */
    public String getType() {
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
     * Get exception message.
     * @return The exception message
     */
    public final String getMessage() {
        return message;
    }

}
