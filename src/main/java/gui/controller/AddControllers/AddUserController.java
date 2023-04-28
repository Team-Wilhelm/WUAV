package gui.controller.AddControllers;

import be.Document;
import be.User;
import be.enums.UserRole;
import gui.controller.ViewControllers.UserController;
import gui.model.IModel;
import gui.model.UserModel;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXListView;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class AddUserController implements Initializable {
    @FXML
    private ImageView imgProfilePicture;
    @FXML
    private Label lblPosition;
    @FXML
    private MFXTextField txtName, txtUsername, txtPhoneNumber, txtPassword;
    @FXML
    private MFXComboBox<UserRole> comboPosition;
    @FXML
    private MFXComboBox<Action> comboActions;
    @FXML
    private MFXListView<Document> listViewDocuments;
    @FXML
    private MFXButton btnSave;

    private UserModel userModel;
    private User userToUpdate;
    private UserController userController;
    private boolean isEditing;
    private boolean isUpdating;
    private String name, username, password, phoneNumber;
    private UserRole userRole;
    private enum Action {
        EDIT, DELETE;

        @Override
        public String toString() {
            return super.toString().charAt(0) + super.toString().substring(1).toLowerCase();
        }

        public static Action fromString(String string) {
            return Action.valueOf(string.toUpperCase());
        }
    }

    public AddUserController() {
        userModel = UserModel.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        isEditing = false;
        isUpdating = false;
        comboActions.setDisable(true);
        comboActions.getSelectionModel().selectedItemProperty().addListener(actionListener);
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
    }

    public void setIsEditing(User user) {
        disableFields(false);
        comboActions.setDisable(false);
        isEditing = true;

        userToUpdate = user;
        txtName.setText(user.getFullName());
        txtUsername.setText(user.getUsername());
        txtPassword.setText(user.getPassword());
        comboPosition.getSelectionModel().selectItem(user.getUserRole());
        listViewDocuments.getItems().setAll(user.getAssignedDocuments());
    }

    private void populateComboboxes() {
        comboPosition.getItems().setAll(UserRole.values());
        comboActions.getItems().setAll(Action.EDIT, Action.DELETE);
    }

    private ChangeListener<Action> actionListener = new ChangeListener() {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            if (newValue != null) {
                switch ((Action) newValue) {
                    case EDIT -> editUser();
                    case DELETE -> deleteUser();
                }
                comboActions.getSelectionModel().clearSelection();
            }
        }
    };

    private void editUser() {
        disableFields(false);
    }

    private void deleteUser() {
    }

    private void disableFields(boolean disable) {
        txtName.setDisable(disable);
        txtUsername.setDisable(disable);
        txtPassword.setDisable(disable);
        txtPhoneNumber.setDisable(disable);
        comboPosition.setDisable(disable);
    }

    private boolean checkInput() {
        if ()

    }

    private void assignFieldsToVariables() {
        name = txtName.getText().trim();
        username = txtUsername.getText().trim();
        password = txtPassword.getText();
        phoneNumber = txtPhoneNumber.getText().trim();
        userRole = comboPosition.getSelectionModel().getSelectedItem();
    }
}
