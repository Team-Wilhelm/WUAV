import be.Document;
import bll.PdfGenerator;
import gui.SceneManager;
import gui.model.DocumentModel;
import gui.util.ImageCropper;
import io.github.palexdev.materialfx.css.themes.MFXThemeManager;
import io.github.palexdev.materialfx.css.themes.Themes;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import utils.ThreadPool;

import java.util.Objects;
import java.util.UUID;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        long start = System.currentTimeMillis();
        Parent root;
        if (!true)
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(SceneManager.LOGIN_SCENE)));
        else {
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(SceneManager.MENU_SCENE)));
            primaryStage.setMaximized(true);
        }

        primaryStage.setTitle("WUAV Documentation Management System");
        primaryStage.getIcons().add(new Image("/img/WUAV.png"));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        MFXThemeManager.addOn(scene, Themes.DEFAULT, Themes.LEGACY);
        primaryStage.centerOnScreen();
        primaryStage.show();
        System.out.println("Time to load: " + (System.currentTimeMillis() - start) + "ms");
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void stop() {
        ThreadPool.getInstance().shutdown();
    }
}