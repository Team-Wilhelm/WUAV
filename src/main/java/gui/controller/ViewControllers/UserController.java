package gui.controller.ViewControllers;

import be.cards.EmployeeCard;
import gui.SceneManager;
import gui.controller.AddControllers.AddUserController;
import gui.model.UserModel;
import gui.tasks.TaskState;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXProgressSpinner;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class UserController extends ViewController implements Initializable {
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private FlowPane flowPane;
    @FXML
    private MFXProgressSpinner progressSpinner;
    @FXML
    private Label progressLabel;
    @FXML
    private MFXButton btnAddEmployee;

    private ObservableList<EmployeeCard> employeeCards = FXCollections.observableArrayList();
    private UserModel userModel = UserModel.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setProgressVisibility(false);

        Bindings.bindContent(flowPane.getChildren(), employeeCards);
        employeeCards.setAll(userModel.getLoadedCards().values());

        flowPane.prefHeightProperty().bind(scrollPane.heightProperty());
        flowPane.prefWidthProperty().bind(scrollPane.widthProperty());

        refreshItems();
        btnAddEmployee.getStyleClass().addAll("addButton", "rounded");
    }

    @Override
    public void setProgressVisibility(boolean visible) {
        progressSpinner.setVisible(visible);
        progressLabel.setVisible(visible);
    }

    @Override
    public void bindProgressToTask(Task<TaskState> task) {
        progressSpinner.progressProperty().bind(task.progressProperty());
        progressLabel.textProperty().bind(task.messageProperty());
    }

    @Override
    public void unbindProgress() {
        progressSpinner.progressProperty().unbind();
        progressSpinner.setProgress(100);
        String text = progressLabel.getText();
        progressLabel.textProperty().unbind();
        progressLabel.setText(text);
    }

    @Override
    public void refreshLastFocusedCard() {

    }

    @Override
    public void refreshItems(List<?> items) {

    }

    @Override
    public void refreshItems() {
        refreshItems(List.copyOf(userModel.getAll().values()));
    }

    public void addEmployeeAction(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = openWindow(SceneManager.ADD_EMPLOYEE_SCENE, Modality.APPLICATION_MODAL);
        AddUserController controller = loader.getController();
        controller.setUserController(this);
    }
}
