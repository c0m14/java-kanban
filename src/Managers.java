public abstract class Managers implements TaskManager {

    abstract TaskManager getDefault();

    static InMemoryHistoryManager getDefaultHistory(){
        return new InMemoryHistoryManager<>();
    }
}