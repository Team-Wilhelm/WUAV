package gui.controller.AddControllers;

import be.Document;
import gui.controller.ViewControllers.ViewController;
import gui.tasks.SaveTask;
import gui.tasks.TaskState;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.stage.Window;
import gui.util.AlertManager;

public abstract class AddController<T> {
    protected abstract void assignInputToVariables();
    protected abstract void assignListenersToTextFields();
    protected abstract void setIsEditing(T objectToEdit);
    private final AlertManager alertManager = AlertManager.getInstance();

    protected void setUpSaveTask(SaveTask<T> task, ViewController controller, Window owner, AddController<T> addController) {
        setUpTask(task, controller, owner);

        task.setOnSucceeded(event -> {
            // unbind the progress label and spinner from the task and set spinner to 100%
            controller.unbindProgress();

            // after 3 seconds, the progress bar will be hidden
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            controller.setProgressVisibility(false);
                        }
                    },
                    3000
            );

            if (task.getValue() == TaskState.DUPLICATE_DATA) {
                alertManager.showError("Username already exists!", "Username already exists!", owner);
            /*} else if (task.getValue() == TaskState.SUCCESSFUL && ((SaveTask<?>) task).isEditing()) {
                controller.refreshLastFocusedCard();
                //TODO observer pattern

             */
            } else if (task.getValue() == TaskState.SUCCESSFUL) {
                controller.refreshItems();
                addController.setIsEditing(task.getObjectToSave());
            } else {
                alertManager.showError("Oops...", "Something went wrong!", owner);
            }
        });
    }

    private void setUpTask(Task<TaskState> task, ViewController controller, Window owner) {
        task.setOnRunning(event -> {
            controller.bindProgressToTask(task);
            controller.setProgressVisibility(true);
        });

        task.setOnFailed(event -> {
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            controller.setProgressVisibility(false);
                            controller.unbindProgress();
                        }
                    },
                    3000
            );
            alertManager.showError("Oops...", "Something went wrong!", owner);
        });
    }

    protected void setUpDeleteTask(Task<TaskState> task, ViewController viewController, Window owner) {
        setUpTask(task, viewController, owner);

        task.setOnSucceeded(event -> {
            // unbind the progress label and spinner from the task and set spinner to 100%
            viewController.unbindProgress();

            // after 3 seconds, the progress bar will be hidden
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            viewController.setProgressVisibility(false);
                        }
                    },
                    3000
            );

            if (task.getValue() == TaskState.SUCCESSFUL) {
                viewController.refreshItems();
            } else {
                alertManager.showError("Oops...", "Something went wrong!", owner);
            }
        });
    }

    protected void closeWindow(ActionEvent actionEvent) {
        ((Node) actionEvent.getSource()).getScene().getWindow().hide();
    }

    protected boolean isInputEmpty(MFXTextField textField) {
        return textField.getText().trim().isEmpty();
    }

    protected boolean isInputEmpty(TextArea textArea) {
        return textArea.getText().trim().isEmpty();
    }
}
