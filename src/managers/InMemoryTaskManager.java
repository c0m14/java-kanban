package managers;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager<T extends Task> implements TaskManager<T> {
    private static int idCounter = 1;
    private final HashMap<String, HashMap<Integer, T>> allItems;
    private final List<T> allItemsHistory;
    private final HistoryManager<T> historyManager;

    public InMemoryTaskManager() {
        this.allItems = new HashMap<>();
        this.allItemsHistory = new ArrayList<>();
        this.historyManager = Managers.getDefaultHistory();
    }

    public HistoryManager<T> getHistoryManager() {
        return historyManager;
    }

    @Override
    public int createItem(T anyItem) {
        HashMap<Integer, T> items;
        if (anyItem instanceof Task && !(anyItem instanceof Subtask) && !(anyItem instanceof Epic)) {
            if (allItems.get("model.Task") != null) {
                items = allItems.get("model.Task");
            } else {
                items = new HashMap<>();
            }
            items.put(idCounter, anyItem);
            allItems.put("model.Task", items);
        }
        if (anyItem instanceof Subtask) {
            if (allItems.get("model.Subtask") != null) {
                items = allItems.get("model.Subtask");
            } else {
                items = new HashMap<>();
            }
            items.put(idCounter, anyItem);
            allItems.put("model.Subtask", items);
        }
        if (anyItem instanceof Epic) {
            if (allItems.get("model.Epic") != null) {
                items = allItems.get("model.Epic");
            } else {
                items = new HashMap<>();
            }
            items.put(idCounter, anyItem);
            allItems.put("model.Epic", items);
        }
        return idCounter++;
    }

    @Override
    public int getIdCounter() {
        return idCounter;
    }

    @Override
    public ArrayList<T> getAllItemsByType(String itemType) {
        ArrayList<T> itemsByChosenType = new ArrayList<>();
        for (T item : allItems.get(itemType).values()) {
            itemsByChosenType.add(item);
        }
        return itemsByChosenType;
    }

    @Override
    public void updateItem(T anyItem, int id) {
        HashMap<Integer, T> items;
        if (anyItem instanceof Task && !(anyItem instanceof Subtask) && !(anyItem instanceof Epic)) {
            items = allItems.get("model.Task");
            items.put(id, anyItem);
            allItems.put("model.Task", items);
        }
        if (anyItem instanceof Subtask) {
            Subtask newSubtask = (Subtask) anyItem;
            int epicId = newSubtask.getEpicId();
            epicUpdateStatus(epicId);

            items = allItems.get("model.Subtask");
            items.put(id, anyItem);
            allItems.put("model.Subtask", items);
        }
    }

    @Override
    public void removeItemById(int id) {
        if (getItemById(id) instanceof Subtask) {
            Subtask currSubtask = (Subtask) getItemById(id);
            Epic currEpic = (Epic) getItemById(currSubtask.getEpicId());
            currEpic.deleteSubtask(currSubtask);
            for (HashMap<Integer, T> hashmap : allItems.values()) {
                hashmap.remove(id);
            }
            epicUpdateStatus(currEpic.getId());
        } else {
            for (HashMap<Integer, T> hashmap : allItems.values()) {
                hashmap.remove(id);
            }
        }
    }

    @Override
    public void removeAllItemsByType(String itemType) {
        if (itemType.equals("model.Subtask")) {
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

    public T getItemById(int id) {
        T item = null;
        for (HashMap<Integer, T> hashmap : allItems.values()) {
            if (hashmap.get(id) != null) {
                item = hashmap.get(id);
            }
        }
        historyManager.add(item);
        return item;
    }

    private ArrayList<Subtask> getEpicSubtasks(int epicId) {
        ArrayList<Subtask> epicSubtasks = new ArrayList<>();
        for (Object item : allItems.get("model.Subtask").values()) {
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
            currEpic.setStatus(Status.NEW);
        } else {
            for (Subtask epicSubtask : getEpicSubtasks(epicId)) {
                if (epicSubtask.getStatus().equals(Status.NEW)) {
                    currEpic.setStatus((Status.NEW));
                } else if (epicSubtask.getStatus().equals(Status.DONE)) {
                    currEpic.setStatus((Status.DONE));
                }
            }
            for (Subtask epicSubtask : getEpicSubtasks(epicId)) {
                if (!currEpic.getStatus().equals(epicSubtask.getStatus())) {
                    currEpic.setStatus(Status.IN_PROGRESS);
                    break;
                }
            }
        }
    }
}

