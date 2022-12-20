package managers;

public abstract class Managers implements TaskManager {

    static InMemoryHistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager<>();
    }

    abstract TaskManager getDefault();
}