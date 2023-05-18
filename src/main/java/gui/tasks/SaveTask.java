package gui.tasks;

import gui.model.IModel;
import javafx.concurrent.Task;
import utils.enums.ResultState;

import java.util.concurrent.CompletableFuture;

public class SaveTask<T> extends Task<ResultState> {
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
    protected ResultState call() {
        Thread.currentThread().setName("SaveTask");

        if (isCancelled()) {
            updateMessage("Saving was not successful");
            return ResultState.FAILED;
        }

        else {
            updateMessage("Saving...");
            ResultState resultState;

            if (isEditing) {
                resultState = model.update(objectToSave);
            } else {
                resultState = model.add(objectToSave);
            }

            if (resultState.equals(ResultState.SUCCESSFUL)) {
                updateMessage("Saved successfully");
                return ResultState.SUCCESSFUL;
            }
            else if (resultState.equals(ResultState.NO_PERMISSION)){
                return ResultState.NO_PERMISSION;
            }

            else {
                updateMessage("Saving was not successful");
                return ResultState.FAILED;
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
        void onTaskCompleted(ResultState resultState);
    }
}
