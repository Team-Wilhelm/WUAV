package gui.util.drawing;

import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * The canvas pane to draw on.
 */
public class CanvasPane extends Pane {

    /**
     * The canvas.
     */
    public Canvas canvas;

    /**
     * Holds a list of shapes to render on the canvas.
     */
    private final List<MyShape> shapes;

    /**
     * A handler corresponding to the current selected tool.
     */
    private EventResponsible eventHandler;

    /**
     * Constructs the canvas pane.
     */
    public CanvasPane(int width, int height) {
        canvas = new Canvas(width, height);
        shapes = new ArrayList<>();
        getChildren().add(canvas);

        EventHandler<MouseEvent> handler = this::handle;

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, handler);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, handler);
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, handler);
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, handler);

        drawGrid();
    }

    /**
     * Add background grid to the canvas.
     */
    private void drawGrid() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double cellWidth = 20;  // Width of each grid cell
        double cellHeight = 20; // Height of each grid cell
        double width = getWidth();
        double height = getHeight();

        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);

        // Draw vertical lines
        for (double x = 0; x < width; x += cellWidth) {
            gc.strokeLine(x, 0, x, height);
        }

        // Draw horizontal lines
        for (double y = 0; y < height; y += cellHeight) {
            gc.strokeLine(0, y, width, y);
        }
        gc.setStroke(null);
        gc.setLineWidth(0);
    }

    /**
     * Getter for shapes.
     * @return list of shapes
     */
    public List<MyShape> getShapes() {
        return shapes;
    }

    /**
     * Getter for event handler.
     * @return the current event handler
     */
    public EventResponsible getEventHandler() {
        return eventHandler;
    }

    /**
     * Setter for event handler.
     * @param eventHandler new event handler
     */
    public void setEventHandler(EventResponsible eventHandler) {
        this.eventHandler = eventHandler;
    }

    /**
     * Clears the canvas.
     * @param color the background color of the canvas after clearing
     */
    public void clear(Color color) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(color);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawGrid();
    }

    /**
     * Clears the canvas with a white background.
     */
    public void clear() {
        clear(Color.WHITE);
    }

    /**
     * Draw all the shapes onto the canvas.
     */
    private void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        for (MyShape myShape : shapes) {
            myShape.draw(gc);
        }
    }

    /**
     * Update the canvas by clearing and redrawing the shapes.
     */
    public void update() {
        clear();
        render();
    }

    /**
     * Deselect all the shapes.
     */
    public void deselectAll() {
        for (MyShape myShape : shapes) {
            myShape.setSelected(false);
        }
        update();
    }

    private void handle(MouseEvent e) {
        eventHandler.handle(e);
    }
}
