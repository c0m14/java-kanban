package managers;

import adapters.DurationAdapter;
import adapters.LocalDateTimeAdapter;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import model.Epic;
import model.ItemType;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.KVServer;
import server.KVTaskClient;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTest extends TaskManagerTest<HttpTaskManager> {

    private final String host = "http://localhost:8080";
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();
    private KVServer kvServer;

    protected void setTaskManager() {
        try {
            taskManager = (HttpTaskManager) Managers.getDefault(host);
        } catch (InterruptedException | IOException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }

    @BeforeEach
    public void beforeEach() {
        try {
            kvServer = new KVServer();
        } catch (IOException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
        kvServer.start();
        setTaskManager();
    }

    @AfterEach
    public void afterEach() {
        kvServer.stop();
    }

    //тесты на сохранение состояния на сервере
    @Test
    public void shouldSaveToServerWhenTaskIsCreated() throws IOException, InterruptedException{
        Task task = new Task("task", "task description");
        task.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        task.setDurationMinutes(Duration.ofMinutes(60));

        taskManager.createItem(task);
        Type taskArrayListType = new TypeToken<ArrayList<Task>>() {}.getType();
        String savedData = taskManager.getKvTaskClient().load(KVTaskClient.Key.TASKS);
        ArrayList<Task> requestedItems = gson.fromJson(savedData, taskArrayListType);

        assertTrue(requestedItems.contains(task), "Задача не найдена на сервере");
        assertEquals(1, requestedItems.size(), "Список задач на сервере не полный");
    }

    @Test
    public void shouldSaveToServerWhenSubtaskIsCreated() throws IOException, InterruptedException{
        Subtask subtask = new Subtask("subtask", "subtask description");
        subtask.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        subtask.setDurationMinutes(Duration.ofMinutes(60));

        taskManager.createItem(subtask);
        Type subtaskArrayListType = new TypeToken<ArrayList<Subtask>>() {}.getType();
        String savedData = taskManager.getKvTaskClient().load(KVTaskClient.Key.SUBTASKS);
        ArrayList<Subtask> requestedItems = gson.fromJson(savedData, subtaskArrayListType);

        assertTrue(requestedItems.contains(subtask), "Задача не найдена на сервере");
        assertEquals(1, requestedItems.size(), "Список задач на сервере не полный");
    }

    @Test
    public void shouldSaveToServerWhenEpicIsCreated() throws IOException, InterruptedException{
        Epic epic = new Epic("epic", "epic description");

        taskManager.createItem(epic);
        Type subtaskArrayListType = new TypeToken<ArrayList<Epic>>() {}.getType();
        String savedData = taskManager.getKvTaskClient().load(KVTaskClient.Key.EPICS);
        ArrayList<Epic> requestedItems = gson.fromJson(savedData, subtaskArrayListType);

        assertTrue(requestedItems.contains(epic), "Задача не найдена на сервере");
        assertEquals(1, requestedItems.size(), "Список задач на сервере не полный");
    }

    @Test
    public void shouldSaveToServerWhenTwoPlusItemsIsCreated() throws IOException, InterruptedException{
        Task task1 = new Task("task1", "task1 description");
        task1.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        task1.setDurationMinutes(Duration.ofMinutes(60));
        Task task2 = new Task("task2");

        taskManager.createItem(task1);
        taskManager.createItem(task2);
        Type taskArrayListType = new TypeToken<ArrayList<Task>>() {}.getType();
        String savedData = taskManager.getKvTaskClient().load(KVTaskClient.Key.TASKS);
        ArrayList<Task> requestedItems = gson.fromJson(savedData, taskArrayListType);

        assertTrue(requestedItems.contains(task1), "Задача не найдена на сервере");
        assertTrue(requestedItems.contains(task2), "Задача не найдена на сервере");
        assertEquals(2, requestedItems.size(), "Список задач на сервере не полный");
    }

    @Test
    public void shouldSaveToServerWhenTaskIsUpdated() throws IOException, InterruptedException{
        Task task = new Task("task", "task description");
        task.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        task.setDurationMinutes(Duration.ofMinutes(60));
        taskManager.createItem(task);
        Task updatedTask = new Task("updatedTask");
        updatedTask.setId(task.getId());

        taskManager.updateItem(updatedTask, task.getId());
        Type taskArrayListType = new TypeToken<ArrayList<Task>>() {}.getType();
        String savedData = taskManager.getKvTaskClient().load(KVTaskClient.Key.TASKS);
        ArrayList<Task> requestedItems = gson.fromJson(savedData, taskArrayListType);

        assertTrue(requestedItems.contains(updatedTask), "Задача не найдена на сервере");
        assertEquals(1, requestedItems.size(), "Список задач на сервере не полный");
    }

    @Test
    public void shouldSaveToServerWhenSubtaskIsUpdated() throws IOException, InterruptedException{
        Subtask subtask = new Subtask("subtask", "subtask description");
        subtask.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        subtask.setDurationMinutes(Duration.ofMinutes(60));
        taskManager.createItem(subtask);
        Subtask updatedSubtask = new Subtask("updatedSubtask");
        updatedSubtask.setId(subtask.getId());

        taskManager.updateItem(updatedSubtask, subtask.getId());
        Type subtaskArrayListType = new TypeToken<ArrayList<Subtask>>() {}.getType();
        String savedData = taskManager.getKvTaskClient().load(KVTaskClient.Key.SUBTASKS);
        ArrayList<Subtask> requestedItems = gson.fromJson(savedData, subtaskArrayListType);

        assertTrue(requestedItems.contains(updatedSubtask), "Задача не найдена на сервере");
        assertEquals(1, requestedItems.size(), "Список задач на сервере не полный");
    }

    @Test
    public void shouldSaveToServerWhenEpicIsUpdated() throws IOException, InterruptedException{
        Epic epic = new Epic("epic", "epic description");
        taskManager.createItem(epic);
        Epic updatedEpic = new Epic("updatedEpic");
        updatedEpic.setId(epic.getId());

        taskManager.updateItem(updatedEpic, epic.getId());
        Type subtaskArrayListType = new TypeToken<ArrayList<Epic>>() {}.getType();
        String savedData = taskManager.getKvTaskClient().load(KVTaskClient.Key.EPICS);
        ArrayList<Epic> requestedItems = gson.fromJson(savedData, subtaskArrayListType);

        assertTrue(requestedItems.contains(updatedEpic), "Задача не найдена на сервере");
        assertEquals(1, requestedItems.size(), "Список задач на сервере не полный");
    }

    @Test
    public void shouldRemoveFromServerWhenTaskIsRemovedById() throws IOException, InterruptedException{
        Task task = new Task("task", "task description");
        task.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        task.setDurationMinutes(Duration.ofMinutes(60));
        taskManager.createItem(task);

        taskManager.removeItemById(task.getId());
        Type taskArrayListType = new TypeToken<ArrayList<Task>>() {}.getType();
        String savedData = taskManager.getKvTaskClient().load(KVTaskClient.Key.TASKS);
        ArrayList<Task> requestedItems = gson.fromJson(savedData, taskArrayListType);

        assertFalse(requestedItems.contains(task), "Задача не найдена на сервере");
        assertEquals(0, requestedItems.size(), "Список задач на сервере не корректный");
    }

    @Test
    public void shouldRemoveFromServerWhenSubtaskIsRemovedById() throws IOException, InterruptedException{
        Subtask subtask = new Subtask("subtask", "subtask description");
        subtask.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        subtask.setDurationMinutes(Duration.ofMinutes(60));
        taskManager.createItem(subtask);

        taskManager.removeItemById(subtask.getId());
        Type taskArrayListType = new TypeToken<ArrayList<Subtask>>() {}.getType();
        String savedData = taskManager.getKvTaskClient().load(KVTaskClient.Key.SUBTASKS);
        ArrayList<Subtask> requestedItems = gson.fromJson(savedData, taskArrayListType);

        assertFalse(requestedItems.contains(subtask), "Задача не найдена на сервере");
        assertEquals(0, requestedItems.size(), "Список задач на сервере не корректный");
    }

    @Test
    public void shouldRemoveFromServerWhenEpicIsRemovedById() throws IOException, InterruptedException{
        Epic epic = new Epic("epic", "epic description");
        taskManager.createItem(epic);

        taskManager.removeItemById(epic.getId());
        Type taskArrayListType = new TypeToken<ArrayList<Epic>>() {}.getType();
        String savedData = taskManager.getKvTaskClient().load(KVTaskClient.Key.EPICS);
        ArrayList<Epic> requestedItems = gson.fromJson(savedData, taskArrayListType);

        assertFalse(requestedItems.contains(epic), "Задача не найдена на сервере");
        assertEquals(0, requestedItems.size(), "Список задач на сервере не корректный");
    }

    @Test
    public void shouldRemoveFromServerWhenTaskIsRemovedByType() throws IOException, InterruptedException{
        Task task1 = new Task("task1", "task1 description");
        taskManager.createItem(task1);
        Task task2 = new Task("task2");
        taskManager.createItem(task2);

        taskManager.removeAllItemsByType(ItemType.TASK);
        Type taskArrayListType = new TypeToken<ArrayList<Task>>() {}.getType();
        String savedData = taskManager.getKvTaskClient().load(KVTaskClient.Key.TASKS);
        ArrayList<Task> requestedItems = gson.fromJson(savedData, taskArrayListType);

        assertEquals(0, requestedItems.size(), "Список задач на сервере не корректный");
    }

    @Test
    public void shouldRemoveFromServerWhenSubtaskIsRemovedByType() throws IOException, InterruptedException{
        Subtask subtask1 = new Subtask("subtask1", "subtask1 description");
        taskManager.createItem(subtask1);
        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);

        taskManager.removeAllItemsByType(ItemType.SUBTASK);
        Type taskArrayListType = new TypeToken<ArrayList<Subtask>>() {}.getType();
        String savedData = taskManager.getKvTaskClient().load(KVTaskClient.Key.SUBTASKS);
        ArrayList<Subtask> requestedItems = gson.fromJson(savedData, taskArrayListType);

        assertEquals(0, requestedItems.size(), "Список задач на сервере не корректный");
    }

    @Test
    public void shouldRemoveFromServerWhenEpicIsRemovedByType() throws IOException, InterruptedException{
        Epic epic1 = new Epic("epic1", "epic1 description");
        taskManager.createItem(epic1);
        Epic epic2 = new Epic("epic2");
        taskManager.createItem(epic2);

        taskManager.removeAllItemsByType(ItemType.EPIC);
        Type taskArrayListType = new TypeToken<ArrayList<Epic>>() {}.getType();
        String savedData = taskManager.getKvTaskClient().load(KVTaskClient.Key.EPICS);
        ArrayList<Epic> requestedItems = gson.fromJson(savedData, taskArrayListType);

        assertEquals(0, requestedItems.size(), "Список задач на сервере не корректный");
    }

    //тесты на сохранение истории на сервере
    @Test
    public void shouldSaveHistoryToServer() throws IOException, InterruptedException{
        Task task = new Task("task");
        taskManager.createItem(task);
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        Subtask subtask = new Subtask("subtask");
        taskManager.createItem(subtask);
        taskManager.linkSubtaskToEpic(subtask, epic);

        taskManager.getItemById(subtask.getId());
        taskManager.getItemById(task.getId());
        taskManager.getItemById(epic.getId());

        Type taskType = new TypeToken<Task>() {
        }.getType();
        Type subtaskType = new TypeToken<Subtask>() {
        }.getType();
        Type epicType = new TypeToken<Epic>() {
        }.getType();
        JsonElement jsonElement = JsonParser.parseString(taskManager.getKvTaskClient().load(KVTaskClient.Key.HISTORY));
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        ArrayList<Task> requestedItems = new ArrayList<>();
        for (JsonElement element : jsonArray) {
            String itemType = element.getAsJsonObject().get("itemType").getAsString();
            if (itemType.equals(ItemType.TASK.toString())) {
                requestedItems.add(gson.fromJson(element, taskType));
            } else if (itemType.equals(ItemType.SUBTASK.toString())) {
                requestedItems.add(gson.fromJson(element, subtaskType));
            } else if (itemType.equals(ItemType.EPIC.toString())) {
                requestedItems.add(gson.fromJson(element, epicType));
            }
        }

        assertEquals(3, requestedItems.size(), "Список задач на сервере не корректный");
        assertEquals(subtask, requestedItems.get(0), "Некорректная очередность задач в списке");
        assertEquals(task, requestedItems.get(1), "Некорректная очередность задач в списке");
        assertEquals(epic, requestedItems.get(2), "Некорректная очередность задач в списке");
    }

}
