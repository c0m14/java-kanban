package managers;

import model.*;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager<T extends Task> implements TaskManager<T> {
    private static int idCounter = 1;
    private final HashMap<ItemType, HashMap<Integer, T>> allItems;
    private final HistoryManager<T> historyManager;

    public InMemoryTaskManager() {
        this.allItems = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
    }

    public HistoryManager<T> getHistoryManager() {
        return historyManager;
    }

    @Override
    public int createItem(T anyItem) {
        HashMap<Integer, T> items;
        if (anyItem.getClass() == Task.class) {
            if (allItems.get(ItemType.TASK) != null) {
                items = allItems.get(ItemType.TASK);
            } else {
                items = new HashMap<>();
            }
            items.put(idCounter, anyItem);
            allItems.put(ItemType.TASK, items);
        }
        if (anyItem.getClass() == Subtask.class) {
            if (allItems.get(ItemType.SUBTASK) != null) {
                items = allItems.get(ItemType.SUBTASK);
            } else {
                items = new HashMap<>();
            }
            items.put(idCounter, anyItem);
            allItems.put(ItemType.SUBTASK, items);
        }
        if (anyItem.getClass() == Epic.class) {
            if (allItems.get(ItemType.EPIC) != null) {
                items = allItems.get(ItemType.EPIC);
            } else {
                items = new HashMap<>();
            }
            items.put(idCounter, anyItem);
            allItems.put(ItemType.EPIC, items);
        }
        return idCounter++;
    }

    @Override
    public int getIdCounter() {
        return idCounter;
    }

    @Override
    public ArrayList<T> getAllItemsByType(ItemType itemType) {
        ArrayList<T> itemsByChosenType = new ArrayList<>();
        for (T item : allItems.get(itemType).values()) {
            itemsByChosenType.add(item);
        }
        return itemsByChosenType;
    }

    @Override
    public void updateItem(T anyItem, int id) {
        HashMap<Integer, T> items;
        if (anyItem.getClass() == Task.class) {
            items = allItems.get(ItemType.TASK);
            items.put(id, anyItem);
            allItems.put(ItemType.TASK, items);
        }
        if (anyItem.getClass() == Subtask.class) {
            Subtask newSubtask = (Subtask) anyItem;
            int epicId = newSubtask.getEpicId();
            epicUpdateStatus(epicId);

            items = allItems.get(ItemType.SUBTASK);
            items.put(id, anyItem);
            allItems.put(ItemType.SUBTASK, items);
        }
    }

    @Override
    public void removeItemById(int id) {
        if (getItemById(id).getClass() == Subtask.class) {
            Subtask currSubtask = (Subtask) getItemById(id);
            Epic currEpic = (Epic) getItemById(currSubtask.getEpicId());
            currEpic.deleteSubtaskById(id);
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
    public void removeAllItemsByType(ItemType itemType) {
        if (itemType.equals(ItemType.SUBTASK)) {
            ArrayList<Integer> relatedEpicsId = new ArrayList<>();
            for (T subtask : allItems.get(itemType).values()) {
                Subtask currSubtask = (Subtask) subtask;
                if (currSubtask.getEpicId() != 0) {
                    Epic currEpic = (Epic) getItemById(currSubtask.getEpicId());
                    currEpic.deleteSubtaskById(currSubtask.getId());
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
        for (Object item : allItems.get(ItemType.SUBTASK).values()) {
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

