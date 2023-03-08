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
import exceptions.NoSuchTaskExistsException;
import model.*;
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
        HistoryManager restoredHistoryManager;
        TreeSet<Task> restoredPrioritizedItems = new TreeSet<>(new TaskStartTimeComparator());
        int restoredIdCounter;
        KVTaskClient client = new KVTaskClient(host);

        //Восстанавливаем задачи
        restoreItemsByTypeWithPriorities(restoredPrioritizedItems,
                restoredAllItems,
                client,
                ItemType.TASK);
        //Восстанавливаем эпики
        restoreItemsByTypeWithPriorities(restoredPrioritizedItems,
                restoredAllItems,
                client,
                ItemType.EPIC);
        //Восстанавливаем подзадачи
        restoreItemsByTypeWithPriorities(restoredPrioritizedItems,
                restoredAllItems,
                client,
                ItemType.SUBTASK);
        //Восстанавливаем idCounter
        restoredIdCounter = restoreIdCounter(restoredAllItems);
        //Восстанавливаем историю
        restoredHistoryManager = restoreHistoryFromJson(client);

        return new HttpTaskManager(restoredIdCounter,
                restoredAllItems,
                restoredHistoryManager,
                restoredPrioritizedItems,
                host);
    }

    private static void restoreItemsByTypeWithPriorities
            (TreeSet<Task> restoredPrioritizedItems,
             HashMap<ItemType, HashMap<Integer, Task>> restoredAllItems,
             KVTaskClient client,
             ItemType itemType) throws IOException, InterruptedException {

        try {
            HashMap<Integer, Task> restoredItems = new HashMap<>();
            JsonElement loadedTasksJson = JsonParser.parseString(client.load(getKVTaskClientKeyByItemType(itemType)));
            for (JsonElement jsonTask : loadedTasksJson.getAsJsonArray()) {
                Task currentTask = gson.fromJson(jsonTask, getJsonParseTypeByItemType(itemType));
                restoredItems.put(currentTask.getId(), currentTask);
                if (itemType.equals(ItemType.TASK) || itemType.equals(ItemType.SUBTASK)) {
                    restoredPrioritizedItems.add(currentTask);
                }
            }
            restoredAllItems.put(itemType, restoredItems);
        } catch (IncorrectLoadFromServerRequestException e) {
            //При загрузке менеджера с сервера игнорируем возможное отсутствие полей
        }
    }

    private static KVTaskClient.Key getKVTaskClientKeyByItemType(ItemType itemType) {
        switch (itemType) {
            case TASK:
                return KVTaskClient.Key.TASKS;
            case SUBTASK:
                return KVTaskClient.Key.SUBTASKS;
            case EPIC:
                return KVTaskClient.Key.EPICS;
        }
        return null;
    }

    private static Type getJsonParseTypeByItemType(ItemType itemType) {
        switch (itemType) {
            case TASK:
                return new TypeToken<Task>() {
                }.getType();
            case SUBTASK:
                return new TypeToken<Subtask>() {
                }.getType();
            case EPIC:
                return new TypeToken<Epic>() {
                }.getType();
        }
        return null;
    }

    private static HistoryManager restoreHistoryFromJson(KVTaskClient client) throws IOException, InterruptedException {
        HistoryManager restoredHistoryManager = Managers.getDefaultHistory();
        try {
            JsonElement historyItemsJson = JsonParser.parseString(client.load(KVTaskClient.Key.HISTORY));
            for (JsonElement item : historyItemsJson.getAsJsonArray()) {
                String itemType = item.getAsJsonObject().get("itemType").getAsString();
                if (itemType.equals(ItemType.TASK.toString())) {
                    restoredHistoryManager.add(gson.fromJson(item, getJsonParseTypeByItemType(ItemType.TASK)));
                } else if (itemType.equals(ItemType.SUBTASK.toString())) {
                    restoredHistoryManager.add(gson.fromJson(item, getJsonParseTypeByItemType(ItemType.SUBTASK)));
                } else if (itemType.equals(ItemType.EPIC.toString())) {
                    restoredHistoryManager.add(gson.fromJson(item, getJsonParseTypeByItemType(ItemType.EPIC)));
                }
            }
        } catch (IncorrectLoadFromServerRequestException e) {
            //При загрузке менеджера с сервера игнорируем возможное отсутствие полей
        }
        return restoredHistoryManager;
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
            throw new NoSuchTaskExistsException("Нет задачи с таким id");
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
