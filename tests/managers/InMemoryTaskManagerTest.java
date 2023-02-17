package managers;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected void setTaskManager() {
        taskManager = (InMemoryTaskManager) Managers.getDefault();
    }

}
