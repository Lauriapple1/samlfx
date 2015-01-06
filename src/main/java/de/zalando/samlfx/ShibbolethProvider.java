package de.zalando.samlfx;

import java.net.URI;

public final class ShibbolethProvider extends GenericProvider {
    private static final String POST_PATH = "profile/SAML2/POST/SSO";

    public ShibbolethProvider(final URI baseUri) {
        super(baseUri);
    }

    @Override
    protected String getHttpPostBindingPath() {
        return POST_PATH;
    }
}
