import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private int idCounter;
    private HashMap<Integer, Task> allTasks;

    public TaskManager() {
        this.idCounter = 1;
        this.allTasks = new HashMap<>();
    }

    public void createTask(Task task){
        allTasks.put(idCounter, task);
        idCounter++;
    }

    public int getIdCounter() {
        return idCounter;
    }

    public ArrayList<Task> getAllTasks() {
        ArrayList<Task> allTasksList = new ArrayList<>();
        for (Task task : allTasks.values()) {
            allTasksList.add(task);
        }
        return allTasksList;
    }

    public void updateTask(Task task, int id) {
        allTasks.put(id, task);
    }
}

