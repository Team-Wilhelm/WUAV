package gui.controller;

import gui.controller.AddControllers.AddUserController;
import gui.controller.ViewControllers.CustomerInfoController;
import gui.controller.ViewControllers.DocumentController;
import gui.controller.ViewControllers.UserController;
import gui.model.UserModel;
import gui.nodes.NotificationBubble;
import gui.util.DialogManager;
import gui.util.SceneManager;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.css.themes.MFXThemeManager;
import io.github.palexdev.materialfx.css.themes.Themes;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Responsible for switching between the different scenes in the application.
 */
public class MenuController implements Initializable {
    @FXML
    private HBox btnCustomersBox;
    @FXML
    private GridPane gridPane;
    private Node documentView, employeeView, myProfileView, currentScene, customerView;
    private DocumentController documentController;
    private UserController userController;
    private AddUserController myProfileController;
    private CustomerInfoController customerController;
    private LoginViewController loginViewController;
    private MFXButton logOutButton;
    private NotificationBubble notificationBubble;
    private BooleanProperty customerChangedProperty = new SimpleBooleanProperty(false);
    private BooleanProperty customerDeletedProperty = new SimpleBooleanProperty(false);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FXMLLoader documentLoader = new FXMLLoader(getClass().getResource(SceneManager.DOCUMENT_SCENE));
        FXMLLoader employeeLoader = new FXMLLoader(getClass().getResource(SceneManager.EMPLOYEE_SCENE));
        FXMLLoader myProfileLoader = new FXMLLoader(getClass().getResource(SceneManager.ADD_EMPLOYEE_SCENE));
        FXMLLoader customerLoader = new FXMLLoader(getClass().getResource(SceneManager.CUSTOMER_SCENE));
        try {
            // Load the scenes
            documentView = documentLoader.load();
            employeeView = employeeLoader.load();
            myProfileView = myProfileLoader.load();
            customerView = customerLoader.load();

            // Get the controllers
            documentController = documentLoader.getController();
            userController = employeeLoader.getController();
            myProfileController = myProfileLoader.getController();
            myProfileController.setUserController(userController);
            customerController = customerLoader.getController();

            // Set the default scene
            currentScene = documentView;
            gridPane.add(currentScene, 2, 0, 1, gridPane.getRowCount());

            // Makes sure that any time a customer is saved, the notification bubble is updated
            customerChangedProperty.bind(documentController.customerChangedProperty());
            customerChangedProperty.addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    customerController.showAlmostExpiredCustomers();
                }
            });

            // If a customer is deleted, all the documents associated with that customer are deleted as well
            customerDeletedProperty.bind(customerController.customerDeletedProperty());
            customerDeletedProperty.addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    documentController.refreshItems();
                    customerController.customerDeletedProperty().set(false);
                }
            });

            // Set the notification bubble
            notificationBubble = new NotificationBubble();
            btnCustomersBox.getChildren().add(notificationBubble);
            notificationBubble.visibleProperty().bind(customerController.expiredCustomersProperty());
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
              customerController.showAlmostExpiredCustomers();
       }
    }

    private void switchScene(String fxmlPath) {
        if (fxmlPath.equals(SceneManager.LOGIN_SCENE)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Stage stage = (Stage) gridPane.getScene().getWindow();
                Scene scene = new Scene(loader.load());
                MFXThemeManager.addOn(scene, Themes.DEFAULT, Themes.LEGACY);
                stage.setScene(scene);
                stage.setMaximized(false);
                stage.setWidth(Screen.getPrimary().getBounds().getWidth() - 400);
                stage.setHeight(Screen.getPrimary().getBounds().getHeight() - 200);
                stage.centerOnScreen();
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
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

    public void btnCustomersAction() {
        customerController.deleteExpiredCustomers();
        switchScene(customerView);
    }

    /**
     * To perform these actions, the scene must be loaded first, therefore this method is called from the LoginViewController when the user logs in .
     * Also, the user must be logged in to be able to set them as the user to edit in MyProfileView.
     */
    public void userLogInAction(Scene scene) {
        setVisibilityForUserRole();
        myProfileController.setIsEditing(UserModel.getLoggedInUser());
        customerController.showAlmostExpiredCustomers();
    }

    private void setVisibilityForUserRole() {
        documentController.setVisibilityForUserRole();
        userController.setVisibilityForUserRole();
        customerController.setVisibilityForUserRole();
    }

    public void logOutAction(ActionEvent actionEvent) {
        DialogManager.getInstance().showConfirmation("Log out", "Are you sure you want to log out?", gridPane, () -> {
            UserModel.getInstance().logOut();
            switchScene(SceneManager.LOGIN_SCENE);
            loginViewController.unfocusAll();
        });
    }

    public void setLoginViewController(LoginViewController loginViewController) {
        this.loginViewController = loginViewController;
    }
}
