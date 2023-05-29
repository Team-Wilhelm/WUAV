package gui.tasks;

import gui.model.IModel;
import javafx.concurrent.Task;
import utils.enums.ResultState;

import java.util.UUID;

/**
 * A task that deletes an object from the database.
 * @param <T> The type of object to delete.
 */
public class DeleteTask<T> extends Task<ResultState> {
    private final UUID objectToDelete;
    private final IModel<T> model;

    public DeleteTask(UUID objectToDelete, IModel<T> model) {
        this.objectToDelete = objectToDelete;
        this.model = model;
    }

    @Override
    protected ResultState call() {
        if (isCancelled()) {
            updateMessage("Deleting cancelled");
            return ResultState.FAILED;
        }
        else {
            updateMessage("Deleting...");
            ResultState resultState = model.delete(objectToDelete);;

            if (resultState.equals(ResultState.SUCCESSFUL)) {
                updateMessage("Deleted successfully");
                return ResultState.SUCCESSFUL;
            } else if(resultState.equals(ResultState.NO_PERMISSION)){
                return ResultState.NO_PERMISSION;
            } else {
                updateMessage("Deleting was not successful");
                return ResultState.FAILED;
            }
        }
    }
}
