package gui.controller.AddControllers;

import gui.controller.ViewControllers.ViewController;
import gui.tasks.SaveTask;
import gui.tasks.TaskState;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.stage.Window;
import utils.AlertManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AddController {
    private AlertManager alertManager = AlertManager.getInstance();
    protected void setUpSaveTask(Task<TaskState> task, ViewController controller, Window owner) {
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
            } else if (task.getValue() == TaskState.SUCCESSFUL && ((SaveTask) task).isEditing()) {
                controller.refreshLastFocusedCard();
            } else if (task.getValue() == TaskState.SUCCESSFUL) {
                controller.refreshItems();
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

    protected void executeTask(Task<TaskState> task) {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(task);

        try {
            executorService.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executorService.shutdownNow();
        }
    }

    protected void closeWindow(ActionEvent actionEvent) {
        ((Node) actionEvent.getSource()).getScene().getWindow().hide();
    }
}
