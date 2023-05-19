package gui.nodes.dialogs;

import io.github.palexdev.materialfx.controls.MFXProgressBar;
import javafx.beans.property.DoubleProperty;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


public class LoadingDialog extends CustomDialog {
    private MFXProgressBar progressBar;
    private Label progressLabel;


    public LoadingDialog() {
        this("", "");
    }

    public LoadingDialog(String title, String content) {
        super(title, content);

        VBox vbox = new VBox();
        progressBar = new MFXProgressBar();
        progressBar.setAnimationSpeed(1);

        progressLabel = new Label(content);
        vbox.getChildren().addAll(progressBar, progressLabel);

        super.setContent(vbox);
        this.setContent(super.getDialogContent());
    }

    public void setProgress(double progress) {
        progressBar.setProgress(progress);
    }

    public DoubleProperty progressProperty() {
        return progressBar.progressProperty();
    }

    public void clear() {
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
        progressBar.setAnimationSpeed(1);
        progressLabel.setText("");
    }

    public void setProgressLabel(String content) {
        progressLabel.setText(content);
    }
}
