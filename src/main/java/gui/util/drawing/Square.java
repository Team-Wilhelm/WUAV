package gui.util.drawing;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.Arrays;

/**
 * A square shape.
 */
public class Square extends MyShape {

    /**
     * Constructs a square object.
     */
    public Square() {
        points = Arrays.asList(null, null);
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (points.get(0) == null || points.get(1) == null)
            return;

        gc.setFill(color);
        Rectangle bound = getBound();

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
            points.set(1, calcEndPoint(currentPoint));
        else if (e.getEventType() == MouseEvent.MOUSE_RELEASED)
            didFinishDrawingCallback.run();
    }

    @Override
    public void handle(KeyEvent e) {

    }

    /**
     * A helper method to calculate the end point based on the start point
     * and the dragging direction to make the shape to be a square.
     * @param endPoint current end point
     * @return the correct end point
     */
    private Point2D calcEndPoint(Point2D endPoint) {
        final Point2D startPoint = points.get(0).add(transform);
        final double width = Math.abs(startPoint.getX() - endPoint.getX());
        final double height = Math.abs(startPoint.getY() - endPoint.getY());
        final double side = Math.min(width, height);
        final double multiple = 10.0; // the multiple of 10

        // calculate the size of the shape that is a multiple of 10
        double newSize = Math.floor(side / multiple) * multiple;

        // if the size is less than 10, set it to 10
        newSize = Math.max(newSize, 10);

        // calculate the new endpoint based on the start point and the new size
        if (startPoint.getX() < endPoint.getX()) {
            if (startPoint.getY() < endPoint.getY())
                return startPoint.add(newSize, newSize);
            else
                return startPoint.add(newSize, -newSize);
        } else {
            if (startPoint.getY() < endPoint.getY())
                return startPoint.add(-newSize, newSize);
            else
                return startPoint.add(-newSize, -newSize);
        }
    }
}
