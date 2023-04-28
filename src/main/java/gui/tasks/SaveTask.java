package gui.tasks;

import gui.model.IModel;
import javafx.concurrent.Task;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class SaveTask<T> extends Task<TaskState> {
    private final T objectToSave;
    private final boolean isEditing;
    private final IModel<T> model;

    public SaveTask(T objectToSave, boolean isEditing, IModel<T> model) {
        this.objectToSave = objectToSave;
        this.isEditing = isEditing;
        this.model = model;
    }


    @Override
    protected TaskState call() throws Exception {
        if (isCancelled()) {
            updateMessage("Saving was not successful");
            return TaskState.NOT_SUCCESSFUL;
        }

        else {
            updateMessage("Saving...");
            String message;
            if (isEditing)
                message = model.update(objectToSave);
            else {
                CompletableFuture<String> future = model.add(objectToSave);
                message = future.join();
            }

            if (message.equals("saved") || message.equals("updated")) {
                updateMessage("Saved successfully");
                return TaskState.SUCCESSFUL;
            } else {
                updateMessage("Saving was not successful");
                return TaskState.NOT_SUCCESSFUL;
            }
        }
    }

    public boolean isEditing() {
        return isEditing;
    }
}
