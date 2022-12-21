package managers;

public class Managers {

    static InMemoryHistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager<>();
    }

    TaskManager getDefault() {
        TaskManager defaultManager = new InMemoryTaskManager<>();
        return defaultManager;
    }
}