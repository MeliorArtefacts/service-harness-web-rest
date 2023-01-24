package org.melior.client.web.rest.patch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

/**
 * Copy of Spring Boot StringHttpMessageConverter with patches to improve SSL performance.
 * Will be dropped when Spring Boot is patched.
 */
public class StringHttpMessageConverter extends AbstractHttpMessageConverter<String> {

    private static final MediaType APPLICATION_PLUS_JSON = new MediaType("application", "*+json");

    /**
     * The default charset used by the converter.
     */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;


    @Nullable
    private volatile List<Charset> availableCharsets;

    private boolean writeAcceptCharset = false;


    /**
     * A default constructor that uses {@code "ISO-8859-1"} as the default charset.
     * @see #StringHttpMessageConverter(Charset)
     */
    public StringHttpMessageConverter() {
        this(DEFAULT_CHARSET);
    }

    /**
     * A constructor accepting a default charset to use if the requested content
     * type does not specify one.
     * @param defaultCharset the
     */
    public StringHttpMessageConverter(Charset defaultCharset) {
        super(defaultCharset, MediaType.TEXT_PLAIN, MediaType.ALL);
    }


    /**
     * Whether the {@code Accept-Charset} header should be written to any outgoing
     * request sourced from the value of {@link Charset#availableCharsets()}.
     * The behavior is suppressed if the header has already been set.
     * <p>As of 5.2, by default is set to {@code false}.
     * @param writeAcceptCharset the
     */
    public void setWriteAcceptCharset(boolean writeAcceptCharset) {
        this.writeAcceptCharset = writeAcceptCharset;
    }


    @Override
    public boolean supports(Class<?> clazz) {
        return String.class == clazz;
    }

    @Override
    protected String readInternal(Class<? extends String> clazz, HttpInputMessage inputMessage) throws IOException {
        Charset charset = getContentTypeCharset(inputMessage.getHeaders().getContentType());
        InputStream inputStream = StreamUtils.nonClosing(inputMessage.getBody());
        return StreamUtils.copyToString(inputStream, charset);
    }

    @Override
    protected Long getContentLength(String str, @Nullable MediaType contentType) {
        Charset charset = getContentTypeCharset(contentType);
        return (long) str.getBytes(charset).length;
    }


    @Override
    protected void addDefaultHeaders(HttpHeaders headers, String s, @Nullable MediaType type) throws IOException {
        if (headers.getContentType() == null ) {
            if (type != null && type.isConcrete() &&
                    (type.isCompatibleWith(MediaType.APPLICATION_JSON) ||
                    type.isCompatibleWith(APPLICATION_PLUS_JSON))) {

                headers.setContentType(type);
            }
        }
        super.addDefaultHeaders(headers, s, type);
    }

    @Override
    protected void writeInternal(String str, HttpOutputMessage outputMessage) throws IOException {
        HttpHeaders headers = outputMessage.getHeaders();
        if (this.writeAcceptCharset && headers.get(HttpHeaders.ACCEPT_CHARSET) == null) {
            headers.setAcceptCharset(getAcceptedCharsets());
        }
        Charset charset = getContentTypeCharset(headers.getContentType());
        OutputStream outputStream = StreamUtils.nonClosing(outputMessage.getBody());
        StreamUtils.copy(str, charset, outputStream);
    }


    /**
     * Return the list of supported {@link Charset Charsets}.
     * <p>By default, returns {@link Charset#availableCharsets()}.
     * Can be overridden in subclasses.
     * @return the list of accepted charsets
     */
    protected List<Charset> getAcceptedCharsets() {
        List<Charset> charsets = this.availableCharsets;
        if (charsets == null) {
            charsets = new ArrayList<>(Charset.availableCharsets().values());
            this.availableCharsets = charsets;
        }
        return charsets;
    }

    private Charset getContentTypeCharset(@Nullable MediaType contentType) {
        if (contentType != null) {
            Charset charset = contentType.getCharset();
            if (charset != null) {
                return charset;
            }
            else if (contentType.isCompatibleWith(MediaType.APPLICATION_JSON) ||
                    contentType.isCompatibleWith(APPLICATION_PLUS_JSON)) {

                return StandardCharsets.UTF_8;
            }
        }
        Charset charset = getDefaultCharset();
        Assert.state(charset != null, "No default charset");
        return charset;
    }

}
