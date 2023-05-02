package gui.controller;

import dal.UserDAO;
import gui.SceneManager;
import gui.model.UserModel;
import gui.util.AlertManager;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.HashPasswordHelper;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static javafx.scene.input.KeyCode.ENTER;

public class LoginViewController implements Initializable {
    @FXML
    private MFXButton btnLogin;
    @FXML
    private MFXTextField usernameInput;
    @FXML
    private MFXPasswordField passwordInput;

    private Parent root;
    private Stage stage;
    private final UserModel userModel = UserModel.getInstance();
    private HashPasswordHelper hashPasswordHelper = new HashPasswordHelper();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(this::setEnterKeyAction);
    }

    public Boolean loginUser(Event event){
        if(userModel.logIn(usernameInput.getText(), hashPasswordHelper.hashPassword(passwordInput.getText()))){
            userModel.setLoggedInUser(userModel.getUserByUsername(usernameInput.getText()));
            openMenuView();
            return true;
        }
        AlertManager.getInstance().showError(
                "Login failed!",
                "Check that username and password are correct",
                btnLogin.getScene().getWindow());
        return false;
    }

    private void openMenuView(){
        FXMLLoader menuLoader = new FXMLLoader(getClass().getResource(SceneManager.MENU_SCENE));
        try {
            root = menuLoader.load();
            stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.setMaximized(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
