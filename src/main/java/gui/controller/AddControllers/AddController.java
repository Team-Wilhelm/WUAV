package gui.controller.AddControllers;

import gui.controller.ViewControllers.ViewController;
import gui.tasks.SaveTask;
import utils.enums.ResultState;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import gui.util.DialogueManager;

import java.util.Timer;

public abstract class AddController<T> {
    protected abstract void assignInputToVariables();
    protected abstract void assignListenersToTextFields();
    protected abstract void setIsEditing(T objectToEdit);
    private final DialogueManager dialogueManager = DialogueManager.getInstance();

    protected void setUpSaveTask(SaveTask<T> task, ViewController<T> controller, Pane owner, AddController<T> addController) {
        setUpTask(task, controller, owner);

        task.setOnSucceeded(event -> {
            task.setCallback(taskState -> {
                if (taskState == ResultState.SUCCESSFUL) {
                    controller.refreshItems();
                    addController.setIsEditing(task.getObjectToSave());
                    if (addController instanceof AddUserController){
                        ((AddUserController) addController).refreshCard();
                    }
                    if (addController instanceof AddDocumentController) {
                        ((AddDocumentController) addController).setUpPdfListView();
                    }
                } else if (task.getValue() == ResultState.DUPLICATE_DATA) {
                    dialogueManager.showError("Username already exists!", "Username already exists!", owner);
                }
                else if (task.getValue() == ResultState.NO_PERMISSION){
                    dialogueManager.showError("Insufficient permission" , "You do not have permission to do this", owner);
                }
                else {
                    dialogueManager.showError("Oops...", "Something went wrong!", owner);
                }
            });

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

            if (task.getCallback() != null) {
                task.getCallback().onTaskCompleted(task.getValue());
            }
        });
    }

    private void setUpTask(Task<ResultState> task, ViewController<T> controller, Pane owner) {
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
            dialogueManager.showError("Oops...", "Something went wrong!", owner);
        });
    }

    protected void setUpDeleteTask(Task<ResultState> task, ViewController<T> viewController, Pane owner) {
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

            if (task.getValue() == ResultState.SUCCESSFUL) {
                viewController.refreshItems();
            }

            else if (task.getValue() == ResultState.NO_PERMISSION){
                dialogueManager.showError("Insufficient permission" , "You do not have permission to do this", owner);
            }

            else {
                dialogueManager.showError("Oops...", "Something went wrong!", owner);
            }
        });
    }

    protected void closeWindow(Event event) {
        ((Node) event.getSource()).getScene().getWindow().hide();
    }

    protected void closeWindow(Node node) {
        node.getScene().getWindow().hide();
    }

    protected boolean isInputEmpty(MFXTextField textField) {
        return textField.getText().trim().isEmpty();
    }

    protected boolean isInputEmpty(TextArea textArea) {
        return textArea.getText().trim().isEmpty();
    }
}
