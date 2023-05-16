package gui.util.drawing;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.Arrays;

/**
 * A rectangle object.
 */
public class Rectangle extends MyShape {

    /**
     * Constructs a rectangle object.
     */
    public Rectangle() {
        points = Arrays.asList(null, null);
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (points.get(0) == null || points.get(1) == null)
            return;

        gc.setFill(color);
        javafx.scene.shape.Rectangle bound = getBound();

        if (selected) {
            double[] dashes = {5, 5}; // Adjust the dash pattern as desired
            gc.setLineDashes(dashes);
            gc.setStroke(Color.BLACK);
            gc.strokeRect(
                    bound.getX() - borderSpacing,
                    bound.getY() - borderSpacing,
                    bound.getWidth() + 2 * borderSpacing,
                    bound.getHeight() + 2 * borderSpacing
            );
            gc.setLineDashes(null); // Reset the line dashes
        }

        gc.fillRect(bound.getX(), bound.getY(), bound.getWidth(), bound.getHeight());
    }

    @Override
    public void handle(MouseEvent e) {
        Point2D currentPoint = new Point2D(roundToNearestMultiple(e.getX(), 10), roundToNearestMultiple(e.getY(), 10));
        if (e.getEventType() == MouseEvent.MOUSE_PRESSED)
            points.set(0, currentPoint);
        else if (e.getEventType() == MouseEvent.MOUSE_DRAGGED)
            points.set(1, currentPoint);
        else if (e.getEventType() == MouseEvent.MOUSE_RELEASED)
            didFinishDrawingCallback.run();
    }

    @Override
    public void handle(KeyEvent e) {

    }
}
