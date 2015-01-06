package de.zalando.samlfx;


import javafx.event.Event;
import javafx.event.EventType;
import org.opensaml.saml2.core.Assertion;

public class SAMLLoginEvent extends Event {
    public static final EventType<SAMLLoginEvent> SAML_LOGIN = new EventType<>(ANY, "SAML_LOGIN");

    private final Assertion assertion;

    public SAMLLoginEvent(final Assertion assertion) {
        super(SAML_LOGIN);
        this.assertion = assertion;
    }

    public Assertion getAssertion() {
        return assertion;
    }
}
