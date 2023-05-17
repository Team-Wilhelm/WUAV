package gui.controller.AddControllers;

import be.Document;
import be.User;
import be.enums.UserRole;
import gui.controller.ViewControllers.UserController;
import gui.model.UserModel;
import gui.tasks.DeleteTask;
import gui.tasks.SaveTask;
import gui.tasks.TaskState;
import gui.util.AlertManager;
import gui.util.CropImageToCircle;
import gui.util.ImageCropper;
import io.github.palexdev.materialfx.controls.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import utils.BlobService;
import utils.HashPasswordHelper;
import utils.ThreadPool;

import java.io.File;
import java.net.URL;
import java.util.*;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

public class AddUserController extends AddController<User> implements Initializable {
    @FXML
    private GridPane gridPane;
    @FXML
    private ImageView imgProfilePicture;
    @FXML
    private MFXTextField txtName, txtUsername, txtPhoneNumber;
    @FXML
    private MFXPasswordField txtPassword;
    @FXML
    private MFXComboBox<UserRole> comboPosition;
    @FXML
    private MFXButton btnSave, btnDelete, btnEdit;

    private final UserModel userModel;
    private final AlertManager alertManager;
    private HashPasswordHelper hashPasswordHelper;
    private User userToUpdate;
    private UserController userController;
    private boolean isEditing;
    private BooleanProperty isUpdating;
    private String name, username, password, phoneNumber, profilePicturePath;
    private Image profilePicture;
    private UserRole userRole;
    private final ThreadPool executorService;
    private boolean hasAccess = false;

    public AddUserController() {
        userModel = UserModel.getInstance();
        alertManager = AlertManager.getInstance();
        hashPasswordHelper = new HashPasswordHelper();
        executorService = ThreadPool.getInstance();

        isUpdating = new SimpleBooleanProperty(true);
        isUpdating.addListener((observable, oldValue, newValue) -> {
            changeTextFieldStyle(isUpdating.get());
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        isEditing = false;
        profilePicturePath = "/img/userIcon.png";

        btnSave.setDisable(true);
        btnDelete.setDisable(true);
        btnEdit.setDisable(true);

        profilePictureDoubleClick();
        populateCombobox();

        assignListenersToTextFields();
        comboPosition.getSelectionModel().selectedItemProperty().addListener(positionListener);

        btnSave.requestFocus();
        txtPassword.visibleProperty().bind(isUpdating);
    }

    @FXML
    private void btnSaveAction(ActionEvent actionEvent) {
        if (checkInput()) {
            if (!isUpdating.get()) {
                closeWindow(actionEvent);
            }

            // If the user is updating, the save button is disabled after the save is complete
            isUpdating.set(false);
            btnSave.setDisable(true);
            disableFields(true);

            // Save the user
            assignInputToVariables();
            byte[][] passwordHash = hashPasswordHelper.hashPassword(password);
            User user = new User(name, username, passwordHash, phoneNumber, userRole, profilePicturePath);
            SaveTask<User> saveTask = new SaveTask<>(user, isEditing, userModel);
            if (isEditing) {
                user.setUserID(userToUpdate.getUserID());
            }
            setUpSaveTask(saveTask, userController, txtName.getScene().getWindow(), this);
            executorService.execute(saveTask);
            //userToUpdate = user;
        }
    }

    @FXML
    private void editUserAction(ActionEvent actionEvent) {
        isUpdating.set(true);
        disableFields(false);
        btnSave.setDisable(false);
    }

    @FXML
    private void deleteUserAction(ActionEvent actionEvent) {
        Optional<ButtonType> result = alertManager.showConfirmation("Delete user", "Are you sure you want to delete this user?", txtName.getScene().getWindow());
        if (result.isPresent() && result.get().equals(ButtonType.OK)) {
            Task<TaskState> deleteTask = new DeleteTask<>(userToUpdate.getUserID(), userModel);
            setUpDeleteTask(deleteTask, userController, txtName.getScene().getWindow());
            executorService.execute(deleteTask);
            closeWindow(actionEvent);
        }
    }

    private void profilePictureDoubleClick() {
        imgProfilePicture.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && isUpdating.get()) {
                ImageCropper imageCropper = new ImageCropper(this);
                imageCropper.chooseImage(userToUpdate);
            }
        });
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
        disableFields(true);
        isEditing = true;
        isUpdating.set(false);
        btnEdit.setDisable(false);
        btnDelete.setDisable(false);

        userToUpdate = user;
        txtName.setText(user.getFullName());
        txtUsername.setText(user.getUsername());
        txtPassword.setPromptText("Leave empty to keep current password");
        txtPhoneNumber.setText(user.getPhoneNumber().isEmpty() ? "No phone number available" : user.getPhoneNumber());
        comboPosition.getSelectionModel().selectItem(user.getUserRole());
        imgProfilePicture.setImage(CropImageToCircle.getRoundedImage(user.getProfilePicture()));
        profilePicturePath = user.getProfilePicturePath();
    }

    private boolean checkInput() {
        if (userModel.getAll().values().stream().anyMatch(user -> user.getUsername().equals(username))
                && !Objects.equals(userToUpdate.getUsername(), username)) {
            alertManager.showError("Username already exists", "Please choose another username", txtName.getScene().getWindow());
            return false;
        }
        return true;
    }

    private void disableFields(boolean disable) {
        txtName.setEditable(!disable);
        txtUsername.setEditable(!disable);
        txtPassword.setEditable(!disable);
        txtPhoneNumber.setEditable(!disable);
        comboPosition.setEditable(!disable);
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
    }

    private void populateCombobox() {
        comboPosition.getItems().setAll(Arrays.stream(UserRole.values()).toList().subList(0, 4));
    }

    public void setProfilePicture(Image image, String profilePicturePath) throws Exception {
        profilePicture = image;
        imgProfilePicture.setImage(CropImageToCircle.getRoundedImage(profilePicture));

        // Save the picture to blob service
        UUID id = isEditing ? userToUpdate.getUserID() : UUID.randomUUID();
        this.profilePicturePath = BlobService.getInstance().UploadFile(profilePicturePath, id);

        // Delete the physical file
        File file = new File(profilePicturePath);
        file.delete();
    }

    public void setVisibilityForUserRole() {
        UserRole loggedInUserRole = UserModel.getLoggedInUser().getUserRole();
        if(loggedInUserRole == UserRole.ADMINISTRATOR){
            hasAccess = true;
        }
        btnDelete.setVisible(hasAccess);

        if(loggedInUserRole == UserRole.ADMINISTRATOR || userToUpdate.equals(UserModel.getLoggedInUser())){
            hasAccess = true;
        }
        btnEdit.setVisible(hasAccess);
        btnSave.setVisible(hasAccess);
    }

    private void changeTextFieldStyle(boolean isUpdating) {
        if (!isUpdating) {
            // Make the text fields look like labels if they're not editable
            txtName.getStyleClass().add("not-editable");
            txtUsername.getStyleClass().add("not-editable");
            txtPassword.getStyleClass().add("not-editable");
            txtPhoneNumber.getStyleClass().add("not-editable");
            comboPosition.getStyleClass().add("not-editable");
            }
        else {
            // Revert the text fields to their original style
            txtName.getStyleClass().removeIf(s -> s.equals("not-editable"));
            txtUsername.getStyleClass().removeIf(s -> s.equals("not-editable"));
            txtPassword.getStyleClass().removeIf(s -> s.equals("not-editable"));
            txtPhoneNumber.getStyleClass().removeIf(s -> s.equals("not-editable"));
            comboPosition.getStyleClass().removeIf(s -> s.equals("not-editable"));
        }
    }
    //endregion
}
