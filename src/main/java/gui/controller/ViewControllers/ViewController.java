package gui.controller.ViewControllers;

import com.sun.tools.javac.Main;
import gui.tasks.TaskState;
import io.github.palexdev.materialfx.css.themes.MFXThemeManager;
import io.github.palexdev.materialfx.css.themes.Themes;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public abstract class ViewController<T> {
    public abstract void setProgressVisibility(boolean isVisible);
    public abstract void bindProgressToTask(Task<TaskState> task);
    public abstract void unbindProgress();
    public abstract void refreshLastFocusedCard();
    public abstract void refreshItems(List<T> items);
    public abstract void refreshItems();

    protected FXMLLoader openWindow(String fxmlPath, Modality modalityType) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load());
        MFXThemeManager.addOn(scene, Themes.DEFAULT, Themes.LEGACY);
        stage.setScene(scene);
        stage.setTitle("WUAV Documentation Management System");
        stage.getIcons().add(new Image("/img/WUAV.png"));
        stage.centerOnScreen();
        stage.initModality(modalityType);
        stage.show();
        return fxmlLoader;
    }
}
