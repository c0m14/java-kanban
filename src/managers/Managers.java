package managers;

import java.io.IOException;
import java.nio.file.Path;

public class Managers {

    public static InMemoryHistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static TaskManager getDefault() {
        TaskManager defaultManager = new InMemoryTaskManager();
        return defaultManager;
    }

    public static TaskManager getDefault(Path path) {
        TaskManager defaultManager = new FileBackedTaskManager(path);
        return defaultManager;
    }

    public static TaskManager getDefault(String host) throws IOException, InterruptedException {
        TaskManager defaultManager = new HttpTaskManager(host);
        return defaultManager;
    }

}