package de.zalando.samlfx;

import io.undertow.Undertow;
import io.undertow.util.Headers;
import javafx.scene.web.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public final class SAMLSession {
    private static final Charset CHARSET = Charset.forName("UTF-8");

    private static final String HOST = "localhost";
    private static final int PORT = 29892;

    private final SAMLProvider samlProvider;
    private final WebView webView;

    private Undertow server;

    SAMLSession(final SAMLProvider samlProvider, final WebView webView) {
        this.samlProvider = samlProvider;
        this.webView = webView;
    }

    synchronized void open() {
        if (server != null) {
            throw new IllegalStateException("SAML session is already open");
        }

        final String samlRequest = "foo";
        final String samlToken = "bar";

        String html = loadHtml("SAMLLoginSetup.html");
        html = html.replace("#__IDP_URI__#", samlProvider.getHttpPostBindingUri().toString());
        html = html.replace("#__SAML_REQUEST_NAME__#", samlProvider.getHttpPostBindingRequestParameterName());
        html = html.replace("#__SAML_REQUEST__#", samlRequest);
        html = html.replace("#__SAML_TOKEN_NAME__#", samlProvider.getHttpPostBindingTokenParameterName());
        html = html.replace("#__SAML_TOKEN__#", samlToken);
        final String loginSetupHtml = html;

        server = Undertow.builder()
                .addHttpListener(PORT, HOST)
                .setHandler(exchange -> {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html; charset=" + CHARSET.toString());
                    exchange.getResponseSender().send(loginSetupHtml, CHARSET);
                }).build();

        server.start();

        webView.getEngine().load("http://" + HOST + ":" + PORT);
    }

    synchronized void close() {
        if (server != null) {
            server.stop();
        }
    }

    private String loadHtml(final String filename) {
        final InputStream in = this.getClass().getResourceAsStream(filename);

        final StringBuilder sb = new StringBuilder();

        try (final BufferedReader br = new BufferedReader(new InputStreamReader(in, CHARSET))) {
            for (int c = br.read(); c != -1; c = br.read()) {
                sb.append((char)c);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return sb.toString();
    }
}
