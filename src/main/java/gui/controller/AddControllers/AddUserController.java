package gui.controller.AddControllers;

import be.User;
import gui.nodes.dialogs.PasswordDialog;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import utils.enums.EditingOptions;
import utils.enums.UserRole;
import gui.controller.ViewControllers.UserController;
import gui.model.UserModel;
import gui.tasks.DeleteTask;
import gui.tasks.SaveTask;
import utils.enums.ResultState;
import gui.util.DialogManager;
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
import java.util.concurrent.CompletableFuture;

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
    private MFXComboBox<EditingOptions> comboOptions;
    @FXML
    private MFXButton btnSave;

    private final UserModel userModel;
    private final DialogManager dialogManager;
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
        dialogManager = DialogManager.getInstance();
        hashPasswordHelper = new HashPasswordHelper();
        executorService = ThreadPool.getInstance();

        isUpdating = new SimpleBooleanProperty(true);

        isUpdating.addListener((observable, oldValue, newValue) -> {
            changeTextFieldStyle();
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        isEditing = false;
        profilePicturePath = "/img/icons/userIcon.png";
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
            comboOptions.getSelectionModel().clearSelection();
            isUpdating.set(false);
            btnSave.setDisable(true);
            disableFields(true);

            // Save the user
            assignInputToVariables();

            // If the user is editing and the password has changed, it's already been hashed and set in the User object
            byte[][] passwordHash = isEditing ? new byte[][] {userToUpdate.getPassword(), userToUpdate.getSalt()} : hashPasswordHelper.hashPassword(password);
            User user = new User(name, username, passwordHash, phoneNumber, userRole, profilePicturePath);
            SaveTask<User> saveTask = new SaveTask<>(user, isEditing, userModel);
            if (isEditing) {
                user.setUserID(userToUpdate.getUserID());
            }
            setUpSaveTask(saveTask, userController, gridPane, this);
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
        CompletableFuture<ButtonType> result = dialogManager.showConfirmation("Delete user", "Are you sure you want to delete this user?", gridPane);
        result.thenAccept(r -> {
            if (r.equals(ButtonType.OK)) {
                Task<ResultState> deleteTask = new DeleteTask<>(userToUpdate.getUserID(), userModel);
                setUpDeleteTask(deleteTask, userController, gridPane);
                executorService.execute(deleteTask);
                closeWindow(gridPane);
            }
        });
    }

    private void profilePictureDoubleClick() {
        imgProfilePicture.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && isUpdating.get()) {
                ImageCropper imageCropper = new ImageCropper(this);
                imageCropper.chooseImage(userToUpdate);
            }
        });
    }

    private void showPasswordDialogue() {
        comboOptions.getSelectionModel().clearSelection();
        PasswordDialog passwordDialog = DialogManager.getInstance().getPasswordDialogue(gridPane);
        passwordDialog.setUserToUpdate(userToUpdate);
        passwordDialog.setAdminEditing(UserModel.getLoggedInUser().getUserRole() == UserRole.ADMINISTRATOR);
        passwordDialog.showDialog();
        passwordDialog.setOnHidden(event -> {
            if (passwordDialog.isPasswordChanged()) {
                isUpdating.set(true);
                userToUpdate.setPassword(passwordDialog.getNewPassword());
                userToUpdate.setSalt(passwordDialog.getNewSalt());
                btnSaveAction(null);
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
        password = txtPassword.getText().trim();
    }

    @Override
    protected void assignListenersToTextFields() {
        txtName.textProperty().addListener(inputListener);
        txtUsername.textProperty().addListener(inputListener);
        txtPhoneNumber.textProperty().addListener(inputListener);
        txtPassword.textProperty().addListener(inputListener);
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
            dialogManager.showError("Username already exists", "Please choose another username", gridPane);
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

        if (!hasAccess)
            gridPane.getRowConstraints().get(gridPane.getRowCount() -1).setPercentHeight(0); // TODO If the user cannot edit, remove dead space
    }

    private void changeTextFieldStyle() {
        if (!isUpdating.get()) {
            // Make the text fields look like labels if they're not editable
            txtName.getStyleClass().add("not-editable");
            txtName.setFloatingText("");
            txtName.setAlignment(Pos.CENTER);
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

    public void setShortcutsAndAccelerators() {
        setShortcutsAndAccelerators(txtName.getScene());
    }

    /**
     * Sets the shortcuts and accelerators for the scene
     * @param scene The scene must be not null
     */
    public void setShortcutsAndAccelerators(Scene scene) {
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN), () -> {
            if (!btnSave.isDisabled()) {
                btnSaveAction(null);
            }
        });

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN), () -> {
            if (!comboOptions.isDisabled()) {
                deleteUserAction(null);
            }
        });

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                if (isEditing) {
                    setIsEditing(userToUpdate);
                    comboOptions.getSelectionModel().clearSelection();
                } else {
                    closeWindow(event);
                }
            }
        });

        // If creating a new user, set the focus to the first text field
        if (!isEditing) {
            Platform.runLater(() -> txtName.requestFocus());
        } else { // Otherwise, set the focus to the combo box, as it makes the text field styled as a label look weird
            Platform.runLater(() -> comboOptions.requestFocus());
        }
    }

    public void refreshCard() {
        userController.refreshCard(userToUpdate);
    }
    //endregion
}
