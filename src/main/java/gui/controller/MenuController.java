package gui.controller;

import be.User;
import gui.controller.AddControllers.AddController;
import gui.controller.AddControllers.AddUserController;
import gui.model.UserModel;
import gui.util.SceneManager;
import gui.controller.ViewControllers.DocumentController;
import gui.controller.ViewControllers.UserController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class MenuController implements Initializable {
    @FXML
    private GridPane gridPane;
    private Node documentView, employeeView, myProfileView, currentScene;
    private DocumentController documentController;
    private UserController userController;
    private AddUserController myProfileController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FXMLLoader documentLoader = new FXMLLoader(getClass().getResource(SceneManager.DOCUMENT_SCENE));
        FXMLLoader employeeLoader = new FXMLLoader(getClass().getResource(SceneManager.EMPLOYEE_SCENE));
        FXMLLoader myProfileLoader = new FXMLLoader(getClass().getResource(SceneManager.ADD_EMPLOYEE_SCENE));
        try {
            // Load the scenes
            documentView = documentLoader.load();
            employeeView = employeeLoader.load();
            myProfileView = myProfileLoader.load();

            // Get the controllers
            documentController = documentLoader.getController();
            userController = employeeLoader.getController();
            myProfileController = myProfileLoader.getController();
            myProfileController.setUserController(userController);

            // Set the default scene
            currentScene = documentView;
            gridPane.add(currentScene, 2, 0, 1, gridPane.getRowCount());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void switchScene(Node scene) {
       if (currentScene != scene) {
              gridPane.getChildren().remove(currentScene);
              gridPane.add(scene, 2, 0, 1, gridPane.getRowCount());
              currentScene = scene;
              if (scene == documentView) {
                  documentController.addShortcuts();
              }
       }
    }

    public void btnDocumentAction() {
        switchScene(documentView);
    }

    public void btnEmployeesAction() {
        switchScene(employeeView);
    }

    public void btnMyProfileAction() {
        switchScene(myProfileView);
    }

    /**
     * To perform these actions, the scene must be loaded first, therefore this method is called from the LoginViewController when the user logs in .
     * Also, the user must be logged in to be able to set them as the user to edit in MyProfileView.
     */
    public void userLogInAction(Scene scene) {
        setVisibilityForUserRole();
        //TODO may cause issues with other views (as they are not really scenes)
        myProfileController.setShortcutsAndAccelerators(scene);   // Because the scene already exists, we can add the shortcuts here
        myProfileController.setIsEditing(UserModel.getLoggedInUser());
    }

    private void setVisibilityForUserRole() {
        documentController.setVisibilityForUserRole();
        userController.setVisibilityForUserRole();
    }
}
