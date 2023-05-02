package gui.controller;

import com.sun.tools.javac.Main;
import gui.SceneManager;
import gui.model.DocumentModel;
import gui.model.UserModel;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static javafx.scene.input.KeyCode.ENTER;

public class LoginViewController implements Initializable {
    @FXML
    private MFXTextField usernameInput;
    @FXML
    private MFXPasswordField passwordInput;

    private Parent root;
    private Stage stage;
    private FXMLLoader fxmlLoader;
    private final UserModel userModel = UserModel.getInstance();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(this::setEnterKeyAction);

    }

    public void loginUser(Event event) {
        fxmlLoader = new FXMLLoader(getClass().getResource(SceneManager.MENU_SCENE));
    }

    private void setEnterKeyAction() {
        Scene scene = usernameInput.getScene();
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == ENTER) {
                loginUser(new Event(passwordInput, passwordInput, Event.ANY));
            }
        });
    }
}
