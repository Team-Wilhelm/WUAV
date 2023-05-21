package gui.controller;
import com.sun.jna.platform.win32.Guid;
import gui.util.drawing.CanvasPane;
import gui.util.drawing.MyToolBar;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.ResourceBundle;

public class CanvasController implements Initializable {
    @FXML
    public VBox canvasVbox, toolbar;
    @FXML
    public CanvasPane canvas;
    public MyToolBar myToolBar;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        canvas = new CanvasPane(550, 711);
        myToolBar = new MyToolBar(canvas);
        toolbar.getChildren().add(myToolBar);
        canvasVbox.getChildren().addAll(canvas);

        Platform.runLater(() -> {
            try {
                Scene scene = canvasVbox.getScene();
                scene.setOnKeyPressed(event -> canvas.getEventHandler().handle(event));
            } catch (NullPointerException npe) { }
        });
    }
}
