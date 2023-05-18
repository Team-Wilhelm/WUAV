package gui.tasks;

import gui.model.IModel;
import javafx.concurrent.Task;
import utils.enums.ResultState;

import java.util.UUID;

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
            String message = model.delete(objectToDelete);;

            if (message.equals("deleted")) {
                updateMessage("Deleted successfully");
                return ResultState.SUCCESSFUL;
            }

            else if(message.equals("No Permission")){
                return ResultState.NO_PERMISSION;
            }

            else {
                updateMessage("Deleting was not successful");
                return ResultState.FAILED;
            }
        }
    }
}
