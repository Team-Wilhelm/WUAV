package gui.controller.ViewControllers;

import utils.enums.ResultState;
import io.github.palexdev.materialfx.css.themes.MFXThemeManager;
import io.github.palexdev.materialfx.css.themes.Themes;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public abstract class ViewController<T> {
    public abstract void setProgressVisibility(boolean isVisible);
    public abstract void bindProgressToTask(Task<ResultState> task);
    public abstract void unbindProgress();
    public abstract void refreshItems(List<T> items);
    public abstract void refreshItems();

    protected FXMLLoader openWindow(String fxmlPath, Modality modalityType) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
        Stage stage = new Stage();
        Scene scene;

        try {
            scene = new Scene(fxmlLoader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        MFXThemeManager.addOn(scene, Themes.DEFAULT, Themes.LEGACY);
        stage.setScene(scene);
        stage.setTitle("WUAV Documentation Management System");
        stage.getIcons().add(new Image("/img/WUAV.png"));
        stage.centerOnScreen();
        stage.initModality(modalityType);
        stage.show();
        stage.setOnShown(e -> Platform.runLater(() -> {
            stage.requestFocus();
            stage.toFront();
        }));

        return fxmlLoader;
    }
}
