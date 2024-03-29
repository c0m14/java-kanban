package managers;

import exceptions.NoSuchTaskExistsException;
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
        this.prioritizedItems = new TreeSet<>(new TaskStartTimeComparator());
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
        ItemType anyItemType = anyItem.getItemType();
        boolean isItemTaskOrSubtask = anyItemType.equals(ItemType.TASK) || anyItemType.equals(ItemType.SUBTASK);

        if (isItemTaskOrSubtask) {
            checkIntervalAvailability(anyItem);
        }
        items = allItems.getOrDefault(anyItemType, new HashMap<>());
        items.put(idCounter, anyItem);
        allItems.put(anyItemType, items);
        if (isItemTaskOrSubtask) {
            prioritizedItems.add(anyItem);
        }
        if (anyItemType.equals(ItemType.SUBTASK) && ((Subtask) anyItem).getEpicId() != 0) {
            updateEpicStatus(((Subtask) anyItem).getEpicId());
            updateEpicStartTimeDurationEndTime(((Subtask) anyItem).getEpicId());
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
        return new ArrayList<>(allItems.get(itemType).values());
    }

    @Override
    public void updateItem(Task anyItem, int id) throws NoSuchTaskExistsException {
        HashMap<Integer, Task> items;
        ItemType anyItemType = anyItem.getItemType();
        boolean isItemTaskOrSubtask = anyItemType.equals(ItemType.TASK) || anyItemType.equals(ItemType.SUBTASK);
        if (getItemByIdWithoutSavingHistory(id) == null) {
            throw new NoSuchTaskExistsException("Задача с указанным Id не существует");
        }
        Task itemToChange = getItemByIdWithoutSavingHistory(anyItem.getId());
        prioritizedItems.remove(itemToChange);

        if (isItemTaskOrSubtask) {
            checkIntervalAvailability(anyItem);
            if (anyItemType.equals(ItemType.SUBTASK) && ((Subtask) anyItem).getEpicId() != 0) {
                int epicId = ((Subtask) anyItem).getEpicId();
                updateEpicStatus(epicId);
                updateEpicStartTimeDurationEndTime(epicId);
            }
            items = allItems.get(anyItemType);
            items.put(id, anyItem);
            allItems.put(anyItemType, items);
            prioritizedItems.add(anyItem);
        }
    }

    @Override
    public void removeItemById(int id) throws NoSuchTaskExistsException {
        if (getItemByIdWithoutSavingHistory(id) == null) {
            throw new NoSuchTaskExistsException("Нет задачи с таким id");
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
        } else if (currItem.getClass() == Epic.class) {
            Epic currEpic = (Epic) currItem;
            List<Integer> currEpicSubtasksIds = new ArrayList<>(currEpic.getEpicSubtaskIds());
            if (currEpicSubtasksIds.size() > 0) {
                for (Integer epicSubtaskId : currEpicSubtasksIds) {
                    removeItemById(epicSubtaskId);
                }
            }
        }
        historyManager.remove(id);
        allItems.get(currItem.getItemType()).remove(id);
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
    public Task getItemById(int id) throws NoSuchTaskExistsException {
        if (getItemByIdWithoutSavingHistory(id) == null) {
            throw new NoSuchTaskExistsException("Нет задачи с таким id");
        }
        Task item = getItemByIdWithoutSavingHistory(id);
        historyManager.add(item);
        return item;
    }

    public Task getItemByIdWithoutSavingHistory(int id) throws NoSuchTaskExistsException {
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
        if (allItems.get(ItemType.SUBTASK) == null) {
            return epicSubtasks;
        }
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
            tasks.addAll(entrySet.values());
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

