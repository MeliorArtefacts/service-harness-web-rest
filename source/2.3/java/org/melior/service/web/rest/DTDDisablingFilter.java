/* __  __      _ _            
  |  \/  |    | (_)           
  | \  / | ___| |_  ___  _ __ 
  | |\/| |/ _ \ | |/ _ \| '__|
  | |  | |  __/ | | (_) | |   
  |_|  |_|\___|_|_|\___/|_|   
        Service Harness
*/
package org.melior.service.web.rest;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Implementation of a request filter that eliminates XXE vulnerabilities in a synchronous
 * client request by stripping all DOCTYPE definitions from the client request before the
 * servlet container invokes the target resource.
 * @author Melior
 * @since 2.3
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(name = "server.request.disable-dtd", havingValue = "true")
public class DTDDisablingFilter extends OncePerRequestFilter {

    /**
     * This method is called only once for each request that is passed through the filter chain
     * due to a client request. The {@code FilterChain} passed in to this method allows the filter
     * to pass on the request and response to the next entity in the chain.
     * @param request The HTTP servlet request
     * @param response The HTTP servlet response
     * @param chain The filter chain
     * @throws ServletException if an exception occurs that interferes with the filter chain's normal operation
     * @throws IOException if an I/O error occurs during the processing
     */
    protected void doFilterInternal(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final FilterChain chain) throws ServletException, IOException {

        HttpServletRequestWrapper requestWrapper;

        requestWrapper = new HttpServletRequestWrapper(request);

        requestWrapper.setBody(requestWrapper.getBody().replaceAll("(?)<!DOCTYPE.*(\\[[\\s\\S]*?\\])?>", ""));

        chain.doFilter(requestWrapper, response);
    }

}
