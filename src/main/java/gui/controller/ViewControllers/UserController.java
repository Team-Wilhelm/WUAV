package gui.controller.ViewControllers;

import be.User;
import utils.enums.UserRole;
import gui.nodes.UserCard;
import gui.util.SceneManager;
import gui.controller.AddControllers.AddUserController;
import gui.model.UserModel;
import utils.enums.ResultState;
import gui.util.DialogueManager;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXProgressSpinner;
import io.github.palexdev.materialfx.controls.MFXTextField;
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
import javafx.stage.Window;
import utils.permissions.AccessChecker;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

public class UserController extends ViewController<User> implements Initializable {
    @FXML
    public FlowPane flowPane;
    @FXML
    public ScrollPane scrollPane;
    @FXML
    private MFXProgressSpinner progressSpinner;
    @FXML
    private Label progressLabel;
    @FXML
    private MFXButton btnAddEmployee;
    @FXML
    private MFXTextField searchBar;

    private ObservableList<UserCard> userCards = FXCollections.observableArrayList();
    private final UserModel userModel = UserModel.getInstance();
    private UserCard lastFocusedCard;
    private boolean hasAccess = false;
    private AccessChecker checker = new AccessChecker();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //TODO disable edit button while saving
        setProgressVisibility(false);

        Bindings.bindContent(flowPane.getChildren(), userCards);
        userCards.setAll(userModel.getLoadedCards().values());

        flowPane.prefHeightProperty().bind(scrollPane.heightProperty());
        flowPane.prefWidthProperty().bind(scrollPane.widthProperty());

        searchBar.textProperty().addListener((observable, oldValue, newValue) ->
                refreshItems(userModel.searchUsers(searchBar.getText().toLowerCase().trim())));

        refreshItems();
    }

    @Override
    public void setProgressVisibility(boolean visible) {
        progressSpinner.setVisible(visible);
        progressLabel.setVisible(visible);
    }

    @Override
    public void bindProgressToTask(Task<ResultState> task) {
        progressSpinner.setProgress(0);
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

    public void refreshLastFocusedCard() {
        //if (lastFocusedCard != null)
            //lastFocusedCard.update(userModel.getAll().get(lastFocusedCard.getUser().getUserID()), userModel.getAll().get(lastFocusedCard.getUser().getUserID()));
    }

    @Override
    public void refreshItems(List<User> items) {
        userCards.clear();

        HashMap<User, UserCard> loadedCards = (HashMap<User, UserCard>) userModel.getLoadedCards();
        for (User user : items) {
            UserCard userCard = loadedCards.get(user);
            if (userCard == null) {
                userCard = userModel.addUserCard(user);
            }

            //TODO change into observer pattern
            if (lastFocusedCard != null && userCard.getUser() == lastFocusedCard.getUser()) {
                userCard = userModel.addUserCard(user);
                loadedCards.put(user, userCard);
            }

            final UserCard finalUserCard = userCard;
            userCard.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) lastFocusedCard = finalUserCard;
            });

            userCard.setOnMouseClicked(e -> {
                if (!finalUserCard.isFocused())
                    finalUserCard.requestFocus();

                if (e.getClickCount() == 2) {
                    lastFocusedCard = finalUserCard;
                    editUser(scrollPane.getScene().getWindow());
                }
            });
            userCards.add(userCard);
        }
    }

    private void editUser(Window window) {
        if (lastFocusedCard != null) {
            try {
                FXMLLoader loader = openWindow(SceneManager.ADD_EMPLOYEE_SCENE, Modality.APPLICATION_MODAL);
                AddUserController controller = loader.getController();
                controller.setUserController(this);
                controller.setIsEditing(lastFocusedCard.getUser());
                controller.setVisibilityForUserRole();
                controller.setShortcutsAndAccelerators();
            } catch (Exception e) {
               e.printStackTrace();
            }
        } else {
            DialogueManager.getInstance().showWarning("No user selected", "Please select a user to edit", flowPane);
        }
    }

    @Override
    public void refreshItems() {
        refreshItems(List.copyOf(userModel.getAll().values()));
    }

    @FXML
    private void addEmployeeAction(ActionEvent actionEvent) throws IOException {
            FXMLLoader loader = openWindow(SceneManager.ADD_EMPLOYEE_SCENE, Modality.APPLICATION_MODAL);
            AddUserController controller = loader.getController();
            controller.setUserController(this);
            controller.setShortcutsAndAccelerators();
    }

    public void setVisibilityForUserRole() {
        UserRole loggedInUserRole = UserModel.getLoggedInUser().getUserRole();
        if(loggedInUserRole == UserRole.ADMINISTRATOR){
            hasAccess = true;
        }
        btnAddEmployee.setVisible(hasAccess);
    }
}
