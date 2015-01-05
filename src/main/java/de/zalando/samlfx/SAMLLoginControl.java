package de.zalando.samlfx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class SAMLLoginControl extends VBox {
    @FXML
    private WebView webView;

    private AtomicReference<Optional<SAMLSession>> currentSession =
            new AtomicReference<>(Optional.<SAMLSession>empty());

    public SAMLLoginControl() {
        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SAMLLogin.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public Optional<SAMLSession> getCurrentSession() {
        return currentSession.get();
    }

    public synchronized void close() {
        if (currentSession.get().isPresent()) {
            currentSession.get().get().close();
            currentSession.set(Optional.<SAMLSession>empty());
        }
    }

    public synchronized void login(final String idpURL) {
        close();

        final SAMLSession session = new SAMLSession(idpURL, webView);
        session.open();
        currentSession.set(Optional.of(session));
    }
}
