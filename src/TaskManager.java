import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private int idCounter;
    private HashMap<Integer, Object> allTasks;

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

    public ArrayList<Object> getAllTasks() {
        ArrayList<Object> allTasksList = new ArrayList<>();
        for (Object task : allTasks.values()) {
            if (task instanceof Task){
                allTasksList.add(task);
            }
        }
        return allTasksList;
    }

    public void updateTask(Task task, int id) {
        allTasks.put(id, task);
    }

    public void removeTask(int id) {
        allTasks.remove(id);
    }
}

