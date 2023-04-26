package gui.controller;

import gui.SceneManager;
import io.github.palexdev.materialfx.controls.MFXProgressSpinner;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

public class MenuController implements Initializable {
    @FXML
    private GridPane gridPane;
    private Node documentView, employeeView, currentScene;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FXMLLoader documentLoader = new FXMLLoader(getClass().getResource(SceneManager.DOCUMENT_SCENE));
        FXMLLoader employeeLoader = new FXMLLoader(getClass().getResource(SceneManager.EMPLOYEE_SCENE));
        try {
            documentView = documentLoader.load();
            employeeView = employeeLoader.load();
            currentScene = documentView;
            gridPane.add(currentScene, 1, 0, 1,gridPane.getRowCount());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void switchScene(Node scene) {
       if (currentScene != scene) {
              gridPane.getChildren().remove(currentScene);
              gridPane.add(scene, 1, 0, 1, gridPane.getRowCount());
              currentScene = scene;
       }
    }

    public void btnDocumentAction(ActionEvent actionEvent) {
        switchScene(documentView);
    }

    public void btnEmployeesAction(ActionEvent actionEvent) {
        switchScene(employeeView);
    }
}
