package gui.util.drawing;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.Arrays;

/**
 * A square shape.
 */
public class Icon extends MyShape {
    private final String path;
    /**
     * Constructs a square object.
     */
    public Icon(String path) {
        this.path = path;
        points = Arrays.asList(null, null);
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (points.get(0) == null || points.get(1) == null)
            return;

        gc.setFill(Color.TRANSPARENT);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(5);
        Rectangle bound = getBound();

        gc.fillRect(bound.getX(), bound.getY(), bound.getWidth(), bound.getHeight());
        var image = new Image(path);
        gc.drawImage(image, bound.getX(), bound.getY(), bound.getWidth(), bound.getHeight());
    }

    @Override
    public void handle(MouseEvent e) {
        Point2D currentPoint = new Point2D(e.getX(), e.getY());
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
        final double multiple = 10.0; // the multiple of 5

        // calculate the size of the shape that is a multiple of 5
        double newSize = Math.floor(side / multiple) * multiple;

        // if the size is less than 5, set it to 5
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
