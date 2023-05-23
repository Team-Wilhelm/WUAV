package gui.nodes.dialogs;

import be.User;
import gui.model.UserModel;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.enums.ScrimPriority;
import javafx.beans.value.ChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Window;
import utils.HashPasswordHelper;
import utils.enums.UserRole;

import java.util.HashMap;
import java.util.Map;

public class PasswordDialog extends CustomDialog {
    private HBox columns;
    private VBox rows;
    private HashMap<PasswordType, MFXPasswordField> passwordFields;
    private MFXButton btnConfirm, btnCancel;
    private byte[] newPassword, newSalt;
    private int row = 1;
    private boolean passwordChanged;
    private HashPasswordHelper hashPasswordHelper;

    private User userToUpdate;
    private Window owner;
    private Pane ownerNode;

    enum PasswordType {
        OLD("Current password"),
        NEW("New password"),
        CONFIRM("Confirm password");

        private String text;
        PasswordType(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    public PasswordDialog() {
        super();
        passwordFields = new HashMap<>();
        hashPasswordHelper = new HashPasswordHelper();

        setUpContent();
        super.setContent(columns);
        this.setContent(super.getDialogContent());
        setUpDialogueWindow();
        addButtons();
    }

    public PasswordDialog(Window owner, Pane ownerNode, User userToUpdate) {
        this();
        this.owner = owner;
        this.ownerNode = ownerNode;
        this.userToUpdate = userToUpdate;
    }

    private void setUpDialogueWindow() {
        this.setContent(super.getDialogContent());
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setDraggable(true);
        setTitle("Change password");
        setOwnerNode(ownerNode);
        setScrimPriority(ScrimPriority.WINDOW);
        setScrimOwner(true);
    }

    private void addButtons() {
        btnConfirm = new MFXButton("Confirm");
        btnConfirm.setDisable(true);
        btnCancel = new MFXButton("Cancel");
        super.getDialogContent().addActions(
                Map.entry(btnConfirm, event -> {
                    passwordChanged = true;
                    byte[][] passwordHashAndSalt = hashPasswordHelper.hashPassword(passwordFields.get(PasswordType.NEW).getText());
                    newPassword = passwordHashAndSalt[0];
                    newSalt = passwordHashAndSalt[1];
                    this.close();
                }),
                Map.entry(btnCancel, event -> this.close())
        );
    }

    private void setUpContent() {
        // Set up the columns with regions to center the content
        columns = new HBox();
        columns.setSpacing(10);

        for (int i = 0; i < 2; i++) {
            Region region = new Region();
            HBox.setHgrow(region, Priority.SOMETIMES);
            columns.getChildren().add(region);
        }

        // Set up the rows with the password fields
        rows = new VBox();
        rows.setSpacing(10);
        rows.setPadding(new Insets(10));
        setUpTextFields();

        columns.getChildren().add(1, rows);
    }

    private void setUpTextFields() {
        // If the user is not an admin, add the current password field, otherwise start at the new password field
        for (int i = 0; i < PasswordType.values().length; i++) {
            PasswordType passwordType = PasswordType.values()[i];
            MFXPasswordField textField = new MFXPasswordField();
            textField.setFloatingText(passwordType.getText());
            textField.setMaxWidth(Double.MAX_VALUE);
            textField.textProperty().addListener(passwordListener);
            passwordFields.put(passwordType, textField);
            rows.getChildren().add(textField);
            VBox.setVgrow(textField, Priority.ALWAYS);
        }
    }

    // Add a listener to the text fields to check if the passwords match
    private final ChangeListener<String> passwordListener = (observable, oldValue, newValue) -> {
        if (passwordFields.get(PasswordType.NEW).getText().isEmpty() || passwordFields.get(PasswordType.CONFIRM).getText().isEmpty()) {
            btnConfirm.setDisable(true);
            return;
        }

        // If the new password and confirm password fields match, check if the old password field matches the current password
        if (passwordFields.get(PasswordType.NEW).getText().equals(passwordFields.get(PasswordType.CONFIRM).getText())) {
            if (UserModel.getLoggedInUser().getUserRole() != UserRole.ADMINISTRATOR) {
                if (hashPasswordHelper.hashPassword(passwordFields.get(PasswordType.OLD).getText(), userToUpdate.getSalt()) != userToUpdate.getPassword()) {
                    btnConfirm.setDisable(true);
                } else {
                    btnConfirm.setDisable(false);
                }
            } else {
                btnConfirm.setDisable(false);
            }
        }
    };


    public void clear() {
        passwordFields.values().forEach(MFXPasswordField::clear);
        userToUpdate = null;
    }

    @Override
    public void setContentText(String contentText) {
        super.getDialogContent().setContentText(contentText);
    }


    public byte[] getNewPassword() {
        return newPassword;
    }

    public byte[] getNewSalt() {
        return newSalt;
    }

    public boolean isPasswordChanged() {
        return passwordChanged;
    }

    public void setOwnerNode(Pane ownerNode) {
        this.ownerNode = ownerNode;
        super.setOwnerNode(ownerNode);
    }

    public void setUserToUpdate(User userToUpdate) {
        this.userToUpdate = userToUpdate;
    }

    public void setAdminEditing(boolean adminEditing) {
        if (adminEditing) {
            passwordFields.get(PasswordType.OLD).setVisible(false);
            passwordFields.get(PasswordType.OLD).setManaged(false);
        } else {
            passwordFields.get(PasswordType.OLD).setVisible(true);
            passwordFields.get(PasswordType.OLD).setManaged(true);
        }
    }
}
