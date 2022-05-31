/* __  __      _ _            
  |  \/  |    | (_)           
  | \  / | ___| |_  ___  _ __ 
  | |\/| |/ _ \ | |/ _ \| '__|
  | |  | |  __/ | | (_) | |   
  |_|  |_|\___|_|_|\___/|_|   
        Service Harness
*/
package org.melior.client.web.rest;
import org.melior.client.core.ResponseExceptionMapper;
import org.melior.service.exception.ExceptionType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * TODO
 * @author Melior
 * @since 2.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({
    "type",
    "code",
    "message",
    "status",
    "error"
})
@JacksonXmlRootElement(localName = "Response")
public class RestResponseExceptionMapper implements ResponseExceptionMapper
{
	@JsonProperty("type")
    private String type;

	@JsonProperty("code")
    private String code;

	@JsonProperty("message")
    private String message;

	@JsonProperty("status")
    private String status;

	@JsonProperty("error")
    private String error;

	/**
	 * @return the type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type)
	{
		this.type = type;
	}

	/**
	 * @return the code
	 */
	public String getCode()
	{
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code)
	{
		this.code = code;
	}

	/**
	 * @return the message
	 */
	public String getMessage()
	{
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message)
	{
		this.message = message;
	}

	/**
	 * @return the status
	 */
	public String getStatus()
	{
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status)
	{
		this.status = status;
	}

	/**
	 * @return the error
	 */
	public String getError()
	{
		return error;
	}

	/**
	 * @param error the error to set
	 */
	public void setError(String error)
	{
		this.error = error;
	}

	/**
	 * Get exception type.
	 * @return The exception type
	 */
	public ExceptionType getExceptionType()
	{

				if (type != null)
		{
						return ExceptionType.valueOf(type);
		}

				if (status != null)
		{
						return getExceptionType(Integer.parseInt(status));
		}

		return null;
	}

	/**
	 * Get exception code.
	 * @return The exception code
	 */
	public String getExceptionCode()
	{
		return (code == null) ? status : code;
	}

	/**
	 * Get exception message.
	 * @return The exception message
	 */
	public String getExceptionMessage()
	{
		return ((message == null) || (message.length() == 0))
			? (((status == null) || (status.length() == 0)
			|| (error == null) || (error.length() == 0))
			? null : status + " " + error) : message;
	}

	/**
	 * Get exception type.
	 * @param statusCode The status code
	 * @return The exception type
	 */
	public static ExceptionType getExceptionType(
		final int statusCode)
	{
				ExceptionType exceptionType;

				if ((statusCode == 401) || (statusCode == 403) || (statusCode == 407))
		{
			exceptionType = ExceptionType.SECURITY;
		}
		else if (statusCode == 404)
		{
			exceptionType = ExceptionType.NO_DATA;
		}
		else if (statusCode == 429)
		{
			exceptionType = ExceptionType.SERVICE_OVERLOAD;
		}
		else if ((statusCode >= 400) && (statusCode < 500))
		{
			exceptionType = ExceptionType.REMOTING_APPLICATION;
		}
		else if (statusCode == 503)
		{
			exceptionType = ExceptionType.SERVICE_UNAVAILABLE;
		}
		else
		{
			exceptionType = ExceptionType.REMOTING_SYSTEM;
		}

		return exceptionType;
	}

}
