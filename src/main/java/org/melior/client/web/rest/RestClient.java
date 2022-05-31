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
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.codehaus.stax2.XMLInputFactory2;
import org.melior.client.core.RawAwarePayload;
import org.melior.client.core.ResponseExceptionMapper;
import org.melior.client.exception.RemotingException;
import org.melior.context.transaction.TransactionContext;
import org.melior.logging.core.Logger;
import org.melior.logging.core.LoggerFactory;
import org.melior.service.exception.ExceptionType;
import org.melior.util.time.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * TODO
 * @author Melior
 * @since 2.0
 */
public class RestClient extends RestClientConfig
{
		@Autowired
	private ConnectionManager connectionManager;

		private Logger logger;

		private MediaType mediaType;

		private boolean basicAuth;

		private ObjectMapper objectMapper;

		private RestTemplate restTemplate;

	/**
	 * Constructor.
	 * @param mediaType The media type
	 * @param basicAuth The basic auth indicator
	 */
	RestClient(
		final MediaType mediaType,
		final boolean basicAuth)
	{
				super();

				logger = LoggerFactory.getLogger(this.getClass());

				this.mediaType = mediaType;

				this.basicAuth = basicAuth;
	}

	/**
	 * Initialize client.
	 * @throws RemotingException when unable to initialize the client
	 */
	private void initialize() throws RemotingException
	{
				URI uri;
		XMLInputFactory2 inputFactory;
		ClientHttpRequestFactory requestFactory;

				if (restTemplate != null)
		{
			return;
		}

				if (getUrl() == null)
		{
			throw new RemotingException(ExceptionType.LOCAL_APPLICATION,
				"URL must be configured.");
		}

		try
		{
						uri = new URI(getUrl());
		}
		catch (Exception exception)
		{
			throw new RemotingException(ExceptionType.LOCAL_APPLICATION,
				"Failed to parse URI: " + exception.getMessage(), exception);
		}

				if (basicAuth == true)
		{

						if (getUsername() == null)
			{
				throw new RemotingException(ExceptionType.LOCAL_APPLICATION,
					"User name must be configured.");
			}

						if (getPassword() == null)
			{
				throw new RemotingException(ExceptionType.LOCAL_APPLICATION,
					"Password must be configured.");
			}

		}

				if ((mediaType == MediaType.APPLICATION_XML)
			|| (mediaType == MediaType.TEXT_XML))
		{
						inputFactory = new WstxInputFactory();
			inputFactory.setProperty(XMLInputFactory2.IS_NAMESPACE_AWARE, Boolean.FALSE);

						objectMapper = new XmlMapper(new XmlFactory(inputFactory, new WstxOutputFactory()));
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		}
		else
		{
						objectMapper = new ObjectMapper();
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		}

				requestFactory = RequestFactoryBuilder.create()
			.connectionManager(connectionManager)
			.uri(uri)
			.maximumConnections(getMaximumConnections())
			.connectionTimeout(getConnectionTimeout())
			.requestTimeout(getRequestTimeout())
			.build();

				restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(requestFactory);
	}

	/**
	 * Send GET request and receive response.
	 * @param uriParameters The URI parameters
	 * @param responseType The response object type
	 * @param exceptionMapper The response exception mapper
	 * @return The response object
	 * @throws RemotingException when unable to send the request, or when an error response is received
	 */
	public <Rs, Em extends ResponseExceptionMapper> Rs get(
		final Object[] uriParameters,
		final Class<Rs> responseType,
		final Class<Em> exceptionMapper) throws RemotingException
	{
				return exchange(HttpMethod.GET, uriParameters, null, responseType, exceptionMapper);
	}

	/**
	 * Send GET request and receive response.
	 * @param responseType The response object type
	 * @param exceptionMapper The response exception mapper
	 * @return The response object
	 * @throws RemotingException when unable to send the request, or when an error response is received
	 */
	public <Rs, Em extends ResponseExceptionMapper> Rs get(
		final Class<Rs> responseType,
		final Class<Em> exceptionMapper) throws RemotingException
	{
				return exchange(HttpMethod.GET, null, null, responseType, exceptionMapper);
	}

	/**
	 * Send GET request and receive response.
	 * @param uriParameters The URI parameters
	 * @param responseType The response object type
	 * @return The response object
	 * @throws RemotingException when unable to send the request, or when an error response is received
	 */
	public <Rs> Rs get(
		final Object[] uriParameters,
		final Class<Rs> responseType) throws RemotingException
	{
				return exchange(HttpMethod.GET, uriParameters, null, responseType, RestResponseExceptionMapper.class);
	}

	/**
	 * Send GET request and receive response.
	 * @param responseType The response object type
	 * @return The response object
	 * @throws RemotingException when unable to send the request, or when an error response is received
	 */
	public <Rs> Rs get(
		final Class<Rs> responseType) throws RemotingException
	{
				return exchange(HttpMethod.GET, null, null, responseType, RestResponseExceptionMapper.class);
	}

	/**
	 * Send POST request and receive response.
	 * @param uriParameters The URI parameters
	 * @param request The request object
	 * @param responseType The response object type
	 * @param exceptionMapper The response exception mapper
	 * @return The response object
	 * @throws RemotingException when unable to send the request, or when an error response is received
	 */
	public <Rq, Rs, Em extends ResponseExceptionMapper> Rs post(
		final Object[] uriParameters,
		final Rq request,
		final Class<Rs> responseType,
		final Class<Em> exceptionMapper) throws RemotingException
	{
				return exchange(HttpMethod.POST, uriParameters, request, responseType, exceptionMapper);
	}

	/**
	 * Send POST request and receive response.
	 * @param request The request object
	 * @param responseType The response object type
	 * @param exceptionMapper The response exception mapper
	 * @return The response object
	 * @throws RemotingException when unable to send the request, or when an error response is received
	 */
	public <Rq, Rs, Em extends ResponseExceptionMapper> Rs post(
		final Rq request,
		final Class<Rs> responseType,
		final Class<Em> exceptionMapper) throws RemotingException
	{
				return exchange(HttpMethod.POST, null, request, responseType, exceptionMapper);
	}

	/**
	 * Send POST request and receive response.
	 * @param uriParameters The URI parameters
	 * @param request The request object
	 * @param responseType The response object type
	 * @return The response object
	 * @throws RemotingException when unable to send the request, or when an error response is received
	 */
	public <Rq, Rs> Rs post(
		final Object[] uriParameters,
		final Rq request,
		final Class<Rs> responseType) throws RemotingException
	{
				return exchange(HttpMethod.POST, uriParameters, request, responseType, RestResponseExceptionMapper.class);
	}

	/**
	 * Send POST request and receive response.
	 * @param request The request object
	 * @param responseType The response object type
	 * @return The response object
	 * @throws RemotingException when unable to send the request, or when an error response is received
	 */
	public <Rq, Rs> Rs post(
		final Rq request,
		final Class<Rs> responseType) throws RemotingException
	{
				return exchange(HttpMethod.POST, null, request, responseType, RestResponseExceptionMapper.class);
	}

	/**
	 * Send PUT request and receive response.
	 * @param uriParameters The URI parameters
	 * @param request The request object
	 * @param responseType The response object type
	 * @param exceptionMapper The response exception mapper
	 * @return The response object
	 * @throws RemotingException when unable to send the request, or when an error response is received
	 */
	public <Rq, Rs, Em extends ResponseExceptionMapper> Rs put(
		final Object[] uriParameters,
		final Rq request,
		final Class<Rs> responseType,
		final Class<Em> exceptionMapper) throws RemotingException
	{
				return exchange(HttpMethod.PUT, uriParameters, request, responseType, exceptionMapper);
	}

	/**
	 * Send PUT request and receive response.
	 * @param request The request object
	 * @param responseType The response object type
	 * @param exceptionMapper The response exception mapper
	 * @return The response object
	 * @throws RemotingException when unable to send the request, or when an error response is received
	 */
	public <Rq, Rs, Em extends ResponseExceptionMapper> Rs put(
		final Rq request,
		final Class<Rs> responseType,
		final Class<Em> exceptionMapper) throws RemotingException
	{
				return exchange(HttpMethod.PUT, null, request, responseType, exceptionMapper);
	}

	/**
	 * Send PUT request and receive response.
	 * @param uriParameters The URI parameters
	 * @param request The request object
	 * @param responseType The response object type
	 * @return The response object
	 * @throws RemotingException when unable to send the request, or when an error response is received
	 */
	public <Rq, Rs> Rs put(
		final Object[] uriParameters,
		final Rq request,
		final Class<Rs> responseType) throws RemotingException
	{
				return exchange(HttpMethod.PUT, uriParameters, request, responseType, RestResponseExceptionMapper.class);
	}

	/**
	 * Send PUT request and receive response.
	 * @param request The request object
	 * @param responseType The response object type
	 * @return The response object
	 * @throws RemotingException when unable to send the request, or when an error response is received
	 */
	public <Rq, Rs> Rs put(
		final Rq request,
		final Class<Rs> responseType) throws RemotingException
	{
				return exchange(HttpMethod.PUT, null, request, responseType, RestResponseExceptionMapper.class);
	}

	
	/**
	 * Send request and receive response.
	 * @param method The HTTP method
	 * @param uriParameters The URI parameters
	 * @param request The request object
	 * @param responseType The response object type
	 * @param exceptionMapper The response exception mapper
	 * @return The response object
	 * @throws RemotingException when unable to send the request, or when an error response is received
	 */
	private <Rq, Rs, Em extends ResponseExceptionMapper> Rs exchange(
		final HttpMethod method,
		final Object[] uriParameters,
		final Rq request,
		final Class<Rs> responseType,
		final Class<Em> exceptionMapper) throws RemotingException
	{
				String methodName = method.name().toLowerCase();
		String payload;
		TransactionContext transactionContext;
		HttpHeaders headers;
		HttpEntity<String> requestEntity;
		Timer timer;
		ResponseEntity<String> responseEntity;
		long duration;
		Rs response;

				initialize();

		try
		{
						payload = ((method == HttpMethod.GET) || (method == HttpMethod.DELETE)) ? null
				: (request instanceof String) ? (String) request : objectMapper.writeValueAsString(request);
		}
		catch (Exception exception)
		{
			throw new RemotingException(ExceptionType.LOCAL_APPLICATION,
				"Failed to serialize request: " + exception.getMessage(), exception);
		}

				if (payload != null)
		{
			logger.debug(methodName, "request = ", payload);
		}

				transactionContext = TransactionContext.get();

				headers = new HttpHeaders();
		headers.setContentType(mediaType);
		headers.setAccept(Collections.singletonList(mediaType));
		headers.set("X-Origin-Id", transactionContext.getOriginId());
		headers.set("X-Request-Id", transactionContext.getTransactionId());

				if (basicAuth == true)
		{
						headers.setBasicAuth(getUsername(), getPassword());
		}

				requestEntity = new HttpEntity<>(payload, headers);

				timer = Timer.ofNanos().start();

		try
		{

						if (uriParameters != null)
			{
								responseEntity = restTemplate.exchange(getUrl(), method, requestEntity, String.class, uriParameters);
			}
			else
			{
								responseEntity = restTemplate.exchange(getUrl(), method, requestEntity, String.class);
			}

						duration = timer.elapsedTime(TimeUnit.MILLISECONDS);

						logger.debug(methodName, "Request sent successfully.  HTTP status = ", responseEntity.getStatusCodeValue(), " ", responseEntity.getStatusCode().getReasonPhrase(), ".  Duration = ", duration, " ms.");

						payload = responseEntity.getBody();

						if (payload != null)
			{
				logger.debug(methodName, "response = ", payload);
			}

		}
		catch (RestClientResponseException exception)
		{
						duration = timer.elapsedTime(TimeUnit.MILLISECONDS);

						logger.debug(methodName, "Request send failed.  HTTP status = ", exception.getRawStatusCode(), " ", exception.getStatusText(), ".  Duration = ", duration, " ms.");

						payload = exception.getResponseBodyAsString();

						if (payload != null)
			{
				logger.debug(methodName, "response = ", payload);
			}

						handleException(getExceptionMapper(exceptionMapper, payload), exception);
		}
		catch (Exception exception)
		{
			throw new RemotingException(ExceptionType.REMOTING_COMMUNICATION,
				"Failed to send request: " + exception.getMessage(), exception);
		}

		try
		{
						response = (payload == null) ? null : (responseType == String.class) ? responseType.cast(payload) : objectMapper.readValue(payload, responseType);
		}
		catch (Exception exception)
		{
			throw new RemotingException(ExceptionType.LOCAL_APPLICATION,
				"Failed to deserialize response: " + exception.getMessage(), exception);
		}

				if (response instanceof ResponseExceptionMapper)
		{
						handleException((ResponseExceptionMapper) response, null);
		}

				if (response instanceof RawAwarePayload)
		{
						((RawAwarePayload) response).setRaw(payload);
		}

		return response;
	}

	/**
	 * Generate exception mapper from response payload.
	 * @param exceptionMapper The exception mapper class
	 * @param payload The response payload
	 * @return The exception mapper
	 * @throws RemotingException when unable to parse the response payload
	 */
	private <Em extends ResponseExceptionMapper> Em getExceptionMapper(
		final Class<Em> exceptionMapper,
		final String payload) throws RemotingException
	{
				Em exceptionMapper1;

		try
		{
						exceptionMapper1 = objectMapper.readValue(payload, exceptionMapper);
		}
		catch (Exception exception)
		{
			throw new RemotingException(ExceptionType.LOCAL_APPLICATION,
				"Failed to deserialize response: " + exception.getMessage(), exception);
		}

		return exceptionMapper1;
	}

	/**
	 * Generate and raise exception.
	 * @param exceptionMapper The exception mapper
	 * @param exception The client response exception
	 * @throws RemotingException
	 */
	private void handleException(
		final ResponseExceptionMapper exceptionMapper,
		final RestClientResponseException exception) throws RemotingException
	{
				ExceptionType exceptionType;
		String exceptionCode;
		String exceptionMessage;

				exceptionType = exceptionMapper.getExceptionType();
		exceptionCode = exceptionMapper.getExceptionCode();
		exceptionMessage = exceptionMapper.getExceptionMessage();

				if (exceptionMessage != null)
		{
						throw new RemotingException((exceptionType == null) ? ExceptionType.REMOTING_APPLICATION : exceptionType,
				(exceptionCode == null) ? "" : exceptionCode, exceptionMessage);
		}
				else if (exception != null)
		{
						throw new RemotingException(RestResponseExceptionMapper.getExceptionType(exception.getRawStatusCode()),
				String.valueOf(exception.getRawStatusCode()), exception.getStatusText());
		}

	}

}
