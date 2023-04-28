package gui.controller.AddControllers;

import be.Document;
import be.User;
import be.enums.UserRole;
import gui.controller.ViewControllers.UserController;
import gui.model.UserModel;
import gui.tasks.SaveTask;
import gui.tasks.TaskState;
import gui.util.AlertManager;
import io.github.palexdev.materialfx.controls.*;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import utils.HashPasswordHelper;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class AddUserController extends AddController implements Initializable {
    @FXML
    private ImageView imgProfilePicture;
    @FXML
    private Label lblPosition;
    @FXML
    private MFXTextField txtName, txtUsername, txtPhoneNumber;
    @FXML
    private MFXPasswordField txtPassword;
    @FXML
    private MFXComboBox<UserRole> comboPosition;
    @FXML
    private MFXComboBox<Action> comboActions;
    @FXML
    private MFXListView<Document> listViewDocuments;
    @FXML
    private MFXButton btnSave;

    private final UserModel userModel;
    private final AlertManager alertManager;
    private HashPasswordHelper hashPasswordHelper;
    private User userToUpdate;
    private UserController userController;
    private boolean isEditing;
    private boolean isUpdating;
    private String name, username, password, phoneNumber;
    private UserRole userRole;

    public AddUserController() {
        userModel = UserModel.getInstance();
        alertManager = AlertManager.getInstance();
        hashPasswordHelper = new HashPasswordHelper();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        isEditing = false;
        isUpdating = false;
        btnSave.setDisable(true);
        comboActions.setDisable(true);
        populateComboboxes();
        assignListenersToTextFields();
        comboActions.getSelectionModel().selectedItemProperty().addListener(actionListener);
        comboPosition.getSelectionModel().selectedItemProperty().addListener(positionListener);

        // Bind the text of the label displaying an employee's role to the selected item of the combo box
        lblPosition.textProperty().bind(Bindings.createStringBinding(() -> {
            if (comboPosition.getSelectionModel().getSelectedItem() == null) {
                return "";
            }
            return comboPosition.getSelectionModel().getSelectedItem().toString();
        }, comboPosition.getSelectionModel().selectedItemProperty()));
    }

    @FXML
    private void btnSaveAction(ActionEvent actionEvent) {
        if (checkInput()) {
            assignInputToVariables();
            byte[] passwordHash = hashPasswordHelper.hashPassword(password);
            User user = new User(name, username, passwordHash, phoneNumber, userRole);
            Task<TaskState> saveTask = new SaveTask<>(user, isEditing, userModel);
            if (isEditing) {
                user.setUserID(userToUpdate.getUserID());
            }
            setUpSaveTask(saveTask, userController, txtName.getScene().getWindow());
            executeTask(saveTask);
        }
    }


    private void editUser() {
        disableFields(false);
    }

    private void deleteUser() {
    }

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

    // region Listeners

    /**
     * Disables the save button if any of the required text fields are empty.
     */
    private final ChangeListener<String> inputListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            if (isInputEmpty(txtName) || isInputEmpty(txtUsername) || (isInputEmpty(txtPassword) && !isEditing)
                    || comboPosition.getValue() == null) {
                btnSave.setDisable(true);
            } else {
                btnSave.setDisable(false);
            }
        }
    };

    private final ChangeListener<UserRole> positionListener = new ChangeListener<UserRole>() {
        @Override
        public void changed(ObservableValue<? extends UserRole> observable, UserRole oldValue, UserRole newValue) {
            if (isInputEmpty(txtName) || isInputEmpty(txtUsername) || (isInputEmpty(txtPassword) && !isEditing)
                    || comboPosition.getValue() == null) {
                btnSave.setDisable(true);
            } else {
                btnSave.setDisable(false);
            }
        }
    };

    /**
     * Listens for changes in the combo box and performs the selected action.
     */
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
    // endregion

    // region Utilities, helpers and setters
    @Override
    protected void assignInputToVariables() {
        name = txtName.getText().trim();
        username = txtUsername.getText().trim();
        password = txtPassword.getText();
        phoneNumber = txtPhoneNumber.getText().trim();
        userRole = comboPosition.getSelectionModel().getSelectedItem();
    }

    @Override
    protected void assignListenersToTextFields() {
        txtName.textProperty().addListener(inputListener);
        txtUsername.textProperty().addListener(inputListener);
        txtPassword.textProperty().addListener(inputListener);
        txtPhoneNumber.textProperty().addListener(inputListener);
    }

    public void setIsEditing(User user) {
        disableFields(false);
        comboActions.setDisable(false);
        isEditing = true;

        userToUpdate = user;
        txtName.setText(user.getFullName());
        txtUsername.setText(user.getUsername());
        txtPassword.setPromptText("Leave empty to keep current password");
        comboPosition.getSelectionModel().selectItem(user.getUserRole());
        listViewDocuments.getItems().setAll(user.getAssignedDocuments());
    }

    private boolean checkInput() {
        if (userModel.getAll().values().stream().anyMatch(user -> user.getUsername().equals(username))) {
            alertManager.showError("Username already exists", "Please choose another username", txtName.getScene().getWindow());
            return false;
        }
        return true;
    }

    private void disableFields(boolean disable) {
        txtName.setDisable(disable);
        txtUsername.setDisable(disable);
        txtPassword.setDisable(disable);
        txtPhoneNumber.setDisable(disable);
        comboPosition.setDisable(disable);
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
    }

    private void populateComboboxes() {
        comboPosition.getItems().setAll(Arrays.stream(UserRole.values()).toList().subList(0, 4));
        comboActions.getItems().setAll(Action.EDIT, Action.DELETE);
    }

    //endregion
}
