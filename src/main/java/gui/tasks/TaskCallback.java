package gui.tasks;

import utils.enums.ResultState;

public interface TaskCallback {
    void onTaskCompleted(ResultState resultState);
}
