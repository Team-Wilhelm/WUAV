package gui.controller.AddControllers;

import be.Document;
import gui.controller.ViewControllers.DocumentController;
import gui.controller.ViewControllers.ViewController;
import gui.tasks.GeneratePdfTask;
import gui.tasks.SaveTask;
import utils.enums.ResultState;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import gui.util.DialogManager;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Timer;

public abstract class AddController<T> {
    protected abstract void assignInputToVariables();
    protected abstract void assignListenersToTextFields();
    protected abstract void setIsEditing(T objectToEdit);
    private final DialogManager dialogManager = DialogManager.getInstance();

    /**
     * Set up save task for the controller
     * @param task task to set up
     * @param controller controller to set up
     * @param owner owner of the task
     * @param addController controller to add
     */
    protected void setUpSaveTask(SaveTask<T> task, ViewController<T> controller, Pane owner, AddController<T> addController) {
        setUpTask(task, controller, owner);

        task.setOnSucceeded(event -> {
            task.setCallback(taskState -> {
                controller.refreshItems();
                if (taskState == ResultState.SUCCESSFUL) {
                    addController.setIsEditing(task.getObjectToSave());
                    if (addController instanceof AddUserController){
                        ((AddUserController) addController).refreshCard();
                    }
                    if (addController instanceof AddDocumentController) {
                        ((AddDocumentController) addController).setUpPdfListView();
                        ((DocumentController) controller).setCustomerChanged(true);
                    }
                } else if (task.getValue() == ResultState.DUPLICATE_DATA) {
                    dialogManager.showError("Username already exists!", "Username already exists!", owner);
                }
                else if (task.getValue() == ResultState.NO_PERMISSION){
                    dialogManager.showError("Insufficient permission" , "You do not have permission to do this", owner);
                }
                else {
                    dialogManager.showError("Oops...", "Something went wrong!", owner);
                }
            });

            hideMessageAfterTimeout(controller);

            if (task.getCallback() != null) {
                task.getCallback().onTaskCompleted(task.getValue());
            }
        });
    }

    /**
     * Set up task for the controller
     * @param task task to set up
     * @param controller controller to set up
     * @param owner owner of the task
     */
    private void setUpTask(Task<ResultState> task, ViewController<T> controller, Pane owner) {
        task.setOnRunning(event -> {
            controller.bindProgressToTask(task);
            controller.setProgressVisibility(true);
        });

        task.setOnFailed(event -> {
            hideMessageAfterTimeout(controller);
            dialogManager.showError("Oops...", "Something went wrong!", owner);
        });
    }

    /**
     * Set up delete task for the controller
     * @param task task to set up
     * @param viewController controller to set up
     * @param owner owner of the task
     */
    protected void setUpDeleteTask(Task<ResultState> task, ViewController<T> viewController, Pane owner) {
        setUpTask(task, viewController, owner);

        task.setOnSucceeded(event -> {
            hideMessageAfterTimeout(viewController);

            if (task.getValue() == ResultState.SUCCESSFUL) {
                viewController.refreshItems();
            }

            else if (task.getValue() == ResultState.NO_PERMISSION){
                dialogManager.showError("Insufficient permission" , "You do not have permission to do this", owner);
            }

            else {
                dialogManager.showError("Oops...", "Something went wrong!", owner);
            }
        });
    }


    /**
     * Opens a loading dialog while the PDF is being created and opens the PDF when it is done
     * @param task task to set up
     * @param document document to set up
     * @param owner owner of the task
     */
    protected void setUpPdfTask(GeneratePdfTask task, Document document, Pane owner) {
        task.setOnRunning(event -> {
            dialogManager.showLoadingDialog("Creating PDF...", "Creating pdf...", owner, task, () -> {
                try {
                    Desktop.getDesktop().open(new File(task.getPdfPath().toUri()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    /**
     * Hides the progress bar after 3 seconds
     * @param controller controller to set up
     */
    private void hideMessageAfterTimeout(ViewController<T> controller) {
        // unbind the progress label and spinner from the task and set spinner to 100%
        controller.unbindProgress();

        // after 3 seconds, the progress bar will be hidden
        new Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        controller.setProgressVisibility(false);
                    }
                },
                3000
        );
    }

    /**
     * Closes the window
     * @param event event to close
     */
    protected void closeWindow(Event event) {
        ((Node) event.getSource()).getScene().getWindow().hide();
    }

    /**
     * Closes the window
     * @param node node to close
     */
    protected void closeWindow(Node node) {
        node.getScene().getWindow().hide();
    }

    /**
     * Checks if the input is empty
     * @param textField text field to check
     * @return true if the input is empty
     */
    protected boolean isInputEmpty(MFXTextField textField) {
        return textField.getText().trim().isEmpty();
    }

    /**
     * Checks if the input is empty
     * @param textArea text area to check
     * @return true if the input is empty
     */
    protected boolean isInputEmpty(TextArea textArea) {
        return textArea.getText().trim().isEmpty();
    }
}
