package de.zalando.samlfx;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

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

        samlLoginControl.login(loginUrl.getText());

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
