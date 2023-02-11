package managers;

import model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<ItemType, HashMap<Integer, Task>> allItems;
    protected final HistoryManager historyManager;
    protected int idCounter = 1;
    protected TreeSet<Task> prioritizedItems;

    public InMemoryTaskManager() {
        this.allItems = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
        this.prioritizedItems = new TreeSet<>(new Comparator<Task>() {
            @Override
            public int compare(Task task1, Task task2) {
                if (task1.getStartTime() == null && task2.getStartTime() == null) {
                    return 2;
                } else if (task1.getStartTime() == null && task2.getStartTime() != null) {
                    return 1;
                } else if (task1.getStartTime() != null && task2.getStartTime() == null) {
                    return -1;
                } else return task1.getStartTime().compareTo(task2.getStartTime());
            }
        });
    }

    public InMemoryTaskManager(int idCounter,
                               HashMap<ItemType,
                                       HashMap<Integer,
                                               Task>> allItems,
                               HistoryManager historyManager) {
        this.idCounter = idCounter;
        this.allItems = allItems;
        this.historyManager = historyManager;
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    @Override
    public int createItem(Task anyItem) {
        HashMap<Integer, Task> items;
        if (anyItem.getClass() == Task.class) {
            if (allItems.get(ItemType.TASK) != null) {
                items = allItems.get(ItemType.TASK);
            } else {
                items = new HashMap<>();
            }
            items.put(idCounter, anyItem);
            allItems.put(ItemType.TASK, items);
            prioritizedItems.add(anyItem);
        }
        if (anyItem.getClass() == Subtask.class) {
            if (allItems.get(ItemType.SUBTASK) != null) {
                items = allItems.get(ItemType.SUBTASK);
            } else {
                items = new HashMap<>();
            }
            items.put(idCounter, anyItem);
            allItems.put(ItemType.SUBTASK, items);
            prioritizedItems.add(anyItem);

            if (((Subtask) anyItem).getEpicId() != 0) {
                updateEpicStatus(((Subtask) anyItem).getEpicId());
                updateEpicStartTimeDurationEndTime(((Subtask) anyItem).getEpicId());
            }
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
    public ArrayList<Task> getAllItemsByType(ItemType itemType) {
        ArrayList<Task> itemsByChosenType = new ArrayList<>();
        for (Task item : allItems.get(itemType).values()) {
            itemsByChosenType.add(item);
        }
        return itemsByChosenType;
    }

    @Override
    public void updateItem(Task anyItem, int id) {
        HashMap<Integer, Task> items;
        if (anyItem.getClass() == Task.class) {
            items = allItems.get(ItemType.TASK);
            items.put(id, anyItem);
            allItems.put(ItemType.TASK, items);
        }
        if (anyItem.getClass() == Subtask.class) {
            Subtask newSubtask = (Subtask) anyItem;
            int epicId = newSubtask.getEpicId();
            updateEpicStatus(epicId);
            updateEpicStartTimeDurationEndTime(epicId);

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
            for (HashMap<Integer, Task> hashmap : allItems.values()) {
                hashmap.remove(id);
            }
            updateEpicStatus(currEpic.getId());
            updateEpicStartTimeDurationEndTime(currEpic.getId());
        } else if (getItemById(id).getClass() == Epic.class) {
            Epic currEpic = (Epic) getItemById(id);
            List<Integer> currEpicSubtasksIds = new ArrayList<>(currEpic.getEpicSubtaskIds());
            for (Integer epicSubtaskId : currEpicSubtasksIds) {
                allItems.remove(epicSubtaskId);
                historyManager.remove(epicSubtaskId);
            }
            for (HashMap<Integer, Task> hashmap : allItems.values()) {
                hashmap.remove(id);
            }
        } else {
            for (HashMap<Integer, Task> hashmap : allItems.values()) {
                hashmap.remove(id);
            }
        }
        historyManager.remove(id);
        prioritizedItems.remove(getItemById(id));
    }

    @Override
    public void removeAllItemsByType(ItemType itemType) {
        if (itemType.equals(ItemType.SUBTASK)) {
            ArrayList<Integer> relatedEpicsId = new ArrayList<>();
            for (Task subtask : allItems.get(itemType).values()) {
                Subtask currSubtask = (Subtask) subtask;
                if (currSubtask.getEpicId() != 0) {
                    Epic currEpic = (Epic) getItemById(currSubtask.getEpicId());
                    currEpic.deleteSubtaskById(currSubtask.getId());
                    relatedEpicsId.add(currEpic.getId());
                }
                historyManager.remove(currSubtask.getId());
                prioritizedItems.remove(currSubtask);
            }
            allItems.get(itemType).clear();
            for (Integer id : relatedEpicsId) {
                updateEpicStatus(id);
                updateEpicStartTimeDurationEndTime(id);
            }
        } else {
            for (Integer itemId : allItems.get(itemType).keySet()) {
                historyManager.remove(itemId);
                prioritizedItems.remove(getItemById(itemId));
            }
            allItems.get(itemType).clear();
        }
    }

    @Override
    public ArrayList<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedItems);
    }

    public Task getItemById(int id) {
        Task item = null;
        for (HashMap<Integer, Task> hashmap : allItems.values()) {
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

    private void updateEpicStatus(int epicId) {
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

    public void updateEpicStartTimeDurationEndTime(int epicId) {
        Epic currEpic = (Epic) getItemById(epicId);
        if (!getEpicSubtasks(epicId).isEmpty()) {
            //Обновляем startTime
            getEpicSubtasks(epicId).stream()
                    .map(Task::getStartTime)
                    .filter(Objects::nonNull)
                    .min(LocalDateTime::compareTo)
                    .ifPresent(currEpic::setStartTime);

            //Обновляем duration
            long epicDuration = getEpicSubtasks(epicId).stream()
                    .filter(subtask -> subtask.getDurationMinutes() != null)
                    .mapToLong(subtask -> subtask.getDurationMinutes().toMinutes())
                    .sum();
            currEpic.setDurationMinutes(Duration.of(epicDuration, ChronoUnit.MINUTES));

            //Обновляем endTime;
            getEpicSubtasks(epicId).stream()
                    .filter(subtask -> subtask.getEndTime().isPresent())
                    .map(subtask -> subtask.getEndTime().get())
                    .max(LocalDateTime::compareTo)
                    .ifPresent(currEpic::setEndTime);
        }
    }

    protected ArrayList<Task> getAllItemsOfAllTypes() {
        ArrayList<Task> tasks = new ArrayList<>();
        for (HashMap<Integer, Task> entrySet : allItems.values()) {
            for (Task task : entrySet.values()) {
                tasks.add(task);
            }
        }
        return tasks;
    }

    public void addSubtask(Subtask subtask, Epic epic) {
        epic.addSubtask(subtask);
        subtask.setEpicId(epic.getId());
    }
}

