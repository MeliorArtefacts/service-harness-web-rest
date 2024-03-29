/* __  __      _ _            
  |  \/  |    | (_)           
  | \  / | ___| |_  ___  _ __ 
  | |\/| |/ _ \ | |/ _ \| '__|
  | |  | |  __/ | | (_) | |   
  |_|  |_|\___|_|_|\___/|_|   
        Service Harness
*/
package org.melior.client.web.rest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.melior.client.core.RawAwarePayload;
import org.melior.client.exception.RemotingException;
import org.melior.client.exception.ResponseExceptionMapper;
import org.melior.client.http.HttpHeader;
import org.melior.client.web.rest.patch.MappingJackson2HttpMessageConverter;
import org.melior.client.web.rest.patch.MappingJackson2XmlHttpMessageConverter;
import org.melior.client.web.rest.patch.StringHttpMessageConverter;
import org.melior.context.transaction.TransactionContext;
import org.melior.logging.core.Logger;
import org.melior.logging.core.LoggerFactory;
import org.melior.service.exception.ExceptionType;
import org.melior.util.exception.ExceptionUtil;
import org.melior.util.object.ObjectUtil;
import org.melior.util.time.Timer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Implements an easy to use, auto-configuring HTTP REST client with connection
 * pooling, automatic object mapping and fast secure HTTP connections.
 * <p>
 * The client writes timing details to the logs while dispatching HTTP requests
 * to the HTTP end-point.  The client automatically converts any exception that
 * occurs during communication with the HTTP end-point into a standard
 * {@code RemotingException}.
 * @author Melior
 * @since 2.0
 */
public class RestClient extends RestClientConfig {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private MediaType mediaType;

    private boolean basicAuth;

    private boolean bearerAuth;

    private boolean apiKeyAuth;

    private boolean proxyAuth;

    private boolean ssl;

    private SSLContext sslContext;

    private ObjectMapper objectMapper;

    private RestTemplate restTemplate;

    /**
     * Constructor.
     * @param mediaType The media type
     * @param basicAuth The basic authentication indicator
     * @param bearerAuth The bearer authentication indicator
     * @param apiKeyAuth The API key authentication indicator
     * @param ssl The SSL indicator
     * @param sslContext The SSL context
     */
    RestClient(
        final MediaType mediaType,
        final boolean basicAuth,
        final boolean bearerAuth,
        final boolean apiKeyAuth,
        final boolean ssl,
        final SSLContext sslContext) {

        super();

        this.mediaType = mediaType;

        this.basicAuth = basicAuth;

        this.bearerAuth = bearerAuth;

        this.apiKeyAuth = apiKeyAuth;

        this.ssl = ssl;

        this.sslContext = sslContext;
    }

    /**
     * Configure client.
     * @param clientConfig The new client configuration parameters
     * @return The REST client
     */
    public RestClient configure(
        final RestClientConfig clientConfig) {
        super.configure(clientConfig);

        return this;
    }

    /**
     * Initialize client.
     * @throws RemotingException if unable to initialize the client
     */
    private void initialize() throws RemotingException {

        ConnectionManager connectionManager;
        RequestConfig requestConfig;
        HttpClientBuilder httpClientBuilder;
        HttpComponentsClientHttpRequestFactory requestFactory;

        if (restTemplate != null) {
            return;
        }

        synchronized (this) {

            if (restTemplate != null) {
                return;
            }

            if (StringUtils.hasLength(getUrl()) == false) {
                throw new RemotingException(ExceptionType.LOCAL_APPLICATION, "URL must be configured.");
            }

            if (basicAuth == true) {

                if (StringUtils.hasLength(getUsername()) == false) {
                    throw new RemotingException(ExceptionType.LOCAL_APPLICATION, "User name must be configured.");
                }

                if (StringUtils.hasLength(getPassword()) == false) {
                    throw new RemotingException(ExceptionType.LOCAL_APPLICATION, "Password must be configured.");
                }

            }

            if (bearerAuth == true) {

                if (StringUtils.hasLength(getToken()) == false) {
                    throw new RemotingException(ExceptionType.LOCAL_APPLICATION, "Token must be configured.");
                }

            }

            if (apiKeyAuth == true) {

                if (StringUtils.hasLength(getToken()) == false) {
                    throw new RemotingException(ExceptionType.LOCAL_APPLICATION, "Token must be configured.");
                }

            }

            if ((mediaType == MediaType.APPLICATION_XML)
                || (mediaType == MediaType.TEXT_XML)) {

                objectMapper = RestObjectMapper.ofXML(false, false, true);
            }
            else {

                objectMapper = RestObjectMapper.ofJSON(false);
            }

            connectionManager = new ConnectionManager(this, ssl, sslContext);
            connectionManager.setMaxPerRoute(new HttpRoute(HttpHostUtil.urlToHost(getUrl())), getMaximumConnections());
            connectionManager.setValidateAfterInactivity(getInactivityTimeout());

            requestConfig =  RequestConfig.custom()
                .setConnectionRequestTimeout(getConnectionTimeout())
                .setConnectTimeout(getConnectionTimeout())
                .setSocketTimeout(getRequestTimeout())
                .build();

            httpClientBuilder = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setConnectionManagerShared(false)
                .setKeepAliveStrategy(new ConnectionKeepAliveStrategy(getInactivityTimeout()))
                .setDefaultRequestConfig(requestConfig);

            if (ssl == true) {

                httpClientBuilder
                    .setConnectionReuseStrategy(new DefaultConnectionReuseStrategy())
                    .setUserTokenHandler(new UserTokenHandler(){public Object getUserToken(HttpContext context) {return null;}});
            }

            if (StringUtils.hasLength(getProxyUrl()) == true) {

                httpClientBuilder
                    .setProxy(HttpHostUtil.urlToHost(getProxyUrl()));
            }

            if ((StringUtils.hasLength(getProxyUsername()) == true)
                && (StringUtils.hasLength(getProxyPassword()) == true)) {

                proxyAuth = true;
            }

            requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClientBuilder.build());

            restTemplate = new RestTemplate();
            restTemplate.setRequestFactory(requestFactory);
            restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter());
            restTemplate.getMessageConverters().add(1, new MappingJackson2XmlHttpMessageConverter());
            restTemplate.getMessageConverters().add(2, new MappingJackson2HttpMessageConverter());
        }

    }

    /**
     * Send GET request and receive response.
     * @param <Rs> The response type
     * @param <Em> The exception mapper type
     * @param uriParameters The URI parameters
     * @param responseType The response object type
     * @param exceptionMapper The response exception mapper
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rs, Em extends ResponseExceptionMapper> Rs get(
        final Object[] uriParameters,
        final Class<Rs> responseType,
        final Class<Em> exceptionMapper) throws RemotingException {

        return exchange(HttpMethod.GET, "", uriParameters, null, null, responseType, exceptionMapper);
    }

    /**
     * Send GET request and receive response.
     * @param <Rs> The response type
     * @param <Em> The exception mapper type
     * @param uriPath The URI path
     * @param uriParameters The URI parameters
     * @param responseType The response object type
     * @param exceptionMapper The response exception mapper
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rs, Em extends ResponseExceptionMapper> Rs get(
        final String uriPath,
        final Object[] uriParameters,
        final Class<Rs> responseType,
        final Class<Em> exceptionMapper) throws RemotingException {

        return exchange(HttpMethod.GET, uriPath, uriParameters, null, null, responseType, exceptionMapper);
    }

    /**
     * Send GET request and receive response.
     * @param <Rs> The response type
     * @param <Em> The exception mapper type
     * @param uriPath The URI path
     * @param uriParameters The URI parameters
     * @param httpHeaders The HTTP headers
     * @param responseType The response object type
     * @param exceptionMapper The response exception mapper
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rs, Em extends ResponseExceptionMapper> Rs get(
        final String uriPath,
        final Object[] uriParameters,
        final HttpHeader[] httpHeaders,
        final Class<Rs> responseType,
        final Class<Em> exceptionMapper) throws RemotingException {

        return exchange(HttpMethod.GET, uriPath, uriParameters, httpHeaders, null, responseType, exceptionMapper);
    }

    /**
     * Send GET request and receive response.
     * @param <Rs> The response type
     * @param <Em> The exception mapper type
     * @param responseType The response object type
     * @param exceptionMapper The response exception mapper
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rs, Em extends ResponseExceptionMapper> Rs get(
        final Class<Rs> responseType,
        final Class<Em> exceptionMapper) throws RemotingException {

        return exchange(HttpMethod.GET, "", null, null, null, responseType, exceptionMapper);
    }

    /**
     * Send GET request and receive response.
     * @param <Rs> The response type
     * @param <Em> The exception mapper type
     * @param uriPath The URI path
     * @param responseType The response object type
     * @param exceptionMapper The response exception mapper
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rs, Em extends ResponseExceptionMapper> Rs get(
        final String uriPath,
        final Class<Rs> responseType,
        final Class<Em> exceptionMapper) throws RemotingException {

        return exchange(HttpMethod.GET, uriPath, null, null, null, responseType, exceptionMapper);
    }

    /**
     * Send GET request and receive response.
     * @param <Rs> The response type
     * @param <Em> The exception mapper type
     * @param uriPath The URI path
     * @param httpHeaders The HTTP headers
     * @param responseType The response object type
     * @param exceptionMapper The response exception mapper
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rs, Em extends ResponseExceptionMapper> Rs get(
        final String uriPath,
        final HttpHeader[] httpHeaders,
        final Class<Rs> responseType,
        final Class<Em> exceptionMapper) throws RemotingException {

        return exchange(HttpMethod.GET, uriPath, null, httpHeaders, null, responseType, exceptionMapper);
    }

    /**
     * Send GET request and receive response.
     * @param <Rs> The response type
     * @param uriParameters The URI parameters
     * @param responseType The response object type
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rs> Rs get(
        final Object[] uriParameters,
        final Class<Rs> responseType) throws RemotingException {

        return exchange(HttpMethod.GET, "", uriParameters, null, null, responseType, RestResponseExceptionMapper.class);
    }

    /**
     * Send GET request and receive response.
     * @param <Rs> The response type
     * @param uriPath The URI path
     * @param uriParameters The URI parameters
     * @param responseType The response object type
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rs> Rs get(
        final String uriPath,
        final Object[] uriParameters,
        final Class<Rs> responseType) throws RemotingException {

        return exchange(HttpMethod.GET, uriPath, uriParameters, null, null, responseType, RestResponseExceptionMapper.class);
    }

    /**
     * Send GET request and receive response.
     * @param <Rs> The response type
     * @param uriPath The URI path
     * @param uriParameters The URI parameters
     * @param httpHeaders The HTTP headers
     * @param responseType The response object type
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rs> Rs get(
        final String uriPath,
        final Object[] uriParameters,
        final HttpHeader[] httpHeaders,
        final Class<Rs> responseType) throws RemotingException {

        return exchange(HttpMethod.GET, uriPath, uriParameters, httpHeaders, null, responseType, RestResponseExceptionMapper.class);
    }

    /**
     * Send GET request and receive response.
     * @param <Rs> The response type
     * @param responseType The response object type
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rs> Rs get(
        final Class<Rs> responseType) throws RemotingException {

        return exchange(HttpMethod.GET, "", null, null, null, responseType, RestResponseExceptionMapper.class);
    }

    /**
     * Send GET request and receive response.
     * @param <Rs> The response type
     * @param uriPath The URI path
     * @param responseType The response object type
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rs> Rs get(
        final String uriPath,
        final Class<Rs> responseType) throws RemotingException {

        return exchange(HttpMethod.GET, uriPath, null, null, null, responseType, RestResponseExceptionMapper.class);
    }

    /**
     * Send GET request and receive response.
     * @param <Rs> The response type
     * @param uriPath The URI path
     * @param httpHeaders The HTTP headers
     * @param responseType The response object type
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rs> Rs get(
        final String uriPath,
        final HttpHeader[] httpHeaders,
        final Class<Rs> responseType) throws RemotingException {

        return exchange(HttpMethod.GET, uriPath, null, httpHeaders, null, responseType, RestResponseExceptionMapper.class);
    }

    /**
     * Send POST request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param <Em> The exception mapper type
     * @param uriParameters The URI parameters
     * @param request The request object
     * @param responseType The response object type
     * @param exceptionMapper The response exception mapper
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs, Em extends ResponseExceptionMapper> Rs post(
        final Object[] uriParameters,
        final Rq request,
        final Class<Rs> responseType,
        final Class<Em> exceptionMapper) throws RemotingException {

        return exchange(HttpMethod.POST, "", uriParameters, null, request, responseType, exceptionMapper);
    }

    /**
     * Send POST request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param <Em> The exception mapper type
     * @param uriPath The URI path
     * @param uriParameters The URI parameters
     * @param request The request object
     * @param responseType The response object type
     * @param exceptionMapper The response exception mapper
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs, Em extends ResponseExceptionMapper> Rs post(
        final String uriPath,
        final Object[] uriParameters,
        final Rq request,
        final Class<Rs> responseType,
        final Class<Em> exceptionMapper) throws RemotingException {

        return exchange(HttpMethod.POST, uriPath, uriParameters, null, request, responseType, exceptionMapper);
    }

    /**
     * Send POST request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param <Em> The exception mapper type
     * @param uriPath The URI path
     * @param uriParameters The URI parameters
     * @param httpHeaders The HTTP headers
     * @param request The request object
     * @param responseType The response object type
     * @param exceptionMapper The response exception mapper
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs, Em extends ResponseExceptionMapper> Rs post(
        final String uriPath,
        final Object[] uriParameters,
        final HttpHeader[] httpHeaders,
        final Rq request,
        final Class<Rs> responseType,
        final Class<Em> exceptionMapper) throws RemotingException {

        return exchange(HttpMethod.POST, uriPath, uriParameters, httpHeaders, request, responseType, exceptionMapper);
    }

    /**
     * Send POST request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param <Em> The exception mapper type
     * @param request The request object
     * @param responseType The response object type
     * @param exceptionMapper The response exception mapper
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs, Em extends ResponseExceptionMapper> Rs post(
        final Rq request,
        final Class<Rs> responseType,
        final Class<Em> exceptionMapper) throws RemotingException {

        return exchange(HttpMethod.POST, "", null, null, request, responseType, exceptionMapper);
    }

    /**
     * Send POST request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param <Em> The exception mapper type
     * @param uriPath The URI path
     * @param request The request object
     * @param responseType The response object type
     * @param exceptionMapper The response exception mapper
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs, Em extends ResponseExceptionMapper> Rs post(
        final String uriPath,
        final Rq request,
        final Class<Rs> responseType,
        final Class<Em> exceptionMapper) throws RemotingException {

        return exchange(HttpMethod.POST, uriPath, null, null, request, responseType, exceptionMapper);
    }

    /**
     * Send POST request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param <Em> The exception mapper type
     * @param uriPath The URI path
     * @param httpHeaders The HTTP headers
     * @param request The request object
     * @param responseType The response object type
     * @param exceptionMapper The response exception mapper
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs, Em extends ResponseExceptionMapper> Rs post(
        final String uriPath,
        final HttpHeader[] httpHeaders,
        final Rq request,
        final Class<Rs> responseType,
        final Class<Em> exceptionMapper) throws RemotingException {

        return exchange(HttpMethod.POST, uriPath, null, httpHeaders, request, responseType, exceptionMapper);
    }

    /**
     * Send POST request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param uriParameters The URI parameters
     * @param request The request object
     * @param responseType The response object type
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs> Rs post(
        final Object[] uriParameters,
        final Rq request,
        final Class<Rs> responseType) throws RemotingException {

        return exchange(HttpMethod.POST, "", uriParameters, null, request, responseType, RestResponseExceptionMapper.class);
    }

    /**
     * Send POST request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param uriPath The URI path
     * @param uriParameters The URI parameters
     * @param request The request object
     * @param responseType The response object type
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs> Rs post(
        final String uriPath,
        final Object[] uriParameters,
        final Rq request,
        final Class<Rs> responseType) throws RemotingException {

        return exchange(HttpMethod.POST, uriPath, uriParameters, null, request, responseType, RestResponseExceptionMapper.class);
    }

    /**
     * Send POST request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param uriPath The URI path
     * @param uriParameters The URI parameters
     * @param httpHeaders The HTTP headers
     * @param request The request object
     * @param responseType The response object type
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs> Rs post(
        final String uriPath,
        final Object[] uriParameters,
        final HttpHeader[] httpHeaders,
        final Rq request,
        final Class<Rs> responseType) throws RemotingException {

        return exchange(HttpMethod.POST, uriPath, uriParameters, httpHeaders, request, responseType, RestResponseExceptionMapper.class);
    }

    /**
     * Send POST request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param request The request object
     * @param responseType The response object type
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs> Rs post(
        final Rq request,
        final Class<Rs> responseType) throws RemotingException {

        return exchange(HttpMethod.POST, "", null, null, request, responseType, RestResponseExceptionMapper.class);
    }

    /**
     * Send POST request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param uriPath The URI path
     * @param request The request object
     * @param responseType The response object type
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs> Rs post(
        final String uriPath,
        final Rq request,
        final Class<Rs> responseType) throws RemotingException {

        return exchange(HttpMethod.POST, uriPath, null, null, request, responseType, RestResponseExceptionMapper.class);
    }

    /**
     * Send POST request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param uriPath The URI path
     * @param httpHeaders The HTTP headers
     * @param request The request object
     * @param responseType The response object type
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs> Rs post(
        final String uriPath,
        final HttpHeader[] httpHeaders,
        final Rq request,
        final Class<Rs> responseType) throws RemotingException {

        return exchange(HttpMethod.POST, uriPath, null, httpHeaders, request, responseType, RestResponseExceptionMapper.class);
    }

    /**
     * Send PUT request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param <Em> The exception mapper type
     * @param uriParameters The URI parameters
     * @param request The request object
     * @param responseType The response object type
     * @param exceptionMapper The response exception mapper
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs, Em extends ResponseExceptionMapper> Rs put(
        final Object[] uriParameters,
        final Rq request,
        final Class<Rs> responseType,
        final Class<Em> exceptionMapper) throws RemotingException {

        return exchange(HttpMethod.PUT, "", uriParameters, null, request, responseType, exceptionMapper);
    }

    /**
     * Send PUT request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param <Em> The exception mapper type
     * @param uriPath The URI path
     * @param uriParameters The URI parameters
     * @param request The request object
     * @param responseType The response object type
     * @param exceptionMapper The response exception mapper
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs, Em extends ResponseExceptionMapper> Rs put(
        final String uriPath,
        final Object[] uriParameters,
        final Rq request,
        final Class<Rs> responseType,
        final Class<Em> exceptionMapper) throws RemotingException {

        return exchange(HttpMethod.PUT, uriPath, uriParameters, null, request, responseType, exceptionMapper);
    }

    /**
     * Send PUT request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param <Em> The exception mapper type
     * @param uriPath The URI path
     * @param uriParameters The URI parameters
     * @param httpHeaders The HTTP headers
     * @param request The request object
     * @param responseType The response object type
     * @param exceptionMapper The response exception mapper
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs, Em extends ResponseExceptionMapper> Rs put(
        final String uriPath,
        final Object[] uriParameters,
        final HttpHeader[] httpHeaders,
        final Rq request,
        final Class<Rs> responseType,
        final Class<Em> exceptionMapper) throws RemotingException {

        return exchange(HttpMethod.PUT, uriPath, uriParameters, httpHeaders, request, responseType, exceptionMapper);
    }

    /**
     * Send PUT request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param <Em> The exception mapper type
     * @param request The request object
     * @param responseType The response object type
     * @param exceptionMapper The response exception mapper
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs, Em extends ResponseExceptionMapper> Rs put(
        final Rq request,
        final Class<Rs> responseType,
        final Class<Em> exceptionMapper) throws RemotingException {

        return exchange(HttpMethod.PUT, "", null, null, request, responseType, exceptionMapper);
    }

    /**
     * Send PUT request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param <Em> The exception mapper type
     * @param uriPath The URI path
     * @param request The request object
     * @param responseType The response object type
     * @param exceptionMapper The response exception mapper
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs, Em extends ResponseExceptionMapper> Rs put(
        final String uriPath,
        final Rq request,
        final Class<Rs> responseType,
        final Class<Em> exceptionMapper) throws RemotingException {

        return exchange(HttpMethod.PUT, uriPath, null, null, request, responseType, exceptionMapper);
    }

    /**
     * Send PUT request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param <Em> The exception mapper type
     * @param uriPath The URI path
     * @param httpHeaders The HTTP headers
     * @param request The request object
     * @param responseType The response object type
     * @param exceptionMapper The response exception mapper
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs, Em extends ResponseExceptionMapper> Rs put(
        final String uriPath,
        final HttpHeader[] httpHeaders,
        final Rq request,
        final Class<Rs> responseType,
        final Class<Em> exceptionMapper) throws RemotingException {

        return exchange(HttpMethod.PUT, uriPath, null, httpHeaders, request, responseType, exceptionMapper);
    }

    /**
     * Send PUT request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param uriParameters The URI parameters
     * @param request The request object
     * @param responseType The response object type
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs> Rs put(
        final Object[] uriParameters,
        final Rq request,
        final Class<Rs> responseType) throws RemotingException {

        return exchange(HttpMethod.PUT, "", uriParameters, null, request, responseType, RestResponseExceptionMapper.class);
    }

    /**
     * Send PUT request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param uriPath The URI path
     * @param uriParameters The URI parameters
     * @param request The request object
     * @param responseType The response object type
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs> Rs put(
        final String uriPath,
        final Object[] uriParameters,
        final Rq request,
        final Class<Rs> responseType) throws RemotingException {

        return exchange(HttpMethod.PUT, uriPath, uriParameters, null, request, responseType, RestResponseExceptionMapper.class);
    }

    /**
     * Send PUT request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param uriPath The URI path
     * @param uriParameters The URI parameters
     * @param httpHeaders The HTTP headers
     * @param request The request object
     * @param responseType The response object type
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs> Rs put(
        final String uriPath,
        final Object[] uriParameters,
        final HttpHeader[] httpHeaders,
        final Rq request,
        final Class<Rs> responseType) throws RemotingException {

        return exchange(HttpMethod.PUT, uriPath, uriParameters, httpHeaders, request, responseType, RestResponseExceptionMapper.class);
    }

    /**
     * Send PUT request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param request The request object
     * @param responseType The response object type
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs> Rs put(
        final Rq request,
        final Class<Rs> responseType) throws RemotingException {

        return exchange(HttpMethod.PUT, "", null, null, request, responseType, RestResponseExceptionMapper.class);
    }

    /**
     * Send PUT request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param uriPath The URI path
     * @param request The request object
     * @param responseType The response object type
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs> Rs put(
        final String uriPath,
        final Rq request,
        final Class<Rs> responseType) throws RemotingException {

        return exchange(HttpMethod.PUT, uriPath, null, null, request, responseType, RestResponseExceptionMapper.class);
    }

    /**
     * Send PUT request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param uriPath The URI path
     * @param httpHeaders The HTTP headers
     * @param request The request object
     * @param responseType The response object type
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs> Rs put(
        final String uriPath,
        final HttpHeader[] httpHeaders,
        final Rq request,
        final Class<Rs> responseType) throws RemotingException {

        return exchange(HttpMethod.PUT, uriPath, null, httpHeaders, request, responseType, RestResponseExceptionMapper.class);
    }

    /**
     * Send PATCH request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param <Em> The exception mapper type
     * @param uriParameters The URI parameters
     * @param request The request object
     * @param responseType The response object type
     * @param exceptionMapper The response exception mapper
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs, Em extends ResponseExceptionMapper> Rs patch(
        final Object[] uriParameters,
        final Rq request,
        final Class<Rs> responseType,
        final Class<Em> exceptionMapper) throws RemotingException {

        return exchange(HttpMethod.PATCH, "", uriParameters, null, request, responseType, exceptionMapper);
    }

    /**
     * Send PATCH request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param <Em> The exception mapper type
     * @param uriPath The URI path
     * @param uriParameters The URI parameters
     * @param request The request object
     * @param responseType The response object type
     * @param exceptionMapper The response exception mapper
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs, Em extends ResponseExceptionMapper> Rs patch(
        final String uriPath,
        final Object[] uriParameters,
        final Rq request,
        final Class<Rs> responseType,
        final Class<Em> exceptionMapper) throws RemotingException {

        return exchange(HttpMethod.PATCH, uriPath, uriParameters, null, request, responseType, exceptionMapper);
    }

    /**
     * Send PATCH request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param <Em> The exception mapper type
     * @param uriPath The URI path
     * @param uriParameters The URI parameters
     * @param httpHeaders The HTTP headers
     * @param request The request object
     * @param responseType The response object type
     * @param exceptionMapper The response exception mapper
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs, Em extends ResponseExceptionMapper> Rs patch(
        final String uriPath,
        final Object[] uriParameters,
        final HttpHeader[] httpHeaders,
        final Rq request,
        final Class<Rs> responseType,
        final Class<Em> exceptionMapper) throws RemotingException {

        return exchange(HttpMethod.PATCH, uriPath, uriParameters, httpHeaders, request, responseType, exceptionMapper);
    }

    /**
     * Send PATCH request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param <Em> The exception mapper type
     * @param request The request object
     * @param responseType The response object type
     * @param exceptionMapper The response exception mapper
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs, Em extends ResponseExceptionMapper> Rs patch(
        final Rq request,
        final Class<Rs> responseType,
        final Class<Em> exceptionMapper) throws RemotingException {

        return exchange(HttpMethod.PATCH, "", null, null, request, responseType, exceptionMapper);
    }

    /**
     * Send PATCH request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param <Em> The exception mapper type
     * @param uriPath The URI path
     * @param request The request object
     * @param responseType The response object type
     * @param exceptionMapper The response exception mapper
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs, Em extends ResponseExceptionMapper> Rs patch(
        final String uriPath,
        final Rq request,
        final Class<Rs> responseType,
        final Class<Em> exceptionMapper) throws RemotingException {

        return exchange(HttpMethod.PATCH, uriPath, null, null, request, responseType, exceptionMapper);
    }

    /**
     * Send PATCH request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param <Em> The exception mapper type
     * @param uriPath The URI path
     * @param httpHeaders The HTTP headers
     * @param request The request object
     * @param responseType The response object type
     * @param exceptionMapper The response exception mapper
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs, Em extends ResponseExceptionMapper> Rs patch(
        final String uriPath,
        final HttpHeader[] httpHeaders,
        final Rq request,
        final Class<Rs> responseType,
        final Class<Em> exceptionMapper) throws RemotingException {

        return exchange(HttpMethod.PATCH, uriPath, null, httpHeaders, request, responseType, exceptionMapper);
    }

    /**
     * Send PATCH request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param uriParameters The URI parameters
     * @param request The request object
     * @param responseType The response object type
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs> Rs patch(
        final Object[] uriParameters,
        final Rq request,
        final Class<Rs> responseType) throws RemotingException {

        return exchange(HttpMethod.PATCH, "", uriParameters, null, request, responseType, RestResponseExceptionMapper.class);
    }

    /**
     * Send PATCH request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param uriPath The URI path
     * @param uriParameters The URI parameters
     * @param request The request object
     * @param responseType The response object type
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs> Rs patch(
        final String uriPath,
        final Object[] uriParameters,
        final Rq request,
        final Class<Rs> responseType) throws RemotingException {

        return exchange(HttpMethod.PATCH, uriPath, uriParameters, null, request, responseType, RestResponseExceptionMapper.class);
    }

    /**
     * Send PATCH request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param uriPath The URI path
     * @param uriParameters The URI parameters
     * @param httpHeaders The HTTP headers
     * @param request The request object
     * @param responseType The response object type
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs> Rs patch(
        final String uriPath,
        final Object[] uriParameters,
        final HttpHeader[] httpHeaders,
        final Rq request,
        final Class<Rs> responseType) throws RemotingException {

        return exchange(HttpMethod.PATCH, uriPath, uriParameters, httpHeaders, request, responseType, RestResponseExceptionMapper.class);
    }

    /**
     * Send PATCH request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param request The request object
     * @param responseType The response object type
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs> Rs patch(
        final Rq request,
        final Class<Rs> responseType) throws RemotingException {

        return exchange(HttpMethod.PATCH, "", null, null, request, responseType, RestResponseExceptionMapper.class);
    }

    /**
     * Send PATCH request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param uriPath The URI path
     * @param request The request object
     * @param responseType The response object type
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs> Rs patch(
        final String uriPath,
        final Rq request,
        final Class<Rs> responseType) throws RemotingException {

        return exchange(HttpMethod.PATCH, uriPath, null, null, request, responseType, RestResponseExceptionMapper.class);
    }

    /**
     * Send PATCH request and receive response.
     * @param <Rq> The request type
     * @param <Rs> The response type
     * @param uriPath The URI path
     * @param httpHeaders The HTTP headers
     * @param request The request object
     * @param responseType The response object type
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Rq, Rs> Rs patch(
        final String uriPath,
        final HttpHeader[] httpHeaders,
        final Rq request,
        final Class<Rs> responseType) throws RemotingException {

        return exchange(HttpMethod.PATCH, uriPath, null, httpHeaders, request, responseType, RestResponseExceptionMapper.class);
    }

    /**
     * Send DELETE request.
     * @param <Em> The exception mapper type
     * @param uriParameters The URI parameters
     * @param exceptionMapper The response exception mapper
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Em extends ResponseExceptionMapper> void delete(
        final Object[] uriParameters,
        final Class<Em> exceptionMapper) throws RemotingException {

        exchange(HttpMethod.DELETE, "", uriParameters, null, null, null, exceptionMapper);
    }

    /**
     * Send DELETE request.
     * @param <Em> The exception mapper type
     * @param uriPath The URI path
     * @param uriParameters The URI parameters
     * @param exceptionMapper The response exception mapper
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Em extends ResponseExceptionMapper> void delete(
        final String uriPath,
        final Object[] uriParameters,
        final Class<Em> exceptionMapper) throws RemotingException {

        exchange(HttpMethod.DELETE, uriPath, uriParameters, null, null, null, exceptionMapper);
    }

    /**
     * Send DELETE request.
     * @param <Em> The exception mapper type
     * @param uriPath The URI path
     * @param uriParameters The URI parameters
     * @param httpHeaders The HTTP headers
     * @param exceptionMapper The response exception mapper
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Em extends ResponseExceptionMapper> void delete(
        final String uriPath,
        final Object[] uriParameters,
        final HttpHeader[] httpHeaders,
        final Class<Em> exceptionMapper) throws RemotingException {

        exchange(HttpMethod.DELETE, uriPath, uriParameters, httpHeaders, null, null, exceptionMapper);
    }

    /**
     * Send DELETE request.
     * @param <Em> The exception mapper type
     * @param exceptionMapper The response exception mapper
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Em extends ResponseExceptionMapper> void delete(
        final Class<Em> exceptionMapper) throws RemotingException {

        exchange(HttpMethod.DELETE, "", null, null, null, null, exceptionMapper);
    }

    /**
     * Send DELETE request.
     * @param <Em> The exception mapper type
     * @param uriPath The URI path
     * @param exceptionMapper The response exception mapper
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Em extends ResponseExceptionMapper> void delete(
        final String uriPath,
        final Class<Em> exceptionMapper) throws RemotingException {

        exchange(HttpMethod.DELETE, uriPath, null, null, null, null, exceptionMapper);
    }

    /**
     * Send DELETE request.
     * @param <Em> The exception mapper type
     * @param uriPath The URI path
     * @param httpHeaders The HTTP headers
     * @param exceptionMapper The response exception mapper
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public <Em extends ResponseExceptionMapper> void delete(
        final String uriPath,
        final HttpHeader[] httpHeaders,
        final Class<Em> exceptionMapper) throws RemotingException {

        exchange(HttpMethod.DELETE, uriPath, null, httpHeaders, null, null, exceptionMapper);
    }

    /**
     * Send DELETE request.
     * @param uriParameters The URI parameters
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public void delete(
        final Object[] uriParameters) throws RemotingException {

        exchange(HttpMethod.DELETE, "", uriParameters, null, null, null, RestResponseExceptionMapper.class);
    }

    /**
     * Send DELETE request.
     * @param uriPath The URI path
     * @param uriParameters The URI parameters
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public void delete(
        final String uriPath,
        final Object[] uriParameters) throws RemotingException {

        exchange(HttpMethod.DELETE, uriPath, uriParameters, null, null, null, RestResponseExceptionMapper.class);
    }

    /**
     * Send DELETE request.
     * @param uriPath The URI path
     * @param uriParameters The URI parameters
     * @param httpHeaders The HTTP headers
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public void delete(
        final String uriPath,
        final Object[] uriParameters,
        final HttpHeader[] httpHeaders) throws RemotingException {

        exchange(HttpMethod.DELETE, uriPath, uriParameters, httpHeaders, null, null, RestResponseExceptionMapper.class);
    }

    /**
     * Send DELETE request.
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public void delete() throws RemotingException {

        exchange(HttpMethod.DELETE, "", null, null, null, null, RestResponseExceptionMapper.class);
    }

    /**
     * Send DELETE request.
     * @param uriPath The URI path
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public void delete(
        final String uriPath) throws RemotingException {

        exchange(HttpMethod.DELETE, uriPath, null, null, null, null, RestResponseExceptionMapper.class);
    }

    /**
     * Send DELETE request.
     * @param uriPath The URI path
     * @param httpHeaders The HTTP headers
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    public void delete(
        final String uriPath,
        final HttpHeader[] httpHeaders) throws RemotingException {

        exchange(HttpMethod.DELETE, uriPath, null, httpHeaders, null, null, RestResponseExceptionMapper.class);
    }

    /**
     * Send request and receive response.
     * @param method The HTTP method
     * @param uriPath The URI path
     * @param uriParameters The URI parameters
     * @param httpHeaders The HTTP headers
     * @param request The request object
     * @param responseType The response object type
     * @param exceptionMapper The response exception mapper
     * @return The response object
     * @throws RemotingException if unable to send the request, or if an error response is received
     */
    private <Rq, Rs, Em extends ResponseExceptionMapper> Rs exchange(
        final HttpMethod method,
        final String uriPath,
        final Object[] uriParameters,
        final HttpHeader[] httpHeaders,
        final Rq request,
        final Class<Rs> responseType,
        final Class<Em> exceptionMapper) throws RemotingException {

        String methodName = method.name().toLowerCase();
        String payload;
        TransactionContext transactionContext;
        HttpHeaders httpHeaderMap;
        HttpEntity<String> requestEntity;
        Timer timer;
        ResponseEntity<String> responseEntity;
        long duration;
        Rs response;

        initialize();

        try {

            payload = ((method == HttpMethod.GET) || (method == HttpMethod.DELETE)) ? null
                : (request instanceof String) ? (String) request : objectMapper.writeValueAsString(request);
        }
        catch (Exception exception) {
            throw new RemotingException(ExceptionType.LOCAL_APPLICATION, "Failed to serialize request: " + exception.getMessage(), exception);
        }

        if (payload != null) {
            logger.debug(methodName, "request = ", payload);
        }

        transactionContext = TransactionContext.get();

        httpHeaderMap = new HttpHeaders();
        httpHeaderMap.setContentType((payload == null) ? null : mediaType);
        httpHeaderMap.setAccept(Collections.singletonList(mediaType));
        if (transactionContext.getOriginId() != null)
            httpHeaderMap.set("X-Origin-Id", transactionContext.getOriginId());
        httpHeaderMap.set("X-Request-Id", transactionContext.getTransactionId());
        if (transactionContext.getCorrelationId() != null)
            httpHeaderMap.set("X-Correlation-Id", transactionContext.getCorrelationId());

        if (httpHeaders != null) {

            for (HttpHeader restHeader : httpHeaders) {
                httpHeaderMap.set(restHeader.getName(), restHeader.getValue().toString());
            }

        }

        if (basicAuth == true) {

            httpHeaderMap.setBasicAuth(getUsername(), getPassword());
        }

        if (bearerAuth == true) {

            httpHeaderMap.setBearerAuth(getToken());
        }

        if (apiKeyAuth == true) {

            httpHeaderMap.set("X-API-Key", getToken());
        }

        if (proxyAuth == true) {

            httpHeaderMap.set(HttpHeaders.PROXY_AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString(
                (getProxyUsername() + ":" + getProxyPassword()).getBytes(StandardCharsets.UTF_8)));
        }

        requestEntity = new HttpEntity<>(payload, httpHeaderMap);

        timer = Timer.ofNanos().start();

        try {

            if (uriParameters != null) {

                responseEntity = restTemplate.exchange(getUrl() + uriPath, method, requestEntity, String.class, uriParameters);
            }
            else {

                responseEntity = restTemplate.exchange(getUrl() + uriPath, method, requestEntity, String.class);
            }

            duration = timer.elapsedTime(TimeUnit.MILLISECONDS);

            logger.debug(methodName, "Request sent successfully.  HTTP status = ", getStatusDescriptor(responseEntity.getStatusCodeValue(), responseEntity.getStatusCode().getReasonPhrase()), ".  Duration = ", duration, " ms.");

            payload = responseEntity.getBody();

            if (StringUtils.hasLength(payload) == true) {
                logger.debug(methodName, "response = ", payload);
            }

        }
        catch (RestClientResponseException exception) {

            duration = timer.elapsedTime(TimeUnit.MILLISECONDS);

            logger.debug(methodName, "Request send failed.  HTTP status = ", getStatusDescriptor(exception.getRawStatusCode(), exception.getStatusText()), ".  Duration = ", duration, " ms.");

            payload = exception.getResponseBodyAsString();

            if (StringUtils.hasLength(payload) == true) {
                logger.debug(methodName, "response = ", payload);
            }

            handleException(getExceptionMapper(exceptionMapper, payload), exception);
        }
        catch (Exception exception) {
            throw new RemotingException(ExceptionType.REMOTING_COMMUNICATION, "Failed to send request: " + exception.getMessage(), exception);
        }

        try {

            response = (payload == null) ? null : (responseType == String.class) ? responseType.cast(payload)
                : objectMapper.readValue(payload, responseType);
        }
        catch (Exception exception) {
            throw new RemotingException(ExceptionType.LOCAL_APPLICATION, "Failed to deserialize response: " + exception.getMessage(), exception);
        }

        if (response instanceof ResponseExceptionMapper) {

            handleException((ResponseExceptionMapper) response, null);
        }

        if (response instanceof RawAwarePayload) {

            ((RawAwarePayload) response).setRaw(payload);
        }

        return response;
    }

    /**
     * Generate exception mapper from response payload.
     * @param exceptionMapper The exception mapper class
     * @param payload The response payload
     * @return The exception mapper
     * @throws RemotingException if unable to parse the response payload
     */
    private <Em extends ResponseExceptionMapper> Em getExceptionMapper(
        final Class<Em> exceptionMapper,
        final String payload) throws RemotingException {

        return ExceptionUtil.swallow(() -> (StringUtils.hasLength(payload) == false) ? null : objectMapper.readValue(payload, exceptionMapper), null);
    }

    /**
     * Generate and raise exception.
     * @param exceptionMapper The exception mapper
     * @param exception The client response exception
     * @throws RemotingException
     */
    private void handleException(
        final ResponseExceptionMapper exceptionMapper,
        final RestClientResponseException exception) throws RemotingException {

        ExceptionType exceptionType;
        String exceptionCode;
        String exceptionMessage;

        exceptionType = (exceptionMapper == null) ? null : exceptionMapper.getExceptionType();
        exceptionCode = (exceptionMapper == null) ? null : exceptionMapper.getExceptionCode();
        exceptionMessage = (exceptionMapper == null) ? null : exceptionMapper.getExceptionMessage();

        if (exceptionMessage != null) {

            throw new RemotingException(ObjectUtil.coalesce(exceptionType, ExceptionType.REMOTING_APPLICATION),
                ObjectUtil.coalesce(exceptionCode, ""), exceptionMessage);
        }

        else if (exception != null) {

            throw new RemotingException(RestResponseExceptionMapper.getExceptionType(exception.getRawStatusCode()),
                String.valueOf(exception.getRawStatusCode()),
                getStatusDescriptor(exception.getRawStatusCode(), exception.getStatusText()));
        }

    }

    /**
     * Get HTTP status descriptor by combining status code and status text.
     * @param statusCode The HTTP status code
     * @param statusText The HTTP status text
     * @return The HTTP status descriptor
     */
    private String getStatusDescriptor(
        final int statusCode,
        final String statusText) {
        return StringUtils.trimWhitespace(statusCode + " " + statusText);
    }

}
