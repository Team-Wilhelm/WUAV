package gui.controller;

import io.github.palexdev.materialfx.controls.MFXProgressSpinner;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class UserController implements Initializable {
    @FXML
    private MFXProgressSpinner progressSpinner;
    @FXML
    private Label progressLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setProgressVisibility(false);
    }

    private void setProgressVisibility(boolean visible) {
        progressSpinner.setVisible(visible);
        progressLabel.setVisible(visible);
    }
}
