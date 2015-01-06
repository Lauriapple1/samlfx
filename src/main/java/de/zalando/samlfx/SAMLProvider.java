package de.zalando.samlfx;

import java.net.URI;

public interface SAMLProvider {
    URI getHttpPostBindingUri();
    String getHttpPostBindingRequestParameterName();
    String getHttpPostBindingTokenParameterName();
}
