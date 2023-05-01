package gui.controller.AddControllers;

import be.Document;
import be.User;
import be.enums.UserRole;
import gui.controller.ViewControllers.UserController;
import gui.model.IModel;
import gui.model.UserModel;
import gui.tasks.DeleteTask;
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
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import utils.HashPasswordHelper;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Stack;

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
        //profilePictureDoubleClick();
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
            if (!isUpdating) {
                closeWindow(actionEvent);
            }

            // If the user is updating, the save button is disabled after the save is complete
            isUpdating = false;
            btnSave.setDisable(true);
            disableFields(true);
            comboActions.getSelectionModel().clearSelection();

            // Save the user
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
        isUpdating = true;
        disableFields(false);
        btnSave.setDisable(false);
    }

    private void deleteUser(ActionEvent actionEvent) {
        Optional<ButtonType> result = alertManager.showConfirmation("Delete user", "Are you sure you want to delete this user?", txtName.getScene().getWindow());
        if (result.isPresent() && result.get().equals(ButtonType.OK)) {
            Task<TaskState> deleteTask = new DeleteTask<>(userToUpdate.getUserID(), userModel);
            setUpDeleteTask(deleteTask, userController, txtName.getScene().getWindow());
            executeTask(deleteTask);
        }
        closeWindow(actionEvent);
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
                    case DELETE -> deleteUser(new ActionEvent(txtName, txtName));
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
        disableFields(true);
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
        txtName.setEditable(!disable);
        txtUsername.setEditable(!disable);
        txtPassword.setEditable(!disable);
        txtPhoneNumber.setEditable(!disable);
        comboPosition.setDisable(disable);
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
    }

    private void populateComboboxes() {
        comboPosition.getItems().setAll(Arrays.stream(UserRole.values()).toList().subList(0, 4));
        comboActions.getItems().setAll(Action.EDIT, Action.DELETE);
    }

    /*private void profilePictureDoubleClick() {
        imgProfilePicture.setOnMouseClicked(event -> {
            if(event.getClickCount() == 2) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Choose profile picture");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.jpeg"),
                        new FileChooser.ExtensionFilter("All Files", "*.*"));
                File selectedFile = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
                if (selectedFile != null) {
                    Image image = new Image(selectedFile.toURI().toString());
                    GridPane root = new GridPane();
                    root.setPadding(new Insets(10));

                    ImageView imageView = new ImageView(image);
                    StackPane imagePane = new StackPane(imageView);

                    Rectangle cropRectangle = new Rectangle(300, 300);
                    cropRectangle.setFill(null);
                    cropRectangle.setStrokeWidth(2);
                    cropRectangle.setStroke(javafx.scene.paint.Color.RED);

                    cropRectangle.setOnMousePressed(this::startCrop);
                    cropRectangle.setOnMouseDragged(this::resizeCrop);
                    cropRectangle.setOnMouseReleased(this::endCrop);

                    imagePane.getChildren().add(cropRectangle);
                    root.add(imagePane, 0, 0);

                    Stage stage = new Stage();
                    stage.setTitle("Crop profile picture");
                    stage.setScene(new Scene(root, image.getWidth(), image.getHeight()));
                    stage.show();

                    imgProfilePicture.setImage(image);
                }
            }
        });
    }

     */

    //endregion
}
