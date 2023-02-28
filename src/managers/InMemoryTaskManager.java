package managers;

import exceptions.NoSuchTaskExists;
import exceptions.TaskTimeIntersectionException;
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
        this.prioritizedItems = new TreeSet<>((task1, task2) -> {
            if (task1.getStartTime() == null) {
                return 1;
            } else if (task2.getStartTime() == null) {
                return -1;
            } else {
                return task1.getStartTime().compareTo(task2.getStartTime());
            }
        });
    }

    public InMemoryTaskManager(int idCounter,
                               HashMap<ItemType, HashMap<Integer, Task>> allItems,
                               HistoryManager historyManager,
                               TreeSet<Task> prioritizedItems) {
        this.idCounter = idCounter;
        this.allItems = allItems;
        this.historyManager = historyManager;
        this.prioritizedItems = prioritizedItems;
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    @Override
    public int createItem(Task anyItem) {
        HashMap<Integer, Task> items;
        if (anyItem.getClass() == Task.class) {
            checkIntervalAvailability(anyItem);
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
            checkIntervalAvailability(anyItem);
            if (allItems.get(ItemType.SUBTASK) != null) {
                items = allItems.get(ItemType.SUBTASK);
            } else {
                items = new HashMap<>();
            }
            items.put(idCounter, anyItem);
            allItems.put(ItemType.SUBTASK, items);
            checkIntervalAvailability(anyItem);
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
        anyItem.setId(idCounter);
        return idCounter++;
    }

    @Override
    public int getIdCounter() {
        return idCounter;
    }

    public void setIdCounter(int idCounter) {
        this.idCounter = idCounter;
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
    public void updateItem(Task anyItem, int id) throws NoSuchTaskExists {
        HashMap<Integer, Task> items;
        if (getItemByIdWithoutSavingHistory(id) == null) {
            throw new NoSuchTaskExists("Задача с указанным Id не существует");
        }
        Task itemToChange = getItemByIdWithoutSavingHistory(anyItem.getId());
        prioritizedItems.remove(itemToChange);
        if (anyItem.getClass() == Task.class) {
            checkIntervalAvailability(anyItem);
            items = allItems.get(ItemType.TASK);
            items.put(id, anyItem);
            allItems.put(ItemType.TASK, items);
            prioritizedItems.add(anyItem);
        }
        if (anyItem.getClass() == Subtask.class) {
            checkIntervalAvailability(anyItem);
            if (((Subtask) anyItem).getEpicId() != 0) {
                int epicId = ((Subtask) anyItem).getEpicId();
                updateEpicStatus(epicId);
                updateEpicStartTimeDurationEndTime(epicId);
            }
            items = allItems.get(ItemType.SUBTASK);
            items.put(id, anyItem);
            allItems.put(ItemType.SUBTASK, items);
            prioritizedItems.add(anyItem);
        }
        if (anyItem.getClass() == Epic.class) {
            items = allItems.get(ItemType.EPIC);
            items.put(id, anyItem);
            allItems.put(ItemType.EPIC, items);
            prioritizedItems.remove(anyItem);
            prioritizedItems.add(anyItem);
        }
    }

    @Override
    public void removeItemById(int id) throws NoSuchTaskExists {
        if (getItemByIdWithoutSavingHistory(id) == null) {
            throw new NoSuchTaskExists("Нет задачи с таким id");
        }
        Task currItem = getItemByIdWithoutSavingHistory(id);
        if (currItem.getClass() == Subtask.class) {
            Subtask currSubtask = (Subtask) currItem;
            if (currSubtask.getEpicId() != 0) {
                Epic currEpic = (Epic) getItemByIdWithoutSavingHistory(currSubtask.getEpicId());
                currEpic.deleteSubtaskById(id);
                currSubtask.setEpicId(0);
                updateEpicStatus(currEpic.getId());
                updateEpicStartTimeDurationEndTime(currEpic.getId());
            }
            historyManager.remove(id);
            allItems.get(ItemType.SUBTASK).remove(id);
        } else if (currItem.getClass() == Epic.class) {
            Epic currEpic = (Epic) currItem;
            List<Integer> currEpicSubtasksIds = new ArrayList<>(currEpic.getEpicSubtaskIds());
            if (currEpicSubtasksIds.size() > 0) {
                for (Integer epicSubtaskId : currEpicSubtasksIds) {
                    removeItemById(epicSubtaskId);
                }
            }
            historyManager.remove(id);
            allItems.get(ItemType.EPIC).remove(id);
        } else {
            historyManager.remove(id);
            allItems.get(ItemType.TASK).remove(id);
        }
        prioritizedItems.remove(currItem);
    }

    @Override
    public void removeAllItemsByType(ItemType itemType) {
        int[] ids = allItems.get(itemType).keySet().stream().mapToInt(key -> key).toArray();
        for (int id : ids) {
            removeItemById(id);
        }
    }

    @Override
    public ArrayList<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedItems);
    }

    private void checkIntervalAvailability(Task item) throws TaskTimeIntersectionException {
        List<Task> prioritizedTasksList = getPrioritizedTasks();

        if (item.getEndTime().isPresent()) {
            for (Task prioritizedTask : prioritizedTasksList) {
                if (prioritizedTask.getStartTime() != null && prioritizedTask.getDurationMinutes() != null) {
                    if (item.getStartTime().isAfter(prioritizedTask.getStartTime())
                            && item.getStartTime().isBefore(prioritizedTask.getEndTime().get())) {
                        throw new TaskTimeIntersectionException("Данное время уже занято задачей " + item);
                    }
                    if (item.getEndTime().get().isAfter(prioritizedTask.getStartTime())
                            && item.getEndTime().get().isBefore(prioritizedTask.getEndTime().get())) {
                        throw new TaskTimeIntersectionException("Данное время уже занято задачей " + item);
                    }
                }
            }
        }
    }

    @Override
    public Task getItemById(int id) {
        Task item = getItemByIdWithoutSavingHistory(id);
        historyManager.add(item);
        return item;
    }

    public Task getItemByIdWithoutSavingHistory(int id) {
        Task item = null;
        for (HashMap<Integer, Task> hashmap : allItems.values()) {
            if (hashmap.get(id) != null) {
                item = hashmap.get(id);
            }
        }
        return item;
    }

    @Override
    public ArrayList<Subtask> getEpicSubtasks(int epicId) {
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
        Epic currEpic = (Epic) getItemByIdWithoutSavingHistory(epicId);
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

    protected void updateEpicStartTimeDurationEndTime(int epicId) {
        Epic currEpic = (Epic) getItemByIdWithoutSavingHistory(epicId);
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

    @Override
    public void linkSubtaskToEpic(Subtask subtask, Epic epic) {
        epic.addSubtask(subtask);
        subtask.setEpicId(epic.getId());
        updateEpicStatus(epic.getId());
        updateEpicStartTimeDurationEndTime(epic.getId());
    }
}

