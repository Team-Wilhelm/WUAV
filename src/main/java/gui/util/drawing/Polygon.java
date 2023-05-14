package gui.util.drawing;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;

/**
 * A polygon shape.
 */
public class Polygon extends MyShape {

    /**
     * Constructs a polygon object.
     */
    public Polygon() {
        points = new ArrayList<>();
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(color);
        double[] x = new double[points.size()];
        double[] y = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
            x[i] = points.get(i).add(transform).getX();
            y[i] = points.get(i).add(transform).getY();
        }
        gc.fillPolygon(x, y, points.size());
    }

    @Override
    public void handle(MouseEvent e) {
        Point2D currentPoint = new Point2D(e.getX(), e.getY());
        if (e.getEventType() == MouseEvent.MOUSE_CLICKED)
            points.add(currentPoint);
    }

    @Override
    public void handle(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER)
            didFinishDrawingCallback.run();
    }
}
