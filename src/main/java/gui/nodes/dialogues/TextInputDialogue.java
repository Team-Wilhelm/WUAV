package gui.nodes.dialogues;

import gui.nodes.textControls.TextAreaWithFloatingText;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialogBuilder;
import io.github.palexdev.materialfx.dialogs.MFXStageDialog;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * A dialogue that allows the user to input text
 * with the initial max length of 256 characters.
 */
public class TextInputDialogue extends MFXStageDialog {
    private TextAreaWithFloatingText textArea;
    private MFXGenericDialog dialogContent;
    private CompletableFuture<String> result = new CompletableFuture<>();

    public TextInputDialogue() {
        this("", "");
    }

    public TextInputDialogue(String title, String content) {
        super();
        textArea = new TextAreaWithFloatingText();
        //textArea.setMaxTextLength(256);

        dialogContent = MFXGenericDialogBuilder.build()
                .setContent(textArea)
                .setOnMinimize(event -> this.setIconified(true))
                .setOnAlwaysOnTop(event -> this.setAlwaysOnTop(true))
                .setOnClose(event -> this.close())
                .setHeaderText(title)
                .setContentText(content)
                .get();

        dialogContent.addActions(
                Map.entry(new MFXButton("Confirm"), event -> {
                    result.complete(textArea.getText());
                    this.close();
                }),
                Map.entry(new MFXButton("Cancel"), event -> this.close())
        );
        dialogContent.getStylesheets().add("/css/style.css");
        this.setContent(dialogContent);
    }

   /* public void setMaxTextLength(int length) {
        textArea.setMaxTextLength(length);
    }*/

    public void setFloatingText(String text) {
        textArea.setFloatingText(text);
    }

    public void setEditorText(String text) {
        textArea.setText(text);
    }

    public String getEditorText() {
        return textArea.getText();
    }

    public CompletableFuture<String> showAndReturnResult() {
        CompletableFuture<String> result = new CompletableFuture<>();
        this.show();
        return result;
    }

    public void setEditable(boolean isEditable) {
        textArea.setEditable(isEditable);
    }

    public void clear() {
        textArea.getTextArea().clear();
    }

    public void setContentText(String text) {
        textArea.setText(text);
    }

    public void setContentDescription(String text) {
        dialogContent.setContentText(text);
    }

    public void setHeaderText(String text) {
        dialogContent.setHeaderText(text);
    }

    public CompletableFuture<String> getResult() {
        return result;
    }
}
