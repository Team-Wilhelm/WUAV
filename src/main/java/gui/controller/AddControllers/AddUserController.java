package gui.controller.AddControllers;

import be.User;
import gui.nodes.PasswordDialogue;
import javafx.geometry.Pos;
import utils.enums.EditingOptions;
import utils.enums.UserRole;
import gui.controller.ViewControllers.UserController;
import gui.model.UserModel;
import gui.tasks.DeleteTask;
import gui.tasks.SaveTask;
import gui.tasks.TaskState;
import gui.util.DialogueManager;
import gui.util.CropImageToCircle;
import gui.util.ImageCropper;
import io.github.palexdev.materialfx.controls.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import utils.BlobService;
import utils.HashPasswordHelper;
import utils.ThreadPool;

import java.io.File;
import java.net.URL;
import java.util.*;

public class AddUserController extends AddController<User> implements Initializable {
    //TODO add dialogue for changing password
    //TODO ESC should revert editing mode
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
    private MFXComboBox<EditingOptions> comboOptions;
    @FXML
    private MFXButton btnSave;

    private final UserModel userModel;
    private final DialogueManager dialogueManager;
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
        dialogueManager = DialogueManager.getInstance();
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
        imgProfilePicture.setImage(CropImageToCircle.getRoundedImage(new Image(profilePicturePath)));

        btnSave.setDisable(true);
        comboOptions.setDisable(true);

        profilePictureDoubleClick();
        setUpComboBoxes();
        assignListenersToTextFields();

        btnSave.requestFocus();
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
            comboOptions.clearSelection();

            // Save the user
            assignInputToVariables();

            // If the user is editing and the password has changed, it's already been hashed and set in the User object
            byte[][] passwordHash = isEditing ? new byte[][] {userToUpdate.getPassword(), userToUpdate.getSalt()} : hashPasswordHelper.hashPassword(password);
            User user = new User(name, username, passwordHash, phoneNumber, userRole, profilePicturePath);
            SaveTask<User> saveTask = new SaveTask<>(user, isEditing, userModel);
            if (isEditing) {
                user.setUserID(userToUpdate.getUserID());
            }
            setUpSaveTask(saveTask, userController, txtName.getScene().getWindow(), this);
            executorService.execute(saveTask);
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
        Optional<ButtonType> result = dialogueManager.showConfirmation("Delete user", "Are you sure you want to delete this user?", txtName.getScene().getWindow());
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
            if (isInputEmpty(txtName) || isInputEmpty(txtUsername)
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
            if (isInputEmpty(txtName) || isInputEmpty(txtUsername)
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
        phoneNumber = txtPhoneNumber.getText().trim();
        userRole = comboPosition.getSelectionModel().getSelectedItem();
    }

    @Override
    protected void assignListenersToTextFields() {
        txtName.textProperty().addListener(inputListener);
        txtUsername.textProperty().addListener(inputListener);
        txtPhoneNumber.textProperty().addListener(inputListener);
    }

    public void setIsEditing(User user) {
        disableFields(true);
        isEditing = true;
        isUpdating.set(false);
        comboOptions.setDisable(false);
        txtPassword.setVisible(false);

        userToUpdate = user;
        txtName.setText(user.getFullName());
        txtUsername.setText(user.getUsername());
        txtPhoneNumber.setText(user.getPhoneNumber().isEmpty() ? "No phone number available" : user.getPhoneNumber());
        comboPosition.getSelectionModel().selectItem(user.getUserRole());
        imgProfilePicture.setImage(CropImageToCircle.getRoundedImage(user.getProfilePicture()));
        profilePicturePath = user.getProfilePicturePath();
    }

    private boolean checkInput() {
        if (userModel.getAll().values().stream().anyMatch(user -> user.getUsername().equals(username))
                && !Objects.equals(userToUpdate.getUsername(), username)) {
            dialogueManager.showError("Username already exists", "Please choose another username", txtName.getScene().getWindow());
            return false;
        }
        return true;
    }

    private void disableFields(boolean disable) {
        txtName.setEditable(!disable);
        txtUsername.setEditable(!disable);
        txtPhoneNumber.setEditable(!disable);
        comboPosition.setEditable(!disable);
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
    }

    private void setUpComboBoxes() {
        comboPosition.getItems().setAll(Arrays.stream(UserRole.values()).toList().subList(0, 4));
        comboPosition.getSelectionModel().selectedItemProperty().addListener(positionListener);

        comboOptions.getItems().setAll(EditingOptions.values());
        comboOptions.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == EditingOptions.EDIT) {
                editUserAction(null);
            } else if (newValue == EditingOptions.DELETE) {
                deleteUserAction(null);
            } else if (newValue == EditingOptions.CHANGE_PASSWORD) {
                showPasswordDialogue();
            }
        });
    }

    private void changePasswordAction() {
        showPasswordDialogue();
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
        if(loggedInUserRole == UserRole.ADMINISTRATOR || userToUpdate.equals(UserModel.getLoggedInUser())){
            hasAccess = true;
            if (loggedInUserRole != UserRole.ADMINISTRATOR) {
                comboOptions.getItems().remove(EditingOptions.DELETE);
            }
        }
        comboOptions.setVisible(hasAccess);
        btnSave.setVisible(hasAccess);
    }

    private void changeTextFieldStyle(boolean isUpdating) {
        if (!isUpdating) {
            // Make the text fields look like labels if they're not editable
            txtName.getStyleClass().add("not-editable");
            txtName.setFloatingText("");
            txtName.setAlignment(Pos.CENTER);
            //TODO Make the txtName resize to the size of the text, so it can be centered
            txtUsername.getStyleClass().add("not-editable");
            txtPhoneNumber.getStyleClass().add("not-editable");
            comboPosition.getStyleClass().add("not-editable");
            }
        else {
            // Revert the text fields to their original style
            txtName.getStyleClass().removeIf(s -> s.equals("not-editable"));
            txtName.setFloatingText("Name");
            txtName.setAlignment(Pos.BASELINE_LEFT);
            txtUsername.getStyleClass().removeIf(s -> s.equals("not-editable"));
            txtPhoneNumber.getStyleClass().removeIf(s -> s.equals("not-editable"));
            comboPosition.getStyleClass().removeIf(s -> s.equals("not-editable"));
        }
    }

    private void showPasswordDialogue() {
        PasswordDialogue passwordDialogue = new PasswordDialogue(txtName.getScene().getWindow(), gridPane, userToUpdate);
        passwordDialogue.showDialog();
        passwordDialogue.setOnHidden(event -> {
            if (passwordDialogue.isPasswordChanged()) {
                userToUpdate.setPassword(passwordDialogue.getNewPassword());
                userToUpdate.setSalt(passwordDialogue.getNewSalt());
                btnSaveAction(null);
            }
        });
    }
    //endregion
}
