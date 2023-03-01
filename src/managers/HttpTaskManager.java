package managers;

import adapters.DurationAdapter;
import adapters.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import exceptions.IncorrectLoadFromServerRequestException;
import exceptions.ManagerSaveException;
import exceptions.NoSuchTaskExists;
import model.Epic;
import model.ItemType;
import model.Subtask;
import model.Task;
import server.KVTaskClient;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * Менеджер сохраняет состояние на сервере по ключам в enum Keys:
 * три ключа для разных типов задач
 * один ключ для истории
 */

public class HttpTaskManager extends FileBackedTaskManager {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();
    private final KVTaskClient kvTaskClient;

    public HttpTaskManager(String host) throws InterruptedException, IOException {
        super();
        kvTaskClient = new KVTaskClient(host);
    }

    private HttpTaskManager(int idCounter,
                            HashMap<ItemType, HashMap<Integer, Task>> allItems,
                            HistoryManager historyManager,
                            TreeSet<Task> prioritizedItems,
                            String host) throws IOException, InterruptedException {
        super(idCounter, allItems, historyManager, prioritizedItems);
        kvTaskClient = new KVTaskClient(host);
    }

    public static HttpTaskManager loadFromServer(String host) throws InterruptedException, IOException {
        HashMap<ItemType, HashMap<Integer, Task>> restoredAllItems = new HashMap<>();
        HistoryManager restoredHistoryManager = Managers.getDefaultHistory();
        TreeSet<Task> restoredPrioritizedItems = new TreeSet<>((task1, task2) -> {
            if (task1.getStartTime() == null) {
                return 1;
            } else if (task2.getStartTime() == null) {
                return -1;
            } else {
                return task1.getStartTime().compareTo(task2.getStartTime());
            }
        });
        int restoredIdCounter = -1;
        KVTaskClient client = new KVTaskClient(host);
        Type taskType = new TypeToken<Task>() {
        }.getType();
        Type subtaskType = new TypeToken<Subtask>() {
        }.getType();
        Type epicType = new TypeToken<Epic>() {
        }.getType();

        //Восстанавливаем задачи
        try {
            HashMap<Integer, Task> restoredTasks = new HashMap<>();
            JsonElement loadedTasksJson = JsonParser.parseString(client.load(KVTaskClient.Key.TASKS));
            for (JsonElement jsonTask : loadedTasksJson.getAsJsonArray()) {
                Task currentTask = gson.fromJson(jsonTask, taskType);
                restoredTasks.put(currentTask.getId(), currentTask);
                restoredPrioritizedItems.add(currentTask);

            }
            restoredAllItems.put(ItemType.TASK, restoredTasks);
        } catch (IncorrectLoadFromServerRequestException e) {
            //При загрузке менеджера с сервера игнорируем возможное отсутствие полей
        }


        //Восстанавливаем эпики
        try {
            HashMap<Integer, Task> restoredEpics = new HashMap<>();
            JsonElement loadedEpicsJson = JsonParser.parseString(client.load(KVTaskClient.Key.EPICS));
            for (JsonElement jsonEpic : loadedEpicsJson.getAsJsonArray()) {
                Task currentEpic = gson.fromJson(jsonEpic, epicType);
                restoredEpics.put(currentEpic.getId(), currentEpic);
            }
            restoredAllItems.put(ItemType.EPIC, restoredEpics);
        } catch (IncorrectLoadFromServerRequestException e) {
            //При загрузке менеджера с сервера игнорируем возможное отсутствие полей
        }

        //Восстанавливаем подзадачи
        try {
            HashMap<Integer, Task> restoredSubtasks = new HashMap<>();
            JsonElement loadedSubtasksJson = JsonParser.parseString(client.load(KVTaskClient.Key.SUBTASKS));
            for (JsonElement jsonSubtask : loadedSubtasksJson.getAsJsonArray()) {
                Task currentSubtask = gson.fromJson(jsonSubtask, subtaskType);
                restoredSubtasks.put(currentSubtask.getId(), currentSubtask);
                restoredPrioritizedItems.add(currentSubtask);
            }
            restoredAllItems.put(ItemType.SUBTASK, restoredSubtasks);
        } catch (IncorrectLoadFromServerRequestException e) {
            //При загрузке менеджера с сервера игнорируем возможное отсутствие полей
        }


        //Восстанавливаем idCounter
        if (!restoredAllItems.isEmpty()) {
            for (HashMap<Integer, Task> value : restoredAllItems.values()) {
                for (Integer id : value.keySet()) {
                    restoredIdCounter = Integer.max(id, restoredIdCounter);
                }
            }
        }

        //Восстанавливаем историю
        try {
            JsonElement historyItemsJson = JsonParser.parseString(client.load(KVTaskClient.Key.HISTORY));
            for (JsonElement item : historyItemsJson.getAsJsonArray()) {
                String itemType = item.getAsJsonObject().get("itemType").getAsString();
                if (itemType.equals(ItemType.TASK.toString())) {
                    restoredHistoryManager.add(gson.fromJson(item, taskType));
                } else if (itemType.equals(ItemType.SUBTASK.toString())) {
                    restoredHistoryManager.add(gson.fromJson(item, subtaskType));
                } else if (itemType.equals(ItemType.EPIC.toString())) {
                    restoredHistoryManager.add(gson.fromJson(item, epicType));
                }
            }
        } catch (IncorrectLoadFromServerRequestException e) {
            //При загрузке менеджера с сервера игнорируем возможное отсутствие полей
        }

        return new HttpTaskManager(restoredIdCounter,
                restoredAllItems,
                restoredHistoryManager,
                restoredPrioritizedItems,
                host);
    }

    @Override
    public int createItem(Task anyItem) {
        int id = super.createItem(anyItem);
        try {
            saveItemsToServer(anyItem.getItemType());
        } catch (IOException | InterruptedException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
        return id;
    }

    @Override
    public void updateItem(Task anyItem, int id) {
        super.updateItem(anyItem, id);
        try {
            saveItemsToServer(anyItem.getItemType());
        } catch (IOException | InterruptedException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    public void removeItemById(int id) {
        if (getItemByIdWithoutSavingHistory(id) == null) {
            throw new NoSuchTaskExists("Нет задачи с таким id");
        }
        ItemType itemType = super.getItemByIdWithoutSavingHistory(id).getItemType();
        super.removeItemById(id);
        try {
            saveItemsToServer(itemType);
        } catch (IOException | InterruptedException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    public void removeAllItemsByType(ItemType itemType) {
        super.removeAllItemsByType(itemType);
        try {
            saveItemsToServer(itemType);
        } catch (IOException | InterruptedException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    public Task getItemById(int id) {
        Task task = super.getItemById(id);
        try {
            saveHistoryToServer();
        } catch (IOException | InterruptedException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
        return task;
    }

    @Override
    public void linkSubtaskToEpic(Subtask subtask, Epic epic) {
        super.linkSubtaskToEpic(subtask, epic);
        try {
            saveItemsToServer(ItemType.SUBTASK);
            saveItemsToServer(ItemType.EPIC);
        } catch (IOException | InterruptedException e) {
            throw new ManagerSaveException("Ошибка сохранения задач на сервер");
        }
    }

    private void saveHistoryToServer() throws IOException, InterruptedException {
        String json = "";
        if (historyManager.getHistory() != null) {
            json = gson.toJson(historyManager.getHistory());
        } else {
            json = "[{}]";
        }
        kvTaskClient.put(KVTaskClient.Key.HISTORY, json);
    }

    public KVTaskClient getKvTaskClient() {
        return kvTaskClient;
    }

    private void saveItemsToServer(ItemType itemType) throws IOException, InterruptedException {
        String json = "";
        if (super.getAllItemsByType(itemType) != null) {
            json = gson.toJson(super.getAllItemsByType(itemType));
        } else {
            json = "[{}]";
        }
        switch (itemType) {
            case TASK:
                kvTaskClient.put(KVTaskClient.Key.TASKS, json);
                break;
            case SUBTASK:
                kvTaskClient.put(KVTaskClient.Key.SUBTASKS, json);
                break;
            case EPIC:
                kvTaskClient.put(KVTaskClient.Key.EPICS, json);
                break;
        }
    }

}
