package gui.controller;

import be.User;
import gui.util.SceneManager;
import gui.model.UserModel;
import gui.util.DialogueManager;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.css.themes.MFXThemeManager;
import io.github.palexdev.materialfx.css.themes.Themes;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
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
    @FXML
    private BorderPane borderPane;
    private Parent root;
    private Stage stage;
    private MenuController menuController;
    private final UserModel userModel = UserModel.getInstance();
    private HashPasswordHelper hashPasswordHelper = new HashPasswordHelper();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(this::setEnterKeyAction);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.MENU_SCENE));
            root = loader.load();
            menuController = loader.getController();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Boolean loginUser(Event event){
        User user = userModel.getUserByUsername(usernameInput.getText());
        if(user == null){
            DialogueManager.getInstance().showError(
                    "Login failed!",
                    "Check that username and password are correct",
                    borderPane);
            return false;
        }
        if(userModel.logIn(usernameInput.getText(), hashPasswordHelper.hashPassword(passwordInput.getText(), user.getSalt()))){
            userModel.setLoggedInUser(user);
            openMenuView();
            return true;
        }
        DialogueManager.getInstance().showError(
                "Login failed!",
                "Check that username and password are correct",
                borderPane);
        return false;
    }

    private void openMenuView(){
        stage = (Stage) btnLogin.getScene().getWindow();
        Scene scene = new Scene(root);
        MFXThemeManager.addOn(scene, Themes.DEFAULT, Themes.LEGACY);

        stage.setScene(scene);
        stage.setWidth(Screen.getPrimary().getBounds().getWidth() - 200);
        stage.setHeight(Screen.getPrimary().getBounds().getHeight() - 200);

        menuController.userLogInAction(scene); // Needs to be called after the scene is set
        stage.centerOnScreen();
        stage.setMaximized(true);
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
