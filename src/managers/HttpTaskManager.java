package managers;

import adapters.DurationAdapter;
import adapters.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import exceptions.NoSuchTaskExists;
import model.Epic;
import model.ItemType;
import model.Subtask;
import model.Task;
import server.KVTaskClient;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Менеджер сохраняет состояние на сервере по ключам в enum Keys:
 * три ключа для разных типов задач
 * один ключ для истории
 */

public class HttpTaskManager extends FileBackedTaskManager {

    private KVTaskClient kvTaskClient;
    private Gson gson;

    public HttpTaskManager(String host) throws InterruptedException, IOException {
        super();
        kvTaskClient = new KVTaskClient(host);
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
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
    }

    private void saveHistoryToServer() throws IOException, InterruptedException {
        String json = "";
        if (super.getAllItemsByType(ItemType.TASK) != null) {
            json = gson.toJson(super.historyManager.getHistory());
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
