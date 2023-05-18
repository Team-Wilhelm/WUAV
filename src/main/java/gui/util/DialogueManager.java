package gui.util;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialogBuilder;
import io.github.palexdev.materialfx.dialogs.MFXStageDialog;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DialogueManager {
    //TODO optimize this class
    //TODO password dialog
    private MFXStageDialog dialog;
    private MFXGenericDialog dialogContent;
    private static DialogueManager instance = null;
    private MFXFontIcon warnIcon, errorIcon, infoIcon;
    private MFXButton btnConfirm, btnCancel;
    private CompletableFuture<ButtonType> result;

    private DialogueManager() {
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
    }

    /**
     * Makes DialogueManager a singleton class, in order to reuse the same alert and avoid code repetition
     */
    public static DialogueManager getInstance() {
        if (instance == null) {
            instance = new DialogueManager();
        }
        return instance;
    }

    public void showWarning(String header, String content, Pane parent) {
        convertDialogTo(Alert.AlertType.WARNING, header, content, parent);
        dialog.showDialog();
    }

    public void showError(String header, String content, Pane parent) {
        convertDialogTo(Alert.AlertType.ERROR, header, content, parent);
        dialog.showDialog();
    }

    public CompletableFuture<ButtonType> showConfirmation(String header, String content, Pane parent) {
        convertDialogTo(Alert.AlertType.CONFIRMATION, header, content, parent);
        dialog.showDialog();
        return result;
    }

    private void convertDialogTo(Alert.AlertType alertType, String header, String content, Pane parent) {
        String styleClass = null;

        dialogContent.setContentText(content);
        dialogContent.setHeaderText(header);

        if (alertType != Alert.AlertType.CONFIRMATION) {
            dialogContent.clearActions();
            dialogContent.addActions(
                    Map.entry(btnConfirm, event -> dialog.close()),
                    Map.entry(btnCancel, event -> dialog.close())
            );
        }

        if (dialog.getOwnerNode() != parent) {
            dialog.setOwnerNode(parent);
        }

        if (dialog.getOwner() == null) {
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
}
