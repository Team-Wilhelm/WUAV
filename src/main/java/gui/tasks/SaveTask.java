package gui.tasks;

import gui.model.IModel;
import javafx.concurrent.Task;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class SaveTask extends Task<TaskState> {
    private final Object objectToSave;
    private final boolean isEditing;
    private final IModel<Object> model;

    public SaveTask(Object objectToSave, boolean isEditing, IModel<Object> model) {
        this.objectToSave = objectToSave;
        this.isEditing = isEditing;
        this.model = model;
    }


    @Override
    protected TaskState call() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        if (isCancelled()) {
            updateMessage("Saving was not successful");
            return TaskState.NOT_SUCCESSFUL;
        }

        else {
            updateMessage("Saving...");
            String message;
            if (isEditing)
                message = model.update(objectToSave, latch);
            else {
                CompletableFuture<String> future = model.add(objectToSave);
                message = future.join();
            }

            if (message.isEmpty()) {
                updateMessage("Saved successfully");
                return TaskState.SUCCESSFUL;
            }
            else if (message.contains("Violation of UNIQUE KEY constraint")) {
                updateMessage("Already exists");
                return TaskState.DUPLICATE_DATA;
            }
            else {
                updateMessage("Saving was not successful");
                return TaskState.NOT_SUCCESSFUL;
            }
        }
    }

    public boolean isEditing() {
        return isEditing;
    }
}
