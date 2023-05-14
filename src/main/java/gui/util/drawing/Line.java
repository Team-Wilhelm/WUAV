package gui.util.drawing;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;

public class Line extends MyShape {
    private boolean isVertical = false;
    private boolean isHorizontal = false;

    /**
     * Constructs a line object.
     */
    public Line() {
        points = new ArrayList<>();
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (points.size() < 2)
            return;
        gc.setStroke(color);
        gc.setLineWidth(5);

        double startX = points.get(0).add(transform).getX();
        double startY = points.get(0).add(transform).getY();
        double endX = points.get(1).add(transform).getX();
        double endY = points.get(1).add(transform).getY();

        if (isVertical) {
            startX = endX = Math.round((startX + endX) / 2);
        } else if (isHorizontal) {
            startY = endY = Math.round((startY + endY) / 2);
        } else {
            return;
        }

        gc.strokeLine(startX, startY, endX, endY);
    }

    @Override
    public void handle(MouseEvent e) {
        Point2D currentPoint = new Point2D(e.getX(), e.getY());
        if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {
            if (points.isEmpty()) {
                points.add(currentPoint);
            } else {
                Point2D startPoint = points.get(0).add(transform);
                Point2D endPoint = currentPoint.add(transform);
                double deltaX = Math.abs(endPoint.getX() - startPoint.getX());
                double deltaY = Math.abs(endPoint.getY() - startPoint.getY());

                if (deltaX >= deltaY) {
                    points.add(new Point2D(currentPoint.getX(), startPoint.getY()));
                    isHorizontal = true;
                } else {
                    points.add(new Point2D(startPoint.getX(), currentPoint.getY()));
                    isVertical = true;
                }
                didFinishDrawingCallback.run();
            }
        }
    }

    @Override
    public void handle(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER)
            didFinishDrawingCallback.run();
    }
}
