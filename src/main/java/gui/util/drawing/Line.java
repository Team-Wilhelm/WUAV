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
public class Line extends MyShape {

    private Color color;
    private boolean isVertical;
    private boolean isHorizontal;
    /**
     * Constructs a rectangle object.
     */
    public Line(Color color) {
        this.color = color;
        points = Arrays.asList(null, null);
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (points.get(0) == null || points.get(1) == null)
            return;

        gc.setFill(color);
        javafx.scene.shape.Rectangle bound = getBound();
        var size = 5;
        if(selected != true) {
            if (isVertical)
                bound = new javafx.scene.shape.Rectangle(points.get(0).getX(), bound.getY(), size, bound.getHeight());
            else if (isHorizontal)
                bound = new javafx.scene.shape.Rectangle(bound.getX(), points.get(0).getY(), bound.getWidth(), size);
        }else {
            if (isVertical)
                bound = new javafx.scene.shape.Rectangle(bound.getX(), bound.getY(), size, bound.getHeight());
            else if (isHorizontal)
                bound = new javafx.scene.shape.Rectangle(bound.getX(), bound.getY(), bound.getWidth(), size);
        }

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
        if(isVertical)
            gc.fillRect(bound.getX(), bound.getY(), size, bound.getHeight());
        else if(isHorizontal)
            gc.fillRect(bound.getX(), bound.getY(), bound.getWidth(), size);
    }

    @Override
    public void handle(MouseEvent e) {
        Point2D currentPoint = new Point2D(roundToNearestMultiple(e.getX(), 10), roundToNearestMultiple(e.getY(), 10));

        try {
            double width = Math.abs(points.get(1).getX() - points.get(0).getX());
            double height = Math.abs(points.get(1).getY() - points.get(0).getY());

            if (width >= height) {
                isHorizontal = true;
                isVertical = false;
            } else {
                isVertical = true;
                isHorizontal = false;
            }
        }
        catch (Exception ex) { }

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
