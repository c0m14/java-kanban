package api;

import adapters.DurationAdapter;
import adapters.LocalDateTimeAdapter;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import exceptions.NoSuchTaskExistsException;
import managers.Managers;
import managers.TaskManager;
import model.Epic;
import model.ItemType;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerTest {
    private static HttpClient client;
    private static TaskManager taskManager;
    private static HttpTaskServer httpTaskServer;
    private static Gson gson;
    private static DateTimeFormatter formatter;


    @BeforeAll
    public static void beforeAll() {
        client = HttpClient.newHttpClient();
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
        formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    }

    @BeforeEach
    public void beforeEach() throws IOException {
        taskManager = Managers.getDefault();
        httpTaskServer = new HttpTaskServer(taskManager);
        httpTaskServer.start();
    }

    @AfterEach
    public void afterEach() {
        httpTaskServer.stop();
    }

    //тесты на контракт GET /api/v1/tasks/task/
    @Test
    public void shouldReturnAllTasks() throws InterruptedException, IOException {
        Task testTask1 = new Task("testTask1", "Test description");
        testTask1.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        testTask1.setDurationMinutes(Duration.ofMinutes(120));
        Task testTask2 = new Task("testTask2");
        taskManager.createItem(testTask1);
        taskManager.createItem(testTask2);
        URI url = URI.create("http://localhost:8080/api/v1/tasks/task/");
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Type taskArrayListType = new TypeToken<ArrayList<Task>>() {
        }.getType();
        ArrayList<Task> requestedItems = gson.fromJson(response.body(), taskArrayListType);

        assertEquals(200, response.statusCode(), "Некорректный код ответа");
        assertEquals(2, requestedItems.size(), "Список задач не полный");
        assertTrue(requestedItems.contains(testTask1), "Список задач не полный");
        assertTrue(requestedItems.contains(testTask2), "Список задач не полный");
    }

    @Test
    public void shouldReturn204ResponseCodeIfListOfTasksIsEmpty() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/api/v1/tasks/task/");
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode(), "Некорректный код ответа");
    }

    //тесты на контракт GET /api/v1/tasks/subtask/
    @Test
    public void shouldReturnAllSubtasks() throws InterruptedException, IOException {
        Subtask testSubtask1 = new Subtask("testSubtask1");
        testSubtask1.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        testSubtask1.setDurationMinutes(Duration.ofMinutes(120));
        Subtask testSubtask2 = new Subtask("testSubtask2");
        Epic testEpic = new Epic("testEpic");
        taskManager.createItem(testSubtask1);
        taskManager.createItem(testSubtask2);
        taskManager.createItem(testEpic);
        taskManager.linkSubtaskToEpic(testSubtask1, testEpic);
        taskManager.linkSubtaskToEpic(testSubtask2, testEpic);
        URI url = URI.create("http://localhost:8080/api/v1/tasks/subtask/");
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Type subtaskArrayListType = new TypeToken<ArrayList<Subtask>>() {
        }.getType();
        ArrayList<Task> requestedItems = gson.fromJson(response.body(), subtaskArrayListType);

        assertEquals(200, response.statusCode(), "Некорректный код ответа");
        assertEquals(2, requestedItems.size(), "Список задач не полный");
        assertTrue(requestedItems.contains(testSubtask1), "Список задач не полный");
        assertTrue(requestedItems.contains(testSubtask2), "Список задач не полный");
    }

    @Test
    public void shouldReturn204ResponseCodeIfListOfSubtasksIsEmpty() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/api/v1/tasks/subtask/");
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode(), "Некорректный код ответа");
    }

    //тесты на контракт GET /api/v1/tasks/epic/
    @Test
    public void shouldReturnAllEpics() throws InterruptedException, IOException {
        Epic testEpic1 = new Epic("testEpic1", "Test description");
        Subtask testSubtask = new Subtask("testSubtask");
        testSubtask.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        testSubtask.setDurationMinutes(Duration.ofMinutes(120));
        Epic testEpic2 = new Epic("testEpic2");
        taskManager.createItem(testEpic1);
        taskManager.createItem(testEpic2);
        taskManager.createItem(testSubtask);
        taskManager.linkSubtaskToEpic(testSubtask, testEpic1);
        URI url = URI.create("http://localhost:8080/api/v1/tasks/epic/");
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Type epicArrayListType = new TypeToken<ArrayList<Epic>>() {
        }.getType();
        ArrayList<Task> requestedItems = gson.fromJson(response.body(), epicArrayListType);

        assertEquals(200, response.statusCode(), "Некорректный код ответа");
        assertEquals(2, requestedItems.size(), "Список задач не полный");
        assertTrue(requestedItems.contains(testEpic1), "Список задач не полный");
        assertTrue(requestedItems.contains(testEpic2), "Список задач не полный");
    }

    @Test
    public void shouldReturn204ResponseCodeIfListOfEpicsIsEmpty() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/api/v1/tasks/epic/");
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode(), "Некорректный код ответа");
    }

    //тесты на контракт GET /api/v1/tasks/?id={id}
    @Test
    public void shouldReturnTaskById() throws InterruptedException, IOException {
        Task testTask1 = new Task("testTask1");
        testTask1.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        testTask1.setDurationMinutes(Duration.ofMinutes(120));
        taskManager.createItem(testTask1);
        URI url = URI.create("http://localhost:8080/api/v1/tasks/?id=" + testTask1.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Type taskType = new TypeToken<Task>() {
        }.getType();
        Task requestedTask = gson.fromJson(response.body(), taskType);

        assertEquals(200, response.statusCode(), "Некорректный код ответа");
        assertEquals(testTask1, requestedTask, "Получена некорректная задача");
    }

    @Test
    public void shouldReturnSubtaskById() throws InterruptedException, IOException {
        Subtask testSubtask1 = new Subtask("testSubtask1");
        testSubtask1.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        testSubtask1.setDurationMinutes(Duration.ofMinutes(120));
        Epic testEpic = new Epic("testEpic");
        taskManager.createItem(testSubtask1);
        taskManager.createItem(testEpic);
        taskManager.linkSubtaskToEpic(testSubtask1, testEpic);
        URI url = URI.create("http://localhost:8080/api/v1/tasks/?id=" + testSubtask1.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Type subtaskType = new TypeToken<Subtask>() {
        }.getType();
        Subtask requestedSubtask = gson.fromJson(response.body(), subtaskType);

        assertEquals(200, response.statusCode(), "Некорректный код ответа");
        assertEquals(testSubtask1, requestedSubtask, "Получена некорректная задача");
    }

    @Test
    public void shouldReturnEpicById() throws InterruptedException, IOException {
        Subtask testSubtask1 = new Subtask("testSubtask1");
        testSubtask1.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        testSubtask1.setDurationMinutes(Duration.ofMinutes(120));
        Epic testEpic = new Epic("testEpic");
        taskManager.createItem(testSubtask1);
        taskManager.createItem(testEpic);
        taskManager.linkSubtaskToEpic(testSubtask1, testEpic);
        URI url = URI.create("http://localhost:8080/api/v1/tasks/?id=" + testEpic.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Type epicType = new TypeToken<Epic>() {
        }.getType();
        Epic requestedEpic = gson.fromJson(response.body(), epicType);

        assertEquals(200, response.statusCode(), "Некорректный код ответа");
        assertEquals(testEpic, requestedEpic, "Получена некорректная задача");
    }

    @Test
    public void shouldReturn204IfThereIsNoTaskForId() throws InterruptedException, IOException {
        Task testTask1 = new Task("testTask1");
        testTask1.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        testTask1.setDurationMinutes(Duration.ofMinutes(120));
        taskManager.createItem(testTask1);
        URI url = URI.create("http://localhost:8080/api/v1/tasks/?id=" + 2);
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode(), "Некорректный код ответа");
    }

    @Test
    public void shouldReturn400IfIdIsNotANumber() throws InterruptedException, IOException {
        Task testTask1 = new Task("testTask1");
        testTask1.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        testTask1.setDurationMinutes(Duration.ofMinutes(120));
        taskManager.createItem(testTask1);
        URI url = URI.create("http://localhost:8080/api/v1/tasks/?id=-1");
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Некорректный код ответа");
    }

    //тесты на контракт GET /api/v1/tasks/subtasks/epic/?id=
    @Test
    public void shouldReturnAllEpicSubtasks() throws InterruptedException, IOException {
        Epic testEpic1 = new Epic("testEpic1", "Test description");
        Subtask testSubtask = new Subtask("testSubtask");
        testSubtask.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        testSubtask.setDurationMinutes(Duration.ofMinutes(120));
        taskManager.createItem(testEpic1);
        taskManager.createItem(testSubtask);
        taskManager.linkSubtaskToEpic(testSubtask, testEpic1);
        URI url = URI.create("http://localhost:8080/api/v1/tasks/subtask/epic/?id=" + testEpic1.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Type subtaskArrayListType = new TypeToken<ArrayList<Subtask>>() {
        }.getType();
        ArrayList<Task> requestedItems = gson.fromJson(response.body(), subtaskArrayListType);

        assertEquals(200, response.statusCode(), "Некорректный код ответа");
        assertEquals(1, requestedItems.size(), "Список задач не полный");
        assertTrue(requestedItems.contains(testSubtask), "Список задач не полный");
    }

    @Test
    public void shouldReturn204IfSubtasksListForEpicIsEmpty() throws InterruptedException, IOException {
        Epic testEpic1 = new Epic("testEpic1", "Test description");
        taskManager.createItem(testEpic1);
        URI url = URI.create("http://localhost:8080/api/v1/tasks/subtask/epic/?id=" + testEpic1.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode(), "Некорректный код ответа");
    }

    @Test
    public void shouldReturn204IfNoEpicWithProvidedId() throws InterruptedException, IOException {
        Epic testEpic1 = new Epic("testEpic1", "Test description");
        taskManager.createItem(testEpic1);
        Subtask testSubtask1 = new Subtask("testSubtask1");
        taskManager.createItem(testSubtask1);
        taskManager.linkSubtaskToEpic(testSubtask1, testEpic1);
        URI url = URI.create("http://localhost:8080/api/v1/tasks/subtask/epic/?id=" + testSubtask1.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode(), "Некорректный код ответа");
    }

    @Test
    public void shouldReturn400IfEpicIdIsNotANumber() throws InterruptedException, IOException {
        Epic testEpic1 = new Epic("testEpic1", "Test description");
        taskManager.createItem(testEpic1);
        Subtask testSubtask1 = new Subtask("testSubtask1");
        taskManager.createItem(testSubtask1);
        taskManager.linkSubtaskToEpic(testSubtask1, testEpic1);
        URI url = URI.create("http://localhost:8080/api/v1/tasks/subtask/epic/?id=test");
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Некорректный код ответа");
    }

    //тесты на контракт GET /api/v1/tasks/history/
    @Test
    public void shouldReturnHistory() throws InterruptedException, IOException {
        Task testTask = new Task("testTask");
        taskManager.createItem(testTask);
        Epic testEpic = new Epic("testEpic");
        taskManager.createItem(testEpic);
        Subtask testSubtask = new Subtask("testSubtask");
        taskManager.createItem(testSubtask);
        taskManager.linkSubtaskToEpic(testSubtask, testEpic);
        taskManager.getItemById(testEpic.getId());
        taskManager.getItemById(testTask.getId());
        taskManager.getItemById(testSubtask.getId());
        URI url = URI.create("http://localhost:8080/api/v1/tasks/history/");
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Type taskType = new TypeToken<Task>() {
        }.getType();
        Type subtaskType = new TypeToken<Subtask>() {
        }.getType();
        Type epicType = new TypeToken<Epic>() {
        }.getType();
        JsonElement jsonElement = JsonParser.parseString(response.body());
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

        assertEquals(200, response.statusCode(), "Некорректный код ответа");
        assertEquals(3, requestedItems.size(), "Список задач не полный");
        assertEquals(testTask, requestedItems.get(1));
        assertEquals(testEpic, requestedItems.get(0));
        assertEquals(testSubtask, requestedItems.get(2));
    }

    @Test
    public void shouldReturn204IfHistoryListIsEmpty() throws InterruptedException, IOException {
        URI url = URI.create("http://localhost:8080/api/v1/tasks/history/");
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode(), "Некорректный код ответа");
    }

    //тесты на контракт GET /api/v1/tasks/priorities/
    @Test
    public void shouldReturnPriorities() throws InterruptedException, IOException {
        Task testTask = new Task("testTask");
        testTask.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        testTask.setDurationMinutes(Duration.ofMinutes(60));
        taskManager.createItem(testTask);
        Epic testEpic = new Epic("testEpic");
        taskManager.createItem(testEpic);
        Subtask testSubtask = new Subtask("testSubtask");
        testSubtask.setStartTime(LocalDateTime.parse("02-01-2023 12:00", formatter));
        testSubtask.setDurationMinutes(Duration.ofMinutes(60));
        taskManager.createItem(testSubtask);
        taskManager.linkSubtaskToEpic(testSubtask, testEpic);
        URI url = URI.create("http://localhost:8080/api/v1/tasks/priorities/");
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Type taskType = new TypeToken<Task>() {
        }.getType();
        Type subtaskType = new TypeToken<Subtask>() {
        }.getType();
        Type epicType = new TypeToken<Epic>() {
        }.getType();
        JsonElement jsonElement = JsonParser.parseString(response.body());
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

        assertEquals(200, response.statusCode(), "Некорректный код ответа");
        assertEquals(2, requestedItems.size(), "Список задач не полный");
        assertEquals(testTask, requestedItems.get(0));
        assertEquals(testSubtask, requestedItems.get(1));
    }

    @Test
    public void shouldReturn204IfPrioritiesListIsEmpty() throws InterruptedException, IOException {
        URI url = URI.create("http://localhost:8080/api/v1/tasks/priorities/");
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode(), "Некорректный код ответа");
    }

    //тест на некорректный путь при вызове GET
    @Test
    public void shouldReturn404IfGetPathIsIncorrect() throws InterruptedException, IOException {
        URI url = URI.create("http://localhost:8080/api/v1/tasks/somePath/");
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Некорректный код ответа");
    }

    //тесты на контракт POST /api/v1/tasks/
    @Test
    public void shouldCreateTask() throws InterruptedException, IOException {
        Task task = new Task("task");
        URI url = URI.create("http://localhost:8080/api/v1/tasks/");
        String body = gson.toJson(task);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Некорректный код ответа");

        task.setId(1);
        assertEquals(taskManager.getItemById(1), task, "Ошибка при создании задачи");
    }

    @Test
    public void shouldCreateSubtask() throws InterruptedException, IOException {
        Subtask subtask = new Subtask("subtask");
        subtask.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        subtask.setDurationMinutes(Duration.ofMinutes(60));
        URI url = URI.create("http://localhost:8080/api/v1/tasks/");
        String body = gson.toJson(subtask);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Некорректный код ответа");

        subtask.setId(1);
        assertEquals(taskManager.getItemById(1), subtask, "Ошибка при создании задачи");
    }

    @Test
    public void shouldCreateEpic() throws InterruptedException, IOException {
        Epic epic = new Epic("epic");
        URI url = URI.create("http://localhost:8080/api/v1/tasks/");
        String body = gson.toJson(epic);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Некорректный код ответа");

        epic.setId(1);
        assertEquals(taskManager.getItemById(1), epic, "Ошибка при создании задачи");
    }

    @Test
    public void shouldUpdateTask() throws InterruptedException, IOException {
        Task task = new Task("task");
        taskManager.createItem(task);
        Task updatedTask = new Task("updatedTask");
        updatedTask.setId(1);
        URI url = URI.create("http://localhost:8080/api/v1/tasks/");
        String body = gson.toJson(updatedTask);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Некорректный код ответа");
        assertEquals(taskManager.getItemById(1), updatedTask, "Ошибка при обновлении задачи");
    }

    @Test
    public void shouldReturn400IfBodyIsEmpty() throws InterruptedException, IOException {
        URI url = URI.create("http://localhost:8080/api/v1/tasks/");
        String body = "";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Некорректный код ответа");
    }

    @Test
    public void shouldUpdateSubtask() throws InterruptedException, IOException {
        Subtask subtask = new Subtask("subtask");
        taskManager.createItem(subtask);
        Subtask updatedSubtask = new Subtask("updatedSubtask");
        updatedSubtask.setId(1);
        URI url = URI.create("http://localhost:8080/api/v1/tasks/");
        String body = gson.toJson(updatedSubtask);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Некорректный код ответа");
        assertEquals(taskManager.getItemById(1), updatedSubtask, "Ошибка при обновлении задачи");
    }

    @Test
    public void shouldUpdateEpic() throws InterruptedException, IOException {
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        Epic updatedEpic = new Epic("updatedEpic");
        updatedEpic.setId(1);
        URI url = URI.create("http://localhost:8080/api/v1/tasks/");
        String body = gson.toJson(updatedEpic);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Некорректный код ответа");
        assertEquals(taskManager.getItemById(1), updatedEpic, "Ошибка при обновлении задачи");
    }

    @Test
    public void shouldReturn400IfIncorrectJsonInBody() throws InterruptedException, IOException {
        URI url = URI.create("http://localhost:8080/api/v1/tasks/");
        String body = "{" +
                "\"id\":1," +
                "\"name\":\"testTask\"," +
                "\"description\":\"\"," +
                "\"status\":\"NEW\"," +
                "\"itemType\":\"TASK\""; //пропустим закрывающую скобку
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Некорректный код ответа");
    }

    @Test
    public void shouldReturn400IfWrongObjectInBody() throws InterruptedException, IOException {
        URI url = URI.create("http://localhost:8080/api/v1/tasks/");
        String body = gson.toJson(String.valueOf(123));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Некорректный код ответа");
    }

    @Test
    public void shouldReturn400IfWrongItemTypeInBody() throws InterruptedException, IOException {
        URI url = URI.create("http://localhost:8080/api/v1/tasks/");
        String body = "{" +
                "\"id\":0," +
                "\"name\":\"testTask\"," +
                "\"description\":\"\"," +
                "\"status\":\"NEW\"," +
                "\"itemType\":\"EPICTASK\"" + //тут некорректный тип задачи
                "}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Некорректный код ответа");
    }

    @Test
    public void shouldReturn400IfIdIsNot0ButThereIsNotSuchTaskInManager() throws InterruptedException, IOException {
        URI url = URI.create("http://localhost:8080/api/v1/tasks/");
        String body = "{" +
                "\"id\":1," +
                "\"name\":\"testTask\"," +
                "\"description\":\"\"," +
                "\"status\":\"NEW\"," +
                "\"itemType\":\"TASK\"" +
                "}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Некорректный код ответа");
    }

    @Test
    public void shouldReturn404IfPathIsWrongForPost() throws InterruptedException, IOException {
        Task task = new Task("task");
        URI url = URI.create("http://localhost:8080/api/v1/tasks/someWrongPath");
        String body = gson.toJson(task);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Некорректный код ответа");
    }

    //тесты на контракт DELETE /api/v1/tasks/task/
    @Test
    public void shouldDeleteAllTasksIfExist() throws InterruptedException, IOException {
        Task task = new Task("task");
        taskManager.createItem(task);
        URI url = URI.create("http://localhost:8080/api/v1/tasks/task/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Некорректный код ответа");
        assertEquals(0, taskManager.getAllItemsByType(ItemType.TASK).size());
    }

    @Test
    public void shouldReturn204IfDeleteAndTasksDoesNotExist() throws InterruptedException, IOException {
        URI url = URI.create("http://localhost:8080/api/v1/tasks/task/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode(), "Некорректный код ответа");
    }

    //тесты на контракт DELETE /api/v1/tasks/subtask/
    @Test
    public void shouldDeleteAllSubtasksIfExist() throws InterruptedException, IOException {
        Subtask subtask = new Subtask("subtask");
        taskManager.createItem(subtask);
        URI url = URI.create("http://localhost:8080/api/v1/tasks/subtask/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Некорректный код ответа");
        assertEquals(0, taskManager.getAllItemsByType(ItemType.SUBTASK).size());
    }

    @Test
    public void shouldReturn204IfDeleteAndSubtasksDoesNotExist() throws InterruptedException, IOException {
        URI url = URI.create("http://localhost:8080/api/v1/tasks/subtask/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode(), "Некорректный код ответа");
    }

    //тесты на контракт DELETE /api/v1/tasks/epic/
    @Test
    public void shouldDeleteAllEpicsIfExist() throws InterruptedException, IOException {
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        URI url = URI.create("http://localhost:8080/api/v1/tasks/epic/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Некорректный код ответа");
        assertEquals(0, taskManager.getAllItemsByType(ItemType.EPIC).size());
    }

    @Test
    public void shouldReturn204IfDeleteAndEpicsDoesNotExist() throws InterruptedException, IOException {
        URI url = URI.create("http://localhost:8080/api/v1/tasks/epic/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode(), "Некорректный код ответа");
    }

    //тесты на контракт DELETE /api/v1/tasks/?id=
    @Test
    public void shouldDeleteTaskByIdIfExist() throws InterruptedException, IOException {
        Task task = new Task("task");
        taskManager.createItem(task);
        URI url = URI.create("http://localhost:8080/api/v1/tasks/?id=" + task.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Некорректный код ответа");
        Executable executable = () -> taskManager.getItemById(task.getId());
        assertThrows(NoSuchTaskExistsException.class,
                executable,
                "Задача не удалена");
    }

    @Test
    public void shouldReturn204IfDeleteAndTaskDoesNotExists() throws InterruptedException, IOException {
        URI url = URI.create("http://localhost:8080/api/v1/tasks/?id=" + 1);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode(), "Некорректный код ответа");
    }

    @Test
    public void shouldDeleteSubtaskByIdIfExist() throws InterruptedException, IOException {
        Subtask subtask = new Subtask("subtask");
        taskManager.createItem(subtask);
        URI url = URI.create("http://localhost:8080/api/v1/tasks/?id=" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Некорректный код ответа");
        Executable executable = () -> taskManager.getItemById(subtask.getId());
        assertThrows(NoSuchTaskExistsException.class,
                executable,
                "Задача не удалена");
    }

    @Test
    public void shouldDeleteEpicByIdIfExist() throws InterruptedException, IOException {
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        URI url = URI.create("http://localhost:8080/api/v1/tasks/?id=" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Некорректный код ответа");
        Executable executable = () -> taskManager.getItemById(epic.getId());
        assertThrows(NoSuchTaskExistsException.class,
                executable,
                "Задача не удалена");
    }

    //тесты на контракт PATCH /api/v1/tasks/subtask/?subtaskId={id}&epicId={id}
    @Test
    public void shouldLinkSubtaskToEpic() throws InterruptedException, IOException {
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        Subtask subtask = new Subtask("subtask");
        taskManager.createItem(subtask);
        URI url = URI.create("http://localhost:8080/api/v1/tasks/subtask/?subtaskId=" +
                subtask.getId() +
                "&epicId=" +
                epic.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Некорректный код ответа");
        assertEquals(1, subtask.getEpicId());
        assertEquals(List.of(2), epic.getEpicSubtaskIds());
    }

    @Test
    public void shouldReturn404IfEpicDoesNotExist() throws InterruptedException, IOException {
        Subtask subtask = new Subtask("subtask");
        taskManager.createItem(subtask);
        URI url = URI.create("http://localhost:8080/api/v1/tasks/subtask/?subtaskId=" +
                subtask.getId() +
                "&epicId=" +
                2); //неверный id эпика
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Некорректный код ответа");
    }

    @Test
    public void shouldReturn404IfSubtaskDoesNotExist() throws InterruptedException, IOException {
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        URI url = URI.create("http://localhost:8080/api/v1/tasks/subtask/?subtaskId=" +
                2 + //неверный id подзадачи
                "&epicId=" +
                epic.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Некорректный код ответа");
    }

    @Test
    public void shouldReturn400IfSubtaskIdNotANumber() throws InterruptedException, IOException {
        Subtask subtask = new Subtask("subtask");
        taskManager.createItem(subtask);
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        URI url = URI.create("http://localhost:8080/api/v1/tasks/subtask/?subtaskId=" +
                "test" + //неверный id подзадачи
                "&epicId=" +
                epic.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Некорректный код ответа");
    }

    @Test
    public void shouldReturn400IfEpicIdNotANumber() throws InterruptedException, IOException {
        Subtask subtask = new Subtask("subtask");
        taskManager.createItem(subtask);
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        URI url = URI.create("http://localhost:8080/api/v1/tasks/subtask/?subtaskId=" +
                subtask.getId() +
                "&epicId=" +
                "test"); //неверный id эпика
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Некорректный код ответа");
    }

    //тест на некорректный метод
    @Test
    public void shouldReturn400IfMethodIsIncorrect() throws InterruptedException, IOException {
        URI url = URI.create("http://localhost:8080/api/v1/tasks/"); //неверный id эпика
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .method("PUT", HttpRequest.BodyPublishers.noBody())
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Некорректный код ответа");
    }
}
