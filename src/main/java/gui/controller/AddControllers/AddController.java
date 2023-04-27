package gui.controller.AddControllers;

import gui.controller.ViewControllers.ViewController;
import gui.tasks.SaveTask;
import gui.tasks.TaskState;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import utils.AlertManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AddController {
    private AlertManager alertManager = AlertManager.getInstance();
    protected void setUpSaveTask(Task<TaskState> task, ViewController controller, ActionEvent actionEvent) {
        setUpTask(task, controller, actionEvent);

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
                AlertManager.getInstance().getAlert(Alert.AlertType.ERROR, "Username already exists!", actionEvent).showAndWait();
            } else if (task.getValue() == TaskState.SUCCESSFUL && ((SaveTask) task).isEditing()) {
                controller.refreshLastFocusedCard();
            } else if (task.getValue() == TaskState.SUCCESSFUL) {
                controller.refreshItems();
            } else {
                alertManager.getAlert(Alert.AlertType.ERROR, "Something went wrong!", actionEvent).showAndWait();
            }
        });
    }

    private void setUpTask(Task<TaskState> task, ViewController controller, ActionEvent actionEvent) {
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
            alertManager.getAlert(Alert.AlertType.ERROR, "Something went wrong!", actionEvent).showAndWait();
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
