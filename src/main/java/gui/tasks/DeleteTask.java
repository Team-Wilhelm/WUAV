package gui.tasks;

import gui.model.IModel;
import javafx.concurrent.Task;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DeleteTask extends Task<TaskState> {
    private final UUID objectToDelete;
    private final IModel<Object> model;

    public DeleteTask(UUID objectToDelete, IModel<Object> model) {
        this.objectToDelete = objectToDelete;
        this.model = model;
    }

    @Override
    protected TaskState call() throws Exception {
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
            else {
                updateMessage("Deleting was not successful");
                return TaskState.NOT_SUCCESSFUL;
            }
        }
    }
}
