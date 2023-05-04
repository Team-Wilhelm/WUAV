import be.Document;
import bll.PdfGenerator;
import gui.SceneManager;
import gui.model.DocumentModel;
import gui.util.ImageCropper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.UUID;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root;
        if (!true)
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(SceneManager.LOGIN_SCENE)));
        else {
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(SceneManager.MENU_SCENE)));
            primaryStage.setMaximized(true);
        }

        ImageCropper imageCropper = new ImageCropper(null);
        imageCropper.chooseImage();

        primaryStage.setTitle("WUAV Documentation Management System");
        primaryStage.getIcons().add(new Image("/img/WUAV.png"));
        primaryStage.setScene(new Scene(root));
        primaryStage.centerOnScreen();
        //primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}