package managers;

public class Managers {

    public static InMemoryHistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager<>();
    }

    public static TaskManager getDefault() {
        TaskManager defaultManager = new InMemoryTaskManager<>();
        return defaultManager;
    }
}