package de.zalando.samlfx;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.net.URI;
import java.net.URISyntaxException;

public final class SAMLFXExampleController {
    @FXML
    public Pane loginSetupScreen;

    @FXML
    public TextField loginUrl;

    @FXML
    public Pane loginScreen;

    @FXML
    private SAMLLogin samlLogin;

    @FXML
    public Pane finalScreen;

    @FXML
    public Label subjectLabel;

    @FXML
    public void login() {
        loginSetupScreen.setVisible(false);

        final URI idpURI;
        try {
            idpURI = new URI(loginUrl.getText());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
        final SAMLProvider samlProvider = new FakeProvider(idpURI);

        samlLogin.login(samlProvider);

        loginScreen.setVisible(true);
    }

    @FXML
    public void loginSuccessful(final SAMLLoginEvent loginEvent) {
        loginScreen.setVisible(false);

        if (loginEvent.getAssertion() == null) {
            // TODO remove, should never happen, else use optional
            subjectLabel.setText("-unknown-");
        } else {
            subjectLabel.setText(loginEvent.getAssertion().getSubject().getNameID().getValue());
        }
        finalScreen.setVisible(true);
    }

    @FXML
    public void logout() {
        finalScreen.setVisible(false);
        loginSetupScreen.setVisible(true);
    }

}
