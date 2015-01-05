package de.zalando.samlfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class SAMLFXExampleMain extends Application {
    @Override
    public void start(final Stage primaryStage) throws Exception {
        Platform.setImplicitExit(true);

        final Parent root = FXMLLoader.load(getClass().getResource("SAMLFXExample.fxml"));

        final Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("SAML Login Example");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
