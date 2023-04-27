package gui.controller.ViewControllers;

import com.sun.tools.javac.Main;
import gui.tasks.TaskState;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public abstract class ViewController {
    public abstract void setProgressVisibility(boolean isVisible);
    public abstract void bindProgressToTask(Task<TaskState> task);
    public abstract void unbindProgress();
    public abstract void refreshLastFocusedCard();
    public abstract void refreshItems();

    protected FXMLLoader openWindow(String fxmlPath, Modality modalityType) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.setTitle("EASV Ticket System");
        //TODO add icon
        //stage.getIcons().add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream(""))));
        stage.centerOnScreen();
        stage.initModality(modalityType);
        stage.show();
        return fxmlLoader;
    }
}
