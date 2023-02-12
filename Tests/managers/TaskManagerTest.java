package managers;

import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class TaskManagerTest <T extends TaskManager> {
    protected T taskManager;

    protected void setTaskManager(T taskManager) {
        this.taskManager = taskManager;
    }

    @BeforeEach
    public void beforeEach() {
        setTaskManager((T) Managers.getDefault());
    }

    @Test
    public void shouldCreateNewTask() {
        Task testTask1 = new Task()
    }
}
