package gui.nodes.dialogs;

import io.github.palexdev.materialfx.controls.MFXProgressBar;
import javafx.beans.property.DoubleProperty;

public class LoadingDialog extends CustomDialog {
    private MFXProgressBar progressBar;

    public LoadingDialog() {
        this("", "");
    }

    public LoadingDialog(String title, String content) {
        super(title, content);
        progressBar = new MFXProgressBar();

        super.setContent(progressBar);
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
    }
}
