package de.zalando.samlfx;

import java.net.URI;
import java.net.URISyntaxException;

public class GenericProvider implements SAMLProvider {
    private static final String POST_PATH = "SAML2/SSO/POST";

    private final URI baseUri;

    public GenericProvider(final URI baseUri) {
        if (baseUri == null) {
            throw new NullPointerException("baseUri");
        }
        this.baseUri = baseUri;
    }

    protected String getHttpPostBindingPath() {
        return POST_PATH;
    }

    @Override
    public URI getHttpPostBindingUri() {
        try {
            final String path;
            if (baseUri.getPath().endsWith("/")) {
                path = baseUri.getPath() + getHttpPostBindingPath();
            } else {
                path = baseUri.getPath() + "/" + getHttpPostBindingPath();
            }
            return new URI(baseUri.getScheme(), baseUri.getUserInfo(), baseUri.getHost(), baseUri.getPort(), path,
                    baseUri.getQuery(), baseUri.getFragment());
        } catch (final URISyntaxException e) {
            throw new IllegalStateException(e);
        }
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
