package de.zalando.samlfx;

import java.net.URI;
import java.net.URISyntaxException;

public final class ShibbolethProvider implements SAMLProvider {
    private static final String POST_PATH = "/profile/SAML2/POST/SSO";

    private final URI httpPostBindingUri;

    public ShibbolethProvider(final URI baseUri) {
        try {
            this.httpPostBindingUri = new URI(baseUri.getScheme(), baseUri.getHost(), POST_PATH);
        } catch (final URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public URI getHttpPostBindingUri() {
        return httpPostBindingUri;
    }

    @Override
    public String getHttpPostBindingRequestParameterName() {
        return "SAMLRequest";
    }

    @Override
    public String getHttpPostBindingTokenParameterName() {
        return "RelayState";
    }
}
