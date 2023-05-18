package gui.controller;
import gui.util.drawing.CanvasPane;
import gui.util.drawing.MyToolBar;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.ResourceBundle;

public class CanvasController implements Initializable {
    @FXML
    public VBox canvasVbox, toolbar;
    @FXML
    public Canvas canvas;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        CanvasPane canvas = new CanvasPane(550, 711);
        toolbar.getChildren().add(new MyToolBar(canvas));
        canvasVbox.getChildren().addAll(canvas);

        Platform.runLater(() -> {
            try {
                Scene scene = canvasVbox.getScene();
                scene.setOnKeyPressed(event -> canvas.getEventHandler().handle(event));
            } catch (NullPointerException npe) { }
        });
    }
    private void updateCanvasSize() {
        // Subtract the width of the toolbar from the tab pane width
        double canvasWidth = canvasVbox.getWidth();
        double canvasHeight = canvasVbox.getHeight();

        // Set the size of the canvas
        canvas.setWidth(canvasWidth);
        canvas.setHeight(canvasHeight);
    }
}
