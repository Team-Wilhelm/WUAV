package gui.nodes.dialogs;

import io.github.palexdev.materialfx.controls.MFXProgressBar;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;


public class LoadingDialog extends CustomDialog {
    private MFXProgressBar progressBar;
    private Label progressLabel;
    private GridPane gridPane;


    public LoadingDialog() {
        this("", "");
    }

    public LoadingDialog(String title, String content) {
        super(title, content);
        setUpContentNodes(content);

        super.setContent(gridPane);
        this.setContent(super.getDialogContent());
    }

    private void setUpContentNodes(String content) {
        gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);

        ColumnConstraints [] columnConstraints = new ColumnConstraints[4];
        for (int i = 0; i < columnConstraints.length; i++) {
            columnConstraints[i] = new ColumnConstraints();
            columnConstraints[i].setPercentWidth(25);
        }


        gridPane.getColumnConstraints().addAll(columnConstraints);

        progressBar = new MFXProgressBar();
        progressBar.setAnimationSpeed(1);

        progressLabel = new Label(content);

        gridPane.add(progressBar, 1, 1, 2, 1);
        gridPane.add(progressLabel, 1, 2, 2, 1);
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
