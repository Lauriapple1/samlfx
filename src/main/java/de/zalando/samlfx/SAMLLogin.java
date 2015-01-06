package de.zalando.samlfx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class SAMLLogin extends ScrollPane {
    /**
     * Port of our embedded webserver
     */
    @FXML
    private int localPort = 29892;

    /**
     * The actual browser to use for the user to enter its credentials.
     */
    @FXML
    private WebView webView;

    /**
     * onLogin callback
     */
    private ObjectProperty<EventHandler<SAMLLoginEvent>> onLogin = new ObjectPropertyBase<EventHandler<SAMLLoginEvent>>() {
        @Override
        protected void invalidated() {
            setEventHandler(new EventType<>(Event.ANY, "SAMLLogin"), get());
        }

        @Override
        public Object getBean() {
            return SAMLLogin.this;
        }

        @Override
        public String getName() {
            return "onLogin";
        }
    };

    /**
     * Holds the current session.
     */
    private AtomicReference<Optional<SAMLSession>> currentSession =
            new AtomicReference<>(Optional.<SAMLSession>empty());

    public SAMLLogin() {
        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SAMLLogin.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(final int localPort) {
        this.localPort = localPort;
    }

    public final ObjectProperty<EventHandler<SAMLLoginEvent>> onLoginProperty() {
        return onLogin;
    }

    public final EventHandler<SAMLLoginEvent> getOnLogin() {
        return onLoginProperty().get();
    }

    public final void setOnLogin(EventHandler<SAMLLoginEvent> eventHandler) {
        onLoginProperty().set(eventHandler);
    }

    public Optional<SAMLSession> getCurrentSession() {
        return currentSession.get();
    }

    public synchronized void login(final SAMLProvider samlProvider) {
        if (samlProvider == null) {
            throw new NullPointerException("samlProvider");
        }

        close();

        final SAMLSession session = new SAMLSession(localPort, samlProvider, webView, onLogin.getValue());
        session.open();
        currentSession.set(Optional.of(session));
    }

    public synchronized void close() {
        if (currentSession.get().isPresent()) {
            currentSession.get().get().close();
            currentSession.set(Optional.<SAMLSession>empty());
        }
    }
}
