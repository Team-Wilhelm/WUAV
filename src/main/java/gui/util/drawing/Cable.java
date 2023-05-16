package gui.util.drawing;

import javafx.scene.paint.Color;

public enum Cable {
    HDMI(Color.BLACK),
    ELWIRE(Color.RED),
    NULLWIRE(Color.BLUE),
    GROUNDWIRE(Color.GREEN);

    public final Color color;
    Cable(Color color) {
        this.color = color;
    }
}
