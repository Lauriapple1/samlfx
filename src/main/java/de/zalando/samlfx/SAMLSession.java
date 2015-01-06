package de.zalando.samlfx;

import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.Headers;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.web.WebView;
import org.opensaml.saml2.core.Assertion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public final class SAMLSession {
    private static final Logger LOG = Logger.getLogger(SAMLSession.class.getName());

    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static final String HOST = "localhost";

    private final int port;

    private final SAMLProvider samlProvider;
    private final WebView webView;
    private final EventHandler<SAMLLoginEvent> loginEventHandler;

    private Undertow server;

    private AtomicReference<Optional<Assertion>> samlAssertion = new AtomicReference<>(Optional.<Assertion>empty());

    SAMLSession(final int port, final SAMLProvider samlProvider, final WebView webView,
                final EventHandler<SAMLLoginEvent> loginEventHandler) {
        this.port = port;
        this.samlProvider = samlProvider;
        this.webView = webView;
        this.loginEventHandler = loginEventHandler;
    }

    public Optional<Assertion> getSamlAssertion() {
        return samlAssertion.get();
    }

    synchronized void open() {
        if (server != null || samlAssertion.get().isPresent()) {
            throw new IllegalStateException("SAML session is already used");
        }

        server = Undertow.builder()
                .addHttpListener(port, HOST)
                .setHandler(exchange -> {
                    if ("/".equals(exchange.getRequestPath())) {
                        handleInitialization(exchange);

                    } else if ("/fakeIdp".equals(exchange.getRequestPath())) {
                        // TODO remove when correct implementation is done
                        handleFakeIdp(exchange);

                    } else {
                        handleResponse(exchange);

                    }
                }).build();

        server.start();
        LOG.fine("started embedded http server on " + HOST + ":" + port);

        webView.getEngine().load("http://" + HOST + ":" + port);
        LOG.info("started SAML 2.0 WebSSO HTTP-POST binding flow with " + samlProvider.getHttpPostBindingUri());
    }

    synchronized void close() {
        if (server != null) {
            LOG.fine("stopped embedded http server");
            server.stop();
            server = null;
        }
    }

    private void handleInitialization(final HttpServerExchange exchange) {
        // initial setup to send POST to idp
        final String samlRequest = "foo";
        final String samlToken = "bar";

        String html = loadHtml("SAMLLoginSetup.html");
        html = html.replace("#__IDP_URI__#", samlProvider.getHttpPostBindingUri().toString());
        html = html.replace("#__SAML_REQUEST_NAME__#", samlProvider.getHttpPostBindingRequestParameterName());
        html = html.replace("#__SAML_REQUEST__#", samlRequest);
        html = html.replace("#__SAML_TOKEN_NAME__#", samlProvider.getHttpPostBindingTokenParameterName());
        html = html.replace("#__SAML_TOKEN__#", samlToken);
        final String loginSetupHtml = html;

        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html; charset=" + CHARSET.toString());
        exchange.getResponseSender().send(loginSetupHtml, CHARSET);

        LOG.info("user sent to identity provider for authentication; waiting for return");
    }

    private void handleFakeIdp(final HttpServerExchange exchange) throws IOException {
        LOG.info("generating fake response like a true fake identity provider");

        final FormData formData = FormParserFactory.builder().build().createParser(exchange).parseBlocking();

        final String samlRequestEncoded = formData.getFirst("SAMLRequest").getValue();
        final String relayState = formData.getFirst("RelayState").getValue();

        String fakeHtml = loadHtml("SAMLFakeLogin.html");
        fakeHtml = fakeHtml.replace("#__SAML_RESEPONSE__#", "foo");
        fakeHtml = fakeHtml.replace("#__SAML_TOKEN__#", relayState);

        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html; charset=" + CHARSET.toString());
        exchange.getResponseSender().send(fakeHtml, CHARSET);
    }

    private void handleResponse(final HttpServerExchange exchange) {
        // idp response to us with the SAML assertion

        // TODO parse assertion
        final Assertion assertion = null;
        //samlAssertion.set(Optional.of(assertion));

        LOG.info("user authenticated; triggering login event");
        new Thread(() -> {
            Platform.runLater(() -> {
                loginEventHandler.handle(new SAMLLoginEvent(assertion));
            });
        }).start();
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

    /*
    private void doAuthenticationRedirect(final HttpServletResponse response, final HttpSession currentSession, final String gotoURL, final SAMLMetaData metaData) throws IllegalArgumentException, SecurityException, IllegalAccessException {
        AuthnRequest authnRequest = generateAuthnRequest(metaData);

        SAMLUtil.logSAMLObject(authnRequest);

        // Save the request ID to session for future validation
        currentSession.setAttribute("AuthnRequestID", authnRequest.getID());
        currentSession.setAttribute("goto", gotoURL);

        HttpServletResponseAdapter responseAdapter = new HttpServletResponseAdapter(response, true);
        BasicSAMLMessageContext<SAMLObject, AuthnRequest, SAMLObject> context = new BasicSAMLMessageContext<SAMLObject, AuthnRequest, SAMLObject>();
        context.setPeerEntityEndpoint(getEndpointFromMetaData());
        context.setOutboundSAMLMessage(authnRequest);
        context.setOutboundSAMLMessageSigningCredential(getSigningCredential());
        context.setOutboundMessageTransport(responseAdapter);

        HTTPRedirectDeflateEncoder encoder = new HTTPRedirectDeflateEncoder();

        try {
            encoder.encode(context);
        } catch (MessageEncodingException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private AuthnRequest generateAuthnRequest(final SAMLMetaData metaData) throws IllegalArgumentException, SecurityException, IllegalAccessException {

        AuthnRequest authnRequest = SAMLUtil.buildSAMLObjectWithDefaultName(AuthnRequest.class);

        authnRequest.setForceAuthn(true);
        authnRequest.setIsPassive(false);
        authnRequest.setIssueInstant(new DateTime());
        for (SingleSignOnService sss : metaData.getIdpEntityDescriptor().getIDPSSODescriptor(SAMLConstants.SAML20P_NS).getSingleSignOnServices()) {
            if (sss.getBinding().equals(SAMLConstants.SAML2_REDIRECT_BINDING_URI)) {
                authnRequest.setDestination(sss.getLocation());
            }
        }
        authnRequest.setProtocolBinding(SAMLConstants.SAML2_ARTIFACT_BINDING_URI);

        String deployURL = getDeployURL();
        if (deployURL.charAt(deployURL.length() - 1) == '/') {
            deployURL = deployURL.substring(0, deployURL.length() - 1);
        }
        authnRequest.setAssertionConsumerServiceURL(deployURL + SAMLMetaData.CONSUMER_PATH);

        authnRequest.setID(SAMLUtil.getSecureRandomIdentifier());

        Issuer issuer = SAMLUtil.buildSAMLObjectWithDefaultName(Issuer.class);
        issuer.setValue(getSPEntityId());
        authnRequest.setIssuer(issuer);

        NameIDPolicy nameIDPolicy = SAMLUtil.buildSAMLObjectWithDefaultName(NameIDPolicy.class);
        nameIDPolicy.setSPNameQualifier(getSPEntityId());
        nameIDPolicy.setAllowCreate(true);
        nameIDPolicy.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:transient");

        authnRequest.setNameIDPolicy(nameIDPolicy);

        RequestedAuthnContext requestedAuthnContext = SAMLUtil.buildSAMLObjectWithDefaultName(RequestedAuthnContext.class);
        requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.MINIMUM);

        AuthnContextClassRef authnContextClassRef = SAMLUtil.buildSAMLObjectWithDefaultName(AuthnContextClassRef.class);
        authnContextClassRef.setAuthnContextClassRef("urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport");

        requestedAuthnContext.getAuthnContextClassRefs().add(authnContextClassRef);
        authnRequest.setRequestedAuthnContext(requestedAuthnContext);

        return authnRequest;
    }
    */
}
