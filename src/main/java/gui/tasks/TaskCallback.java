package gui.tasks;

import utils.enums.ResultState;

/**
 * A callback interface for tasks.
 */
public interface TaskCallback {
    void onTaskCompleted(ResultState resultState);
}
