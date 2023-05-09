package gui.controller;

import be.User;
import be.enums.UserRole;
import gui.SceneManager;
import gui.model.UserModel;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

public class MenuController implements Initializable {
    @FXML
    private GridPane gridPane;
    private Node documentView, employeeView, currentScene;
    private User user;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FXMLLoader documentLoader = new FXMLLoader(getClass().getResource(SceneManager.DOCUMENT_SCENE));
        FXMLLoader employeeLoader = new FXMLLoader(getClass().getResource(SceneManager.EMPLOYEE_SCENE));
        try {
            documentView = documentLoader.load();
            employeeView = employeeLoader.load();
            currentScene = documentView;
            gridPane.add(currentScene, 2, 0, 1,gridPane.getRowCount());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void switchScene(Node scene) {
       if (currentScene != scene) {
              gridPane.getChildren().remove(currentScene);
              gridPane.add(scene, 2, 0, 1, gridPane.getRowCount());
              currentScene = scene;
       }
    }

    public void btnDocumentAction() {
        switchScene(documentView);
    }

    public void btnEmployeesAction() {
        switchScene(employeeView);
    }

    public void btnMyProfileAction() {}

    public void userLoggedIn() {
        user = UserModel.getInstance().getLoggedInUser();

        if (user.getUserRole() == UserRole.ADMINISTRATOR) {
        }
    }
}
