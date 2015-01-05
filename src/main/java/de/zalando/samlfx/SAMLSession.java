package de.zalando.samlfx;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import javafx.scene.web.WebView;

public final class SAMLSession {
    private static final String HOST = "localhost";
    private static final int PORT = 29892;

    private final String idpURL;
    private final WebView webView;

    private Undertow server;

    SAMLSession(final String idpURL, final WebView webView) {
        this.idpURL = idpURL;
        this.webView = webView;
    }

    public String getIdpURL() {
        return idpURL;
    }

    synchronized void open() {
        if (server != null) {
            throw new IllegalStateException("SAML session is already open");
        }

        server = Undertow.builder()
                .addHttpListener(PORT, HOST)
                .setHandler(new HttpHandler() {
                    @Override
                    public void handleRequest(final HttpServerExchange exchange) throws Exception {
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                        exchange.getResponseSender().send("Redirecting to SAML IDP: " + idpURL);
                    }
                }).build();

        server.start();

        webView.getEngine().load("http://" + HOST + ":" + PORT);
    }

    synchronized void close() {
        if (server != null) {
            server.stop();
        }
    }
}
