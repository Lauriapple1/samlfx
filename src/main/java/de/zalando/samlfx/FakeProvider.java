package de.zalando.samlfx;

import java.net.URI;

public final class FakeProvider extends GenericProvider {
    private static final String POST_PATH = "fakeIdp";

    public FakeProvider(final URI baseUri) {
        super(baseUri);
    }

    @Override
    protected String getHttpPostBindingPath() {
        return POST_PATH;
    }
}
