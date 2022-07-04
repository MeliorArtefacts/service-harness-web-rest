/* __  __    _ _      
  |  \/  |  | (_)       
  | \  / | ___| |_  ___  _ __ 
  | |\/| |/ _ \ | |/ _ \| '__|
  | |  | |  __/ | | (_) | |   
  |_|  |_|\___|_|_|\___/|_|   
    Service Harness
*/
package org.melior.service.web.rest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.melior.context.service.ServiceContext;
import org.melior.context.transaction.TransactionContext;
import org.melior.logging.core.Logger;
import org.melior.logging.core.LoggerFactory;
import org.melior.service.core.WorkManager;
import org.melior.service.exception.ApplicationException;
import org.melior.service.exception.ExceptionType;
import org.melior.util.time.AccurateLocalDateTime;
import org.melior.util.time.DateFormatter;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * TODO
 * @author Melior
 * @since 2.0
 */
@ControllerAdvice
public class RestRequestInterceptor implements HandlerInterceptor, ResponseBodyAdvice<Object>{
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    protected Logger logger;

    private WorkManager workManager;

  /**
   * Constructor.
   * @param serviceContext The service context
   */
  public RestRequestInterceptor(
    final ServiceContext serviceContext){
        super();

        logger = LoggerFactory.getLogger(this.getClass());

        this.workManager = serviceContext.getWorkManager();
  }

  /**
   * Process inbound REST request.
   * @param request The request
   * @param response The response
   * @param handler The handler
   * @throws Exception when unable to process the request
   */
  public boolean preHandle(
    final HttpServletRequest request,
    final HttpServletResponse response,
    final Object handler) throws Exception{
        String operation;

        operation = getOperation(request);

        startRequest(request, operation);

    return true;
  }

  /**
   * Process outbound REST response/exception.
   * @param request The request
   * @param response The response
   * @param handler The handler
   * @param modelAndView The model and view
   * @throws Exception when unable to process the response/exception
   */
  public void postHandle(
    final HttpServletRequest request,
    final HttpServletResponse response,
    final Object handler,
    final ModelAndView modelAndView) throws Exception{
        HttpStatus httpStatus;

    try{
            httpStatus = HttpStatus.valueOf(response.getStatus());
    }
    catch (Exception exception){
            httpStatus = null;
    }

        if ((httpStatus != null) && (httpStatus.isError() == false)){
            completeRequest(response, false);
    }

  }

  /**
   * Process outbound REST response/exception after it has completed.
   * @param request The request
   * @param response The response
   * @param handler The handler
   * @param exception The exception
   * @throws Exception when unable to process the response/exception
   */
  public void afterCompletion(
    final HttpServletRequest request,
    final HttpServletResponse response,
    final Object handler,
    final Exception exception) throws Exception{
        HttpStatus httpStatus;

    try{
            httpStatus = HttpStatus.valueOf(response.getStatus());
    }
    catch (Exception exception2){
            httpStatus = null;
    }

        if ((httpStatus == null) || (httpStatus.isError() == true)){
            completeRequest(response, true);
    }

  }

  /**
   * Support outbound REST response/exception.
   * @param returnType The return type
   * @param converterType The converter type
   * @return true for all responses/exceptions
   */
  public boolean supports(
    final MethodParameter returnType,
    final Class<? extends HttpMessageConverter<?>> converterType){
    return true;
  }

  /**
   * Process outbound REST response/exception before it is sent.
   * @param body The response/exception body
   * @param returnType The return type
   * @param selectedContentType The content type
   * @param selectedConverterType The converter type
   * @param request The request
   * @param response The response
   * @return The response/exception body
   */
  public Object beforeBodyWrite(
    final Object body,
    final MethodParameter returnType,
    final MediaType selectedContentType,
    final Class<? extends HttpMessageConverter<?>> selectedConverterType,
    final ServerHttpRequest request,
    final ServerHttpResponse response){
        modifyResponse(response);

    return body;
  }

  /**
   * Start processing request.
   * @param request The request
   * @param operation The operation
   * @throws Exception when unable to start processing the request
   */
  public final void startRequest(
    final HttpServletRequest request,
    final String operation) throws Exception{
        String methodName = "startRequest";
    TransactionContext transactionContext;

        transactionContext = TransactionContext.get();

        if ("/error".equals(operation) == true){
            transactionContext.setOperation(operation);

      return;
    }

    try{
            transactionContext.startTransaction();
      transactionContext.setOriginId(request.getHeader("X-Origin-Id"));
      transactionContext.setTransactionId(getTransactionId(request.getHeader("X-Request-Id")));
      transactionContext.setOperation(operation);
    }
    catch (Exception exception){
      logger.error(methodName, "Failed to get tracking data from request: ", exception.getMessage(), exception);
    }

    try{
            workManager.startRequest(transactionContext);
    }
    catch (ApplicationException exception){
      logger.error(methodName, "Failed to notify work manager that request has started: ", exception.getMessage(), exception);

      throw new RestInterfaceException(exception.getType(), exception.getCode(), exception.getMessage());
    }
    catch (Exception exception){
      logger.error(methodName, "Failed to notify work manager that request has started: ", exception.getMessage(), exception);

      throw new RestInterfaceException(ExceptionType.UNEXPECTED, "", exception.getMessage());
    }

  }

  /**
   * Modify response.
   * @param response The response
   */
  private final void modifyResponse(
    final ServerHttpResponse response){
        String methodName = "modifyResponse";
    TransactionContext transactionContext;
    String requestTimestamp;
    String responseTimestamp;
    HttpHeaders headers;

        transactionContext = TransactionContext.get();

    try{
            requestTimestamp = DateFormatter.formatTimestamp(LocalDateTime.ofInstant(
        Instant.ofEpochMilli(transactionContext.getStartTimeMillis()),
        TimeZone.getDefault().toZoneId()), TIMESTAMP_FORMAT);

            responseTimestamp = DateFormatter.formatTimestamp(AccurateLocalDateTime.now(), TIMESTAMP_FORMAT);

            headers = response.getHeaders();
      headers.add("X-Request-Id", transactionContext.getTransactionId());
      headers.add("Request-Timestamp", requestTimestamp);
      headers.add("Response-Timestamp", responseTimestamp);
    }
    catch (Exception exception){
      logger.error(methodName, "Failed to set tracking data on response: ", exception.getMessage(), exception);
    }

  }

  /**
   * Complete processing request. 
   * @param response The response
   * @param isException true if the response is an exception, false otherwise
   */
  public final void completeRequest(
    final HttpServletResponse response,
    final boolean isException){
        String methodName = "completeRequest";
    TransactionContext transactionContext;
    String requestTimestamp;
    String responseTimestamp;

        transactionContext = TransactionContext.get();

        if ("/error".equals(transactionContext.getOperation()) == true){
      return;
    }

    try{
            workManager.completeRequest(transactionContext, isException);
    }
    catch (Exception exception){
      logger.error(methodName, "Failed to notify work manager that request has completed: ", exception.getMessage(), exception);
    }

    try{

            if (response.isCommitted() == false){
                requestTimestamp = DateFormatter.formatTimestamp(LocalDateTime.ofInstant(
          Instant.ofEpochMilli(transactionContext.getStartTimeMillis()),
          TimeZone.getDefault().toZoneId()), TIMESTAMP_FORMAT);

                responseTimestamp = DateFormatter.formatTimestamp(AccurateLocalDateTime.now(), TIMESTAMP_FORMAT);

                response.setHeader("X-Request-Id", transactionContext.getTransactionId());
        response.setHeader("Request-Timestamp", requestTimestamp);
        response.setHeader("Response-Timestamp", responseTimestamp);
      }

            transactionContext.reset();
    }
    catch (Exception exception){
      logger.error(methodName, "Failed to set tracking data on response: ", exception.getMessage(), exception);
    }

  }

  /**
   * Get operation.
   * @param request The request
   * @return The operation
   */
  private String getOperation(
    final HttpServletRequest request){
        String operation;

    try{
            operation = request.getMethod() + " " + request.getRequestURI();
    }
    catch (Exception exception){
            operation = "Unknown";
    }

    return operation;
  }

  /**
   * Get transaction identifier.  Generates a UUID if the transaction identifier is undefined.
   * @param transactionId The provided transaction identifier
   * @return The resultant transaction identifier
   */
  private String getTransactionId(
    final String transactionId){
        return (transactionId == null) ? UUID.randomUUID().toString() : transactionId;
  }

}
