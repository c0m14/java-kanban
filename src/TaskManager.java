import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private int idCounter;
    private HashMap<String, HashMap<Integer, Object>> allItems;

    public TaskManager() {
        this.idCounter = 1;
        this.allItems = new HashMap<>();
    }

    public void createTask(Object anyItem){
        HashMap<Integer, Object> items;
        if (anyItem instanceof Task){
            if (allItems.get("Task") != null){
                items = allItems.get("Task");
            } else {
                items = new HashMap<>();
            }
            items.put(idCounter, anyItem);
            allItems.put("Task", items);
        }
        idCounter++;
    }

    public int getIdCounter() {
        return idCounter;
    }

    public ArrayList<Object> getAllTasks(String itemType) {
        ArrayList<Object> itemsByChosenType = new ArrayList<>();
        if (itemType.equals("Task")) {
            for(Object task : allItems.get(itemType).values()) {
                itemsByChosenType.add(task);
            }
        }
        return itemsByChosenType;
    }

    public void updateTask(Object anyItem, int id) {
        HashMap<Integer, Object> items;
        if (anyItem instanceof Task) {
            items = allItems.get("Task");
            items.put(id, anyItem);
            allItems.put("Task", items);
        }
    }

    public void removeItem(Integer id) {
        for (HashMap<Integer, Object> hashmap : allItems.values()) {
            hashmap.remove(id);
        }
    }
}

