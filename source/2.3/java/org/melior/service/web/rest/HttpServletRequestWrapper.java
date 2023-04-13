/* __  __      _ _            
  |  \/  |    | (_)           
  | \  / | ___| |_  ___  _ __ 
  | |\/| |/ _ \ | |/ _ \| '__|
  | |  | |  __/ | | (_) | |   
  |_|  |_|\___|_|_|\___/|_|   
        Service Harness
*/
package org.melior.service.web.rest;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

/**
 * Wraps the servlet request object with a custom implementation to drain the input stream
 * of the request object into a buffer, to allow the request content to be filtered or modified
 * multiple times without incurring penalties.
 * @author Melior
 * @since 2.3
 */
public class HttpServletRequestWrapper extends javax.servlet.http.HttpServletRequestWrapper {

    private byte[] body;

    /**
     * Constructor
     * @param request The HTTP servlet request
     */
    public HttpServletRequestWrapper(
        final HttpServletRequest request) {

        super(request);

        ByteArrayOutputStream outputStream;

        outputStream = new ByteArrayOutputStream() {public byte[] toByteArray() {return buf;}};

        try {

            drainStream(request.getInputStream(), outputStream);
        }
        catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        body = outputStream.toByteArray();
    }

    /**
     * Drain source stream into target stream.
     * @param source The source stream
     * @param target The target stream
     * @throws IOException if an I/O error occurs while draining the source stream
     */
    private void drainStream(
        final InputStream source,
        final OutputStream target) throws IOException {

        byte[] buffer = new byte[1024];
        int length;

        while ((length = source.read(buffer, 0, buffer.length)) != -1) {
            target.write(buffer, 0, length);
        }

    }

    /**
     * Create buffered reader around captured request body.
     * @return The buffered reader
     */
    public BufferedReader getReader() {

        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(body)));
    }

    /**
     * Create servlet input stream around captured request body.
     * @return The servlet input stream
     */
    public ServletInputStream getInputStream() {

        final ByteArrayInputStream inputStream = new ByteArrayInputStream(body);

        return new ServletInputStream() {

            /**
             * Returns true if data can be read without blocking, else returns false.
             * @return {@code true} if data can be read without blocking, {@code false} otherwise
             */
            public boolean isReady() {
                return true;
            }

            /**
             * Returns true if all the data from the stream has been read, else returns false.
             * @return {@code true} if all the data has been read, {@code false} otherwise
             */
            public boolean isFinished() {
                return inputStream.available() == 0;
            }

            /**
             * Reads the next byte of data from the input stream.  If no byte is available because
             * the end of the stream has been reached, the value {@code -1} is returned.
             * @return The next byte of data, or {@code -1} if the end of the stream is reached
             */
            public int read() {
                return inputStream.read();
            }

            /**
             * Instructs the servlet input stream to invoke the provided {@code ReadListener}
             * when it is possible to read.
             * @param readListener The read listener
             */
            public void setReadListener(
                final ReadListener readListener) {
            }

        };

    }

    /**
     * Get original request body as a {@code String}.
     * @return The original request body
     */
    public String getBody() {
        return new String(body);
    }

    /**
     * Set new request body from provided {@code String}.
     * @param body The new request body
     */
    public void setBody(
        final String body) {
        this.body = body.getBytes();
    }

}
