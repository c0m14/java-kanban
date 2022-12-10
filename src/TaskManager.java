import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private int idCounter;
    private HashMap<String, HashMap<Integer, Object>> allItems;

    public TaskManager() {
        this.idCounter = 1;
        this.allItems = new HashMap<>();
    }

    public void createItem(Object anyItem){
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
        if (anyItem instanceof Subtask) {
            if (allItems.get("Subtask") != null){
                items = allItems.get("Subtask");
            } else {
                items = new HashMap<>();
            }
            items.put(idCounter, anyItem);
            allItems.put("Subtask", items);
        }
        if (anyItem instanceof Epic) {
            if (allItems.get("Epic") != null) {
                items = allItems.get("Epic");
            } else {
                items = new HashMap<>();
            }
            items.put(idCounter, anyItem);
            allItems.put("Epic", items);
        }
        idCounter++;
    }

    public int getIdCounter() {
        return idCounter;
    }

    public ArrayList<Object> getAllTasks(String itemType) {
        ArrayList<Object> itemsByChosenType = new ArrayList<>();
        for (Object item : allItems.get(itemType).values()) {
            itemsByChosenType.add(item);
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

    public void removeItemById(Integer id) {
        for (HashMap<Integer, Object> hashmap : allItems.values()) {
            hashmap.remove(id);
        }
    }

    public void removeAllItemsByType (String itemType) {
        allItems.get(itemType).clear();
    }

    public Object getItemById (Integer id) {
        Object item = new Object();
        for (HashMap<Integer, Object> hashmap : allItems.values()) {
            if (hashmap.get(id) != null) {
                item = hashmap.get(id);
            }
        }
        return item;
    }
}

