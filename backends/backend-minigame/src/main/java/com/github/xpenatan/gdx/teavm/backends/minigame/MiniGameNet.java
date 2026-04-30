package com.github.xpenatan.gdx.teavm.backends.minigame;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.*;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;
import com.github.xpenatan.gdx.teavm.backends.minigame.bindings.WX;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.browser.Window;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Network implementation using XMLHttpRequest (available in WeChat Mini Game).
 * Forked from WebNet.
 */
public class MiniGameNet implements Net {

    private final Map<HttpRequest, HttpResponseListener> httpResponseListeners;

    public MiniGameNet() {
        httpResponseListeners = new HashMap<HttpRequest, HttpResponseListener>(16);
    }

    @Override
    public void sendHttpRequest(final HttpRequest httpRequest, HttpResponseListener httpResponseListener) {
        httpResponseListeners.put(httpRequest, httpResponseListener);

        // Use XMLHttpRequest — available in WeChat Mini Game runtime
        org.teavm.jso.ajax.XMLHttpRequest request = org.teavm.jso.ajax.XMLHttpRequest.create();

        Map<String, String> headers = httpRequest.getHeaders();
        final String contentType = headers.get("Content-Type");
        if (contentType != null) {
            request.overrideMimeType(contentType);
        }

        request.setOnReadyStateChange(() -> {
            try {
                if (request.getReadyState() == org.teavm.jso.ajax.XMLHttpRequest.DONE) {
                    HttpResponseListener listener = httpResponseListeners.get(httpRequest);
                    if (listener != null) {
                        final HttpStatus status = new HttpStatus(request.getStatus());
                        final String content = request.getResponseText();

                        final Map<String, List<String>> responseHeaders = new HashMap<String, List<String>>(8);
                        String allHeaders = request.getAllResponseHeaders();
                        String[] allHeaderLines = allHeaders.split("\r?\n|\r");
                        for (String line : allHeaderLines) {
                            int index = line.indexOf(':');
                            if (index >= 0) {
                                String name = line.substring(0, index);
                                String value = line.substring(index + 1).trim();
                                List<String> list = responseHeaders.get(name);
                                if (list == null) {
                                    list = new ArrayList<String>(1);
                                    responseHeaders.put(name, list);
                                }
                                list.add(value);
                            }
                        }

                        httpResponseListeners.remove(httpRequest);
                        listener.handleHttpResponse(new HttpResponse() {
                            @Override
                            public byte[] getResult() {
                                try {
                                    return content.getBytes("UTF-8");
                                } catch (UnsupportedEncodingException e) {
                                    throw new GdxRuntimeException("Cannot create input stream.", e);
                                }
                            }

                            @Override
                            public String getResultAsString() {
                                return content;
                            }

                            @Override
                            public InputStream getResultAsStream() {
                                return new ByteArrayInputStream(getResult());
                            }

                            @Override
                            public HttpStatus getStatus() {
                                return status;
                            }

                            @Override
                            public String getHeader(String name) {
                                List<String> list = responseHeaders.get(name);
                                return (list != null) && (list.size() >= 1) ? list.get(0) : null;
                            }

                            @Override
                            public Map<String, List<String>> getHeaders() {
                                return responseHeaders;
                            }
                        });
                    }
                }
            } catch (Exception e) {
                httpResponseListeners.remove(httpRequest);
                httpResponseListener.failed(e);
            }
        });

        String content = httpRequest.getContent();
        if (content == null) {
            InputStream input = httpRequest.getContentStream();
            if (input != null) {
                try {
                    content = StreamUtils.copyStreamToString(input);
                } catch (IOException e) {
                    throw new GdxRuntimeException("Error reading string from stream.", e);
                }
            }
        }

        String method = httpRequest.getMethod();
        boolean doingOutput = (content != null) && (method.equalsIgnoreCase(HttpMethods.POST) ||
                method.equalsIgnoreCase(HttpMethods.PUT));

        String url = httpRequest.getUrl();
        if ((content != null) && (!"".equals(content)) && (!doingOutput)) {
            url += "?" + content;
        }

        request.open(method, url, true);

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            request.setRequestHeader(entry.getKey(), entry.getValue());
        }

        if (doingOutput) {
            request.send(content);
        } else {
            request.send();
        }
    }

    @Override
    public void cancelHttpRequest(HttpRequest httpRequest) {
        HttpResponseListener listener = httpResponseListeners.get(httpRequest);
        if (listener != null) {
            listener.cancelled();
            httpResponseListeners.remove(httpRequest);
        }
    }

    @Override
    public boolean isHttpRequestPending(HttpRequest httpRequest) {
        return httpResponseListeners.get(httpRequest) != null;
    }

    @Override
    public ServerSocket newServerSocket(Protocol protocol, String hostname, int port, ServerSocketHints hints) {
        throw new GdxRuntimeException("Streaming sockets not available via WeChat.");
    }

    @Override
    public ServerSocket newServerSocket(Protocol protocol, int port, ServerSocketHints hints) {
        throw new GdxRuntimeException("Streaming sockets not available via WeChat.");
    }

    @Override
    public Socket newClientSocket(Protocol protocol, String host, int port, SocketHints hints) {
        throw new GdxRuntimeException("Streaming sockets not available via WeChat.");
    }

    @Override
    public boolean openURI(String URI) {
        // WeChat doesn't support opening external URIs
        return false;
    }
}
