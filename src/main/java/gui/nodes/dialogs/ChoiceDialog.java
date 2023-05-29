package gui.nodes.dialogs;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * A dialogue that allows the user to choose between multiple options.
 */
public class ChoiceDialog extends CustomDialog {

    public ChoiceDialog() {
        this("", "", new HashMap<>());
    }

    public ChoiceDialog(String title, String content, HashMap<String, EventHandler<MouseEvent>> actions) {
        super(title, content);
        super.setContent(super.getDialogContent());

        // Create buttons and assigned the custom actions to them
        for (Map.Entry<String, EventHandler<MouseEvent>> entry : actions.entrySet()) {
            MFXButton button = new MFXButton(entry.getKey());
            button.addEventHandler(MouseEvent.MOUSE_CLICKED, entry.getValue());
            super.getDialogContent().addActions(Map.entry(button, entry.getValue()));
        }
    }

    @Override
    public void clear() {
        super.getDialogContent().clearActions();
    }

    public void addChoice(String text, EventHandler<MouseEvent> action) {
        super.getDialogContent().addActions(Map.entry(new MFXButton(text), action));
    }

    public void setChoices(HashMap<String, Runnable> actions) {
        super.getDialogContent().clearActions();
        for (Map.Entry<String, Runnable> entry : actions.entrySet()) {
            super.getDialogContent().addActions(
                    Map.entry(new MFXButton(entry.getKey()),
                    event -> {
                        entry.getValue().run();
                        close();
                    }
            ));
        }
    }
}
