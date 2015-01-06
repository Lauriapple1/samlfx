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
    private SAMLLoginControl samlLoginControl;

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
        final SAMLProvider samlProvider = new ShibbolethProvider(idpURI);

        samlLoginControl.login(samlProvider);

        loginScreen.setVisible(true);
    }

    public void loginSuccessful() {
        loginScreen.setVisible(false);

        // TODO set credentials in label
        subjectLabel.setText("Unknown");
        finalScreen.setVisible(true);
    }

    @FXML
    public void logout() {
        finalScreen.setVisible(false);
        loginSetupScreen.setVisible(true);
    }
}
