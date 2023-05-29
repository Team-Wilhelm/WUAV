package gui.nodes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Notification bubble that is displayed on customers button in the menu when there are expiring customers.
 */
public class NotificationBubble extends StackPane {
    private Circle bubble;

    public NotificationBubble() {
        bubble = new Circle(7);
        bubble.setFill(Color.RED);
        bubble.setStroke(Color.WHITE);

        getChildren().add(bubble);
        StackPane.setMargin(bubble, new Insets(0, 0, 0, 0));
        StackPane.setAlignment(bubble, Pos.CENTER_RIGHT);
    }
}
