package gui.util;

import gui.nodes.dialogs.*;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialogBuilder;
import io.github.palexdev.materialfx.dialogs.MFXStageDialog;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

/**
 * A manager class for all the alerts and dialogues in the application
 */
public class DialogManager {
    private static DialogManager instance = null;

    // Alerts
    private MFXStageDialog dialog; // reusing the same dialog for all alerts
    private MFXGenericDialog dialogContent;
    private MFXFontIcon warnIcon, errorIcon, infoIcon;
    private MFXButton btnConfirm, btnCancel;
    private CompletableFuture<ButtonType> result;

    // Dialogues
    private PasswordDialog passwordDialog;
    private TextInputDialog textInputDialog;
    private LoadingDialog loadingDialog;
    private ChoiceDialog choiceDialog;

    private DialogManager() {
        dialog = MFXGenericDialogBuilder.build()
                .toStageDialogBuilder()
                .setDraggable(true)
                .get();

        dialogContent = MFXGenericDialogBuilder.build()
                .setOnMinimize(event -> dialog.setIconified(true))
                .setOnClose(event -> dialog.close())
                .setOnAlwaysOnTop(event -> dialog.setAlwaysOnTop(true))
                .get();

        dialog.setContent(dialogContent);

        warnIcon = new MFXFontIcon("fas-circle-exclamation", 18);
        errorIcon = new MFXFontIcon("fas-circle-xmark", 18);
        infoIcon = new MFXFontIcon("fas-circle-info", 18);

        btnConfirm = new MFXButton("Confirm");
        btnCancel = new MFXButton("Cancel");
        result = new CompletableFuture<>();

        passwordDialog = new PasswordDialog();
        textInputDialog = new TextInputDialog();
        loadingDialog = new LoadingDialog();
        choiceDialog = new ChoiceDialog();
    }

    /**
     * Makes DialogueManager a singleton class, in order to reuse the same alert and avoid code repetition
     */
    public static DialogManager getInstance() {
        if (instance == null) {
            instance = new DialogManager();
        }
        return instance;
    }


    // region Alerts
    public void showWarning(String header, String content, Pane parent) {
        convertDialogTo(Alert.AlertType.WARNING, header, content, parent);
        dialog.showDialog();
    }

    public void showError(String header, String content, Pane parent) {
        convertDialogTo(Alert.AlertType.ERROR, header, content, parent);
        dialog.showDialog();
    }

    public void showInformation(String header, String content, Pane parent) {
        convertDialogTo(Alert.AlertType.INFORMATION, header, content, parent);
        dialog.showDialog();
    }

    public CompletableFuture<ButtonType> showConfirmation(String header, String content, Pane parent) {
        convertDialogTo(Alert.AlertType.CONFIRMATION, header, content, parent);
        dialog.showDialog();
        return result;
    }

    public void showConfirmation(String header, String content, Pane parent, Runnable onConfirm) {
        convertDialogTo(Alert.AlertType.CONFIRMATION, header, content, parent);
        dialog.showDialog();
        result.thenAccept(buttonType -> {
            if (buttonType == ButtonType.OK) {
                onConfirm.run();
            }
        });
    }
    // endregion

    // region Custom Dialogs
    public CompletableFuture<String> showTextInputDialog(String header, String contentDescription, String contentValue, Pane parent, boolean editable) {
        setUpCustomDialog(textInputDialog, parent, header, contentDescription);
        textInputDialog.setContentText(contentValue);
        textInputDialog.setEditable(editable);
        return textInputDialog.showAndReturnResult();
    }

    public PasswordDialog getPasswordDialog(Pane parent) {
        setUpCustomDialog(passwordDialog, parent);
        return passwordDialog;
    }

    public void showLoadingDialog(String title, String content, Pane parent, Task<?> task, Runnable onFinished) {
        setUpCustomDialog(loadingDialog, parent, title, content);
        loadingDialog.setProgressLabel(content);
        loadingDialog.progressProperty().bind(task.progressProperty());
        loadingDialog.setOnCloseRequest(event -> task.cancel());

        task.setOnSucceeded(event -> {
            loadingDialog.progressProperty().unbind();
            loadingDialog.setProgress(100);
            loadingDialog.setProgressLabel("Done!");
            onFinished.run();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        loadingDialog.close();
                    });
                }
            }, 1000);
        });
        loadingDialog.showDialog();
    }

    public void showChoiceDialog(String title, String content, Pane parent, HashMap<String, Runnable> choices) {
        setUpCustomDialog(choiceDialog, parent, title, content);
        choiceDialog.setChoices(choices);
        choiceDialog.showDialog();
    }

    // endregion

    // region Utilities

    /**
     * Sets up an MFXStageDialog (with MFXGenericDialog as content) to be used as a classic alert.
     * @param alertType the desired alert type
     * @param header the header text
     * @param content the content text
     * @param parent the pane that will be used as the owner of the alert
     */
    private void convertDialogTo(Alert.AlertType alertType, String header, String content, Pane parent) {
        result = new CompletableFuture<>();
        String styleClass = null;

        dialogContent.setContentText(content);
        dialogContent.setHeaderText(header);

        dialogContent.clearActions();
        dialogContent.addActions(
                Map.entry(btnConfirm, event -> dialog.close()),
                Map.entry(btnCancel, event -> dialog.close())
        );

        if (dialog.getOwnerNode() != parent) {
            dialog.setOwnerNode(parent);
        }

        if (dialog.getOwner() == null) {
            dialog.initOwner(parent.getScene().getWindow());
        } else if (dialog.getOwner() != parent.getScene().getWindow()) {
            dialog = MFXGenericDialogBuilder.build()
                    .toStageDialogBuilder()
                    .setDraggable(true)
                    .get();
            dialogContent = MFXGenericDialogBuilder.build()
                    .setOnMinimize(event -> dialog.setIconified(true))
                    .setOnClose(event -> dialog.close())
                    .setOnAlwaysOnTop(event -> dialog.setAlwaysOnTop(true))
                    .get();
            dialog.setContent(dialogContent);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(parent.getScene().getWindow());
        }

        switch (alertType) {
            case ERROR -> {
                dialogContent.setHeaderIcon(errorIcon);
                styleClass = "mfx-error-dialog";
            }
            case INFORMATION -> {
                dialogContent.setHeaderIcon(infoIcon);
                styleClass = "mfx-info-dialog";
            }
            case WARNING -> {
                dialogContent.setHeaderIcon(warnIcon);
                styleClass = "mfx-warn-dialog";
            }
            case CONFIRMATION -> {
                dialogContent.setHeaderIcon(null);
                dialogContent.clearActions();
                dialogContent.addActions(
                        Map.entry(btnConfirm, event -> {
                            dialog.close();
                            result.complete(ButtonType.OK);
                        }),
                        Map.entry(btnCancel, event -> {
                            dialog.close();
                            result.complete(ButtonType.CANCEL);
                        })
                );

                dialog.setOnCloseRequest(event -> {
                    if (!result.isDone()) {
                        // If the dialog is closed without selecting any option, consider it as canceled
                        result.complete(ButtonType.CANCEL);
                    }
                });
            }
        }

        dialogContent.getStyleClass().removeIf(
                s -> s.equals("mfx-info-dialog") || s.equals("mfx-warn-dialog") || s.equals("mfx-error-dialog"));
        if (styleClass != null)
            dialogContent.getStyleClass().add(styleClass);
    }

    private void setUpCustomDialog(CustomDialog dialog, Pane parent) {
        dialog.clear();

        if (dialog.getOwnerNode() != parent) {
            dialog.setOwnerNode(parent);
        }

        if (dialog.getOwner() == null) {
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(parent.getScene().getWindow());
        }
    }

    private void setUpCustomDialog(CustomDialog dialog, Pane parent, String title, String content) {
        setUpCustomDialog(dialog, parent);
        dialog.setTitleText(title);
        dialog.setContentText(content);
    }
    // endregion
}
