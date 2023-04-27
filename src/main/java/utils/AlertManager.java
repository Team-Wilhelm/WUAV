package utils;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.stage.Window;

public class AlertManager {
    private Alert alert;
    private static AlertManager instance = null;

    private AlertManager() {
        alert = new Alert(Alert.AlertType.ERROR);
    }

    /**
     * Creates an Alert template, allowing it to be reused multiple times.
     */
    public Alert getAlert(Alert.AlertType type, String text, Event actionEvent) {
        alert.setAlertType(type);
        alert.setContentText(text);

        if (actionEvent != null){
            Node node = (Node) actionEvent.getSource();
            Window window = node.getScene().getWindow();
            if (alert.getOwner() == null)
                alert.initOwner(window);
        }
        return alert;
    }

    /**
     * Makes AlertManager a singleton class, in order to reuse the same alert and avoid code repetition
     */
    public static AlertManager getInstance() {
        if (instance == null) {
            instance = new AlertManager();
        }
        return instance;
    }

    public void showWarning(String header, String content, Window owner) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initOwner(owner);
        alert.setTitle("Warning");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
