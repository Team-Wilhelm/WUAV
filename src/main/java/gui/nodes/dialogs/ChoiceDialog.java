package gui.nodes.dialogs;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

import java.util.HashMap;
import java.util.Map;

public class ChoiceDialog extends CustomDialog {
    private HashMap<MFXButton, EventHandler<MouseEvent>> choices = new HashMap<>();

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
        choices.clear();
    }

    public void addChoice(String text, EventHandler<MouseEvent> action) {
        choices.put(new MFXButton(text), action);
    }

    public void setChoices(HashMap<String, EventHandler<MouseEvent>> actions) {
        this.choices.clear();
        for (Map.Entry<String, EventHandler<MouseEvent>> entry : actions.entrySet()) {
            this.choices.put(new MFXButton(entry.getKey()), entry.getValue());
        }
    }
}
