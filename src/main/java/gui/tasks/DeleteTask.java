package gui.tasks;

import gui.model.IModel;
import javafx.concurrent.Task;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DeleteTask<T> extends Task<TaskState> {
    private final UUID objectToDelete;
    private final IModel<T> model;

    public DeleteTask(UUID objectToDelete, IModel<T> model) {
        this.objectToDelete = objectToDelete;
        this.model = model;
    }

    @Override
    protected TaskState call() {
        if (isCancelled()) {
            updateMessage("Deleting cancelled");
            return TaskState.NOT_SUCCESSFUL;
        }
        else {
            updateMessage("Deleting...");
            CompletableFuture<String> future = model.delete(objectToDelete);
            String message = future.join();

            if (message.equals("deleted")) {
                updateMessage("Deleted successfully");
                return TaskState.SUCCESSFUL;
            }

            else if(message.equals("No Permission")){
                return TaskState.NO_PERMISSION;
            }

            else {
                updateMessage("Deleting was not successful");
                return TaskState.NOT_SUCCESSFUL;
            }
        }
    }
}
