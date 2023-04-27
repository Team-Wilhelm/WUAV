package gui.controller.ViewControllers;

import gui.tasks.TaskState;
import io.github.palexdev.materialfx.controls.MFXProgressSpinner;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class DocumentController extends ViewController implements Initializable {
    @FXML
    private MFXProgressSpinner progressSpinner;
    @FXML
    private Label progressLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setProgressVisibility(false);
    }

    @Override
    public void setProgressVisibility(boolean visible) {
        progressSpinner.setVisible(visible);
        progressLabel.setVisible(visible);
    }

    @Override
    public void bindProgressToTask(Task<TaskState> task) {
        progressSpinner.progressProperty().bind(task.progressProperty());
        progressLabel.textProperty().bind(task.messageProperty());
    }

    @Override
    public void unbindProgress() {
        progressSpinner.progressProperty().unbind();
        progressSpinner.setProgress(100);
        String text = progressLabel.getText();
        progressLabel.textProperty().unbind();
        progressLabel.setText(text);
    }

    @Override
    public void refreshLastFocusedCard() {

    }

    @Override
    public void refreshItems() {

    }
}
