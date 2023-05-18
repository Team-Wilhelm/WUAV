package gui.tasks;

import gui.model.IModel;
import javafx.concurrent.Task;

import java.util.concurrent.CompletableFuture;

public class SaveTask<T> extends Task<TaskState> {
    private final T objectToSave;
    private final boolean isEditing;
    private final IModel<T> model;
    private Callback callback;

    public SaveTask(T objectToSave, boolean isEditing, IModel<T> model) {
        this.objectToSave = objectToSave;
        this.isEditing = isEditing;
        this.model = model;
    }

    @Override
    protected TaskState call() {
        Thread.currentThread().setName("SaveTask");

        if (isCancelled()) {
            updateMessage("Saving was not successful");
            return TaskState.NOT_SUCCESSFUL;
        }

        else {
            updateMessage("Saving...");
            CompletableFuture<String> future;
            String message;
            if (isEditing) {
                message = model.update(objectToSave);
            }
            else {
                future = model.add(objectToSave);
                message = future.join();
            }


            if (message.equals("saved") || message.equals("updated")) {
                updateMessage("Saved successfully");
                return TaskState.SUCCESSFUL;
            }
            else if (message.equals("No Permission")){
                return TaskState.NO_PERMISSION;
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

    public T getObjectToSave() {
        return objectToSave;
    }

    public Callback getCallback() {
        return callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void onTaskCompleted(TaskState taskState);
    }
}
