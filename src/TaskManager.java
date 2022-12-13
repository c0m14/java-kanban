import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private int idCounter;
    private HashMap<String, HashMap<Integer, Object>> allItems;

    public TaskManager() {
        this.idCounter = 1;
        this.allItems = new HashMap<>();
    }

    public void createItem(Object anyItem) {
        HashMap<Integer, Object> items;
        if (anyItem instanceof Task && !(anyItem instanceof Subtask) && !(anyItem instanceof Epic)) {
            if (allItems.get("Task") != null) {
                items = allItems.get("Task");
            } else {
                items = new HashMap<>();
            }
            items.put(idCounter, anyItem);
            allItems.put("Task", items);
        }
        if (anyItem instanceof Subtask) {
            if (allItems.get("Subtask") != null) {
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

    public ArrayList<Object> getAllItemsByType(String itemType) {
        ArrayList<Object> itemsByChosenType = new ArrayList<>();
        for (Object item : allItems.get(itemType).values()) {
            itemsByChosenType.add(item);
        }
        return itemsByChosenType;
    }

    public void updateTask(Object anyItem, int id) {
        HashMap<Integer, Object> items;
        if (anyItem instanceof Task && !(anyItem instanceof Subtask) && !(anyItem instanceof Epic)) {
            items = allItems.get("Task");
            items.put(id, anyItem);
            allItems.put("Task", items);
        }
        if (anyItem instanceof Subtask) {
            Subtask newSubtask = (Subtask) anyItem;
            int epicId = newSubtask.getEpicId();
            epicUpdateStatus(epicId);

            items = allItems.get("Subtask");
            items.put(id, anyItem);
            allItems.put("Subtask", items);
        }
    }

    public void removeItemById(int id) {
        if (getItemById(id) instanceof Subtask) {
            Subtask currSubtask = (Subtask) getItemById(id);
            Epic currEpic = (Epic) getItemById(currSubtask.getEpicId());
            currEpic.deleteSubtask(currSubtask);
            for (HashMap<Integer, Object> hashmap : allItems.values()) {
                hashmap.remove(id);
            }
            epicUpdateStatus(currEpic.getId());
        } else {
            for (HashMap<Integer, Object> hashmap : allItems.values()) {
                hashmap.remove(id);
            }
        }
    }

    public void removeAllItemsByType(String itemType) {
        if (itemType.equals("Subtask")) {
            ArrayList<Integer> relatedEpicsId = new ArrayList<>();
            for (Object subtask : allItems.get(itemType).values()) {
                Subtask currSubtask = (Subtask) subtask;
                if (currSubtask.getEpicId() != 0) {
                    Epic currEpic = (Epic) getItemById(currSubtask.getEpicId());
                    currEpic.deleteSubtask(currSubtask);
                    relatedEpicsId.add(currEpic.getId());
                }
            }
            allItems.get(itemType).clear();
            for (Integer id : relatedEpicsId) {
                epicUpdateStatus(id);
            }
        } else {
            allItems.get(itemType).clear();
        }
    }

    private Object getItemById(int id) {
        Object item = new Object();
        for (HashMap<Integer, Object> hashmap : allItems.values()) {
            if (hashmap.get(id) != null) {
                item = hashmap.get(id);
            }
        }
        return item;
    }

    private ArrayList<Subtask> getEpicSubtasks(int epicId) {
        ArrayList<Subtask> epicSubtasks = new ArrayList<>();
        for (Object item : allItems.get("Subtask").values()) {
            Subtask subtask = (Subtask) item;
            if (subtask.getEpicId() == epicId) {
                epicSubtasks.add(subtask);
            }
        }
        return epicSubtasks;
    }

    private void epicUpdateStatus(int epicId) {
        Epic currEpic = (Epic) getItemById(epicId);
        if (getEpicSubtasks(epicId).isEmpty()) {
            currEpic.setStatus("NEW");
        } else {
            for (Subtask epicSubtask : getEpicSubtasks(epicId)) {
                if (epicSubtask.getStatus().equals("NEW")) {
                    currEpic.setStatus(("NEW"));
                } else if (epicSubtask.getStatus().equals("DONE")) {
                    currEpic.setStatus(("DONE"));
                }
            }
            for (Subtask epicSubtask : getEpicSubtasks(epicId)) {
                if (!currEpic.getStatus().equals(epicSubtask.getStatus())) {
                    currEpic.setStatus("IN_PROGRESS");
                    break;
                }
            }
        }
    }
}

