package api;

import adapters.DurationAdapter;
import adapters.LocalDateTimeAdapter;
import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import exceptions.NoSuchTaskExistsException;
import managers.InMemoryTaskManager;
import managers.TaskManager;
import model.Epic;
import model.ItemType;
import model.Subtask;
import model.Task;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpTaskServer {
    private final TaskManager taskManager;
    private final int PORT = 8080;
    private final HttpServer httpServer;
    private final Gson gson;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        httpServer = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        httpServer.createContext("/api/v1/tasks/", this::handleTasksPath);
        httpServer.createContext("/api/v1/tasks/subtask/", this::handleSubtaskPath);
        httpServer.createContext("/api/v1/tasks/task/", this::handleTaskPath);
        httpServer.createContext("/api/v1/tasks/epic/", this::handleEpicPath);
        httpServer.createContext("/api/v1/tasks/history/", this::handleHistoryPath);
        httpServer.createContext("/api/v1/tasks/priorities/", this::handlePrioritiesPath);
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }

    private void handleSubtaskPath(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();

        try {
            switch (method) {
                case "GET":
                    handleGetForSubtaskPath(httpExchange);
                    break;
                case "DELETE":
                    handleDeleteForSubtaskPath(httpExchange);
                    break;
                case "PATCH":
                    handlePatchForSubtaskPath(httpExchange);
                    break;
                default:
                    writeResponse(httpExchange,
                            "Метод не поддерживается",
                            400);
            }
        } catch (IOException e) {
            writeResponse(httpExchange,
                    "Ошибка выполнения запроса",
                    500);
        } finally {
            httpExchange.close();
        }
    }

    private void handlePatchForSubtaskPath(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();
        String query = httpExchange.getRequestURI().getQuery();

        if (Pattern.matches("^/api/v1/tasks/subtask/$", path)) {
            if (Pattern.matches("^subtaskId=\\d+&epicId=\\d+$", query)) {
                linkSubtaskToEpic(httpExchange);
            } else {
                writeResponse(httpExchange,
                        "Неверный формат параметров",
                        400);
            }
        } else {
            writeResponse(httpExchange,
                    "Некорректный путь",
                    404);
        }
    }

    private void linkSubtaskToEpic(HttpExchange httpExchange) throws IOException {
        String query = httpExchange.getRequestURI().getQuery();
        String subtaskQueryId = query
                .replaceFirst("&epicId=\\d+", "");
        String epicQueryId = query
                .replaceFirst("subtaskId=\\d+", "");
        int subtaskId = getIdFromQuery(subtaskQueryId);
        int epicId = getIdFromQuery(epicQueryId);
        try {
            Subtask subtask = (Subtask) ((InMemoryTaskManager) taskManager)
                    .getItemByIdWithoutSavingHistory(subtaskId);
            Epic epic = (Epic) ((InMemoryTaskManager) taskManager)
                    .getItemByIdWithoutSavingHistory(epicId);
            taskManager.linkSubtaskToEpic(subtask, epic);
            writeResponse(httpExchange, "", 200);
        } catch (NullPointerException e) {
            writeResponse(httpExchange,
                    "Эпик или подзадача не найдены",
                    404);
        }
    }

    private void handleDeleteForSubtaskPath(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();

        if (Pattern.matches("^/api/v1/tasks/subtask/$", path)) {
            deleteAllItemsByType(httpExchange, ItemType.SUBTASK);
        } else {
            writeResponse(httpExchange,
                    "Некорректный путь",
                    404);
        }
    }

    private void deleteAllItemsByType(HttpExchange httpExchange, ItemType itemType) throws IOException {
        try {
            taskManager.removeAllItemsByType(itemType);
            writeResponse(httpExchange,
                    "",
                    200);
        } catch (NullPointerException e) {
            writeResponse(httpExchange,
                    "Cписок уже пуст",
                    204);
        }
    }

    private void handleGetForSubtaskPath(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();
        String query = httpExchange.getRequestURI().getQuery();

        if (Pattern.matches("^/api/v1/tasks/subtask/$", path)) {
            sendItemsList(httpExchange, ItemType.SUBTASK);
        } else if (Pattern.matches("^/api/v1/tasks/subtask/epic/$", path)) {
            if (Pattern.matches("^id=\\d+$", query)) {
                sendEpicSubtasksList(httpExchange);
            } else {
                writeResponse(httpExchange,
                        "Неверный формат параметров",
                        400);
            }
        } else if (Pattern.matches("^/api/v1/tasks/history/$", path)) {
            sendHistory(httpExchange);
        } else if (Pattern.matches("^/api/v1/tasks/priorities/$", path)) {
            sendPriorityList(httpExchange);
        } else {
            writeResponse(httpExchange,
                    "Некорректный путь",
                    404);
        }
    }

    private void sendItemsList(HttpExchange httpExchange, ItemType itemType) throws IOException {
        try {
            String response = gson.toJson(taskManager.getAllItemsByType(itemType));
            writeResponse(httpExchange,
                    response,
                    200);
        } catch (NullPointerException e) {
            writeResponse(httpExchange,
                    "Список пуст",
                    204);
        }
    }

    private void sendEpicSubtasksList(HttpExchange httpExchange) throws IOException {
        String query = httpExchange.getRequestURI().getQuery();
        int epicId = getIdFromQuery(query);
        Task requestedEpic = ((InMemoryTaskManager) taskManager)
                .getItemByIdWithoutSavingHistory(epicId);
        if (requestedEpic != null && requestedEpic.getItemType() == ItemType.EPIC) {
            ArrayList<Subtask> epicSubtasks = taskManager
                    .getEpicSubtasks(epicId);
            if (epicSubtasks.size() > 0) {
                String response = gson.toJson(epicSubtasks);
                writeResponse(httpExchange,
                        response,
                        200);
            } else {
                writeResponse(httpExchange,
                        "К эпику не привязаны подзадачи",
                        204);
            }
        } else {
            writeResponse(httpExchange,
                    "Эпик с id " + epicId + " не найден",
                    204);
        }
    }

    private void handleTasksPath(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();

        try {
            switch (method) {
                case "POST":
                    handlePostForTasksPath(httpExchange);
                    break;
                case "GET":
                    handleGetForTasksPath(httpExchange);
                    break;
                case "DELETE":
                    handleDeleteForTasksPath(httpExchange);
                    break;
                default:
                    writeResponse(httpExchange,
                            "Метод не поддерживается",
                            400);
            }
        } catch (IOException e) {
            writeResponse(httpExchange,
                    "Ошибка выполнения запроса",
                    500);
        } finally {
            httpExchange.close();
        }
    }

    private void handleGetForTasksPath(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();
        String query = httpExchange.getRequestURI().getQuery();

        if (Pattern.matches("^/api/v1/tasks/$", path)) {
            if (Pattern.matches("^id=\\d+$", query)) {
                sendItem(httpExchange);
            } else {
                writeResponse(httpExchange,
                        "Неверный формат параметров",
                        400);
            }
        } else {
            writeResponse(httpExchange,
                    "Некорректный путь",
                    404);
        }
    }

    private void sendItem(HttpExchange httpExchange) throws IOException {
        String query = httpExchange.getRequestURI().getQuery();
        int itemId = getIdFromQuery(query);
        try {
            Task item = taskManager.getItemById(itemId);
            String response = gson.toJson(item);
            writeResponse(httpExchange,
                    response,
                    200);
        } catch (NoSuchTaskExistsException e) {
            writeResponse(httpExchange,
                    "Задача с id " + itemId + " не найдена",
                    204);
        }
    }

    private void handlePostForTasksPath(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();

        if (Pattern.matches("^/api/v1/tasks/", path)) {
            if (httpExchange.getRequestBody() != null) {
                JsonObject itemObject;
                try {
                    itemObject = getJsonObjectFromRequest(httpExchange);
                    int itemId = -1;
                    try {
                        itemId = itemObject.get("id").getAsInt();
                    } catch (NumberFormatException e) {
                        writeResponse(httpExchange,
                                "В параметре id передано не число",
                                400);
                    }

                    ItemType itemType = gson.fromJson(itemObject.get("itemType"), ItemType.class);
                    if (itemType == null) {
                        writeResponse(httpExchange,
                                "Некорректный тип задачи",
                                400);
                        return;
                    }
                    try {
                        switch (identifyPostAction(itemObject)) {
                            case "create":
                                createItem(itemObject, itemType);
                                writeResponse(httpExchange,
                                        "",
                                        201);
                            case "update":
                                try {
                                    updateItem(itemObject, itemType, itemId);
                                    writeResponse(httpExchange,
                                            "",
                                            201);
                                } catch (NoSuchTaskExistsException e) {
                                    writeResponse(httpExchange,
                                            "Ошибка обновления. Нет задач с id: " + itemId,
                                            400);
                                }
                        }
                    } catch (NumberFormatException e) {
                        writeResponse(httpExchange,
                                "В параметре id передано не число",
                                400);
                    }
                } catch (JsonParseException e) {
                    writeResponse(httpExchange,
                            "Некорректный Json",
                            400);
                }
            } else {
                writeResponse(httpExchange,
                        "Пустое тело запроса",
                        400);
            }
        } else {
            writeResponse(httpExchange,
                    "Некорректный путь",
                    404);
        }

    }

    private JsonObject getJsonObjectFromRequest(HttpExchange httpExchange) throws IOException {
        try (InputStream inputStream = httpExchange.getRequestBody()) {
            String requestBody = new String(inputStream.readAllBytes(), UTF_8);
            JsonElement jsonElement = JsonParser.parseString(requestBody);

            if (jsonElement.isJsonObject()) {
                return jsonElement.getAsJsonObject();
            } else {
                throw new JsonParseException("Некорректный Json");
            }
        } catch (JsonSyntaxException e) {
            throw new JsonParseException("Некорректный Json");
        }
    }

    private String identifyPostAction(JsonObject itemObject) throws NumberFormatException {
        int itemId = itemObject.get("id").getAsInt();
        return itemId == 0 ? "create" : "update";
    }

    private void createItem(JsonObject itemObject, ItemType itemType) throws JsonParseException {
        switch (itemType) {
            case TASK:
                taskManager.createItem(gson.fromJson(itemObject, Task.class));
                break;
            case SUBTASK:
                taskManager.createItem(gson.fromJson(itemObject, Subtask.class));
                break;
            case EPIC:
                taskManager.createItem(gson.fromJson(itemObject, Epic.class));
                break;
        }
    }

    private void updateItem(JsonObject itemObject, ItemType itemType, int itemId) throws JsonParseException {
        if (((InMemoryTaskManager) taskManager)
                .getItemByIdWithoutSavingHistory(itemId) == null) {
            throw new NoSuchTaskExistsException("Задачи с id=" + itemId + "не существует");
        }
        switch (itemType) {
            case TASK:
                taskManager.updateItem(gson.fromJson(itemObject, Task.class), itemId);
                break;
            case SUBTASK:
                taskManager.updateItem(gson.fromJson(itemObject, Subtask.class), itemId);
                break;
            case EPIC:
                taskManager.updateItem(gson.fromJson(itemObject, Epic.class), itemId);
                break;
        }

    }

    private void handleDeleteForTasksPath(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();
        String query = httpExchange.getRequestURI().getQuery();

        if (Pattern.matches("^/api/v1/tasks/$", path)) {
            if (Pattern.matches("^id=\\d+$", query)) {
                int itemId = getIdFromQuery(query);
                try {
                    taskManager.removeItemById(itemId);
                    writeResponse(httpExchange,
                            "",
                            200);
                } catch (NoSuchTaskExistsException e) {
                    writeResponse(httpExchange,
                            "Задача с id " + itemId + " не найдена",
                            204);
                }
            } else {
                writeResponse(httpExchange,
                        "Неверный формат параметров",
                        400);
            }
        } else {
            writeResponse(httpExchange,
                    "Некорректный путь",
                    404);
        }
    }

    private void handleTaskPath(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();

        try {
            switch (method) {
                case "GET":
                    handleGetForTaskPath(httpExchange);
                    break;
                case "DELETE":
                    handleDeleteForTaskPath(httpExchange);
                    break;
                default:
                    writeResponse(httpExchange,
                            "Метод не поддерживается",
                            400);
            }
        } catch (IOException e) {
            writeResponse(httpExchange,
                    "Ошибка выполнения запроса",
                    500);
        } finally {
            httpExchange.close();
        }
    }

    private void handleGetForTaskPath(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();

        if (Pattern.matches("^/api/v1/tasks/task/$", path)) {
            sendItemsList(httpExchange, ItemType.TASK);
        } else {
            writeResponse(httpExchange,
                    "Некорректный путь",
                    404);
        }
    }

    private void handleDeleteForTaskPath(HttpExchange httpExchange) throws IOException {
        try {
            deleteAllItemsByType(httpExchange, ItemType.TASK);
        } catch (NullPointerException e) {
            writeResponse(httpExchange, "Cписок задач уже пуст", 204);
        }
    }

    private void handleEpicPath(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();

        try {
            switch (method) {
                case "GET":
                    handleGetForEpicPath(httpExchange);
                    break;
                case "DELETE":
                    handleDeleteForEpicPath(httpExchange);
                    break;
                default:
                    writeResponse(httpExchange,
                            "Метод не поддерживается",
                            400);
            }
        } catch (IOException e) {
            writeResponse(httpExchange,
                    "Ошибка выполнения запроса",
                    500);
        } finally {
            httpExchange.close();
        }
    }

    private void handleGetForEpicPath(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();

        if (Pattern.matches("^/api/v1/tasks/epic/$", path)) {
            sendItemsList(httpExchange, ItemType.EPIC);
        } else {
            writeResponse(httpExchange,
                    "Некорректный путь",
                    404);
        }
    }

    private void handleDeleteForEpicPath(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();

        if (Pattern.matches("^/api/v1/tasks/epic/$", path)) {
            deleteAllItemsByType(httpExchange, ItemType.EPIC);
        } else {
            writeResponse(httpExchange,
                    "Неверный формат параметров",
                    400);
        }
    }

    private void handleHistoryPath(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();

        if (method.equals("GET")) {
            try {
                sendHistory(httpExchange);
            } catch (IOException e) {
                writeResponse(httpExchange,
                        "Ошибка выполнения запроса",
                        500);
            }
        } else {
            writeResponse(httpExchange,
                    "Метод не поддерживается",
                    400);
        }
    }

    private void sendHistory(HttpExchange httpExchange) throws IOException {
        List<Task> history = ((InMemoryTaskManager) taskManager)
                .getHistoryManager()
                .getHistory();
        if (history.size() != 0) {
            String response = gson.toJson(history);
            writeResponse(httpExchange,
                    response,
                    200);
        } else {
            writeResponse(httpExchange,
                    "В истории нет записей",
                    204);
        }
    }

    private void handlePrioritiesPath(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();

        if (method.equals("GET")) {
            try {
                sendPriorityList(httpExchange);
            } catch (IOException e) {
                writeResponse(httpExchange,
                        "Ошибка выполнения запроса",
                        500);
            }
        } else {
            writeResponse(httpExchange,
                    "Метод не поддерживается",
                    400);
        }
    }

    private void sendPriorityList(HttpExchange httpExchange) throws IOException {
        List<Task> priorities = taskManager.getPrioritizedTasks();
        if (priorities.size() != 0) {
            String response = gson.toJson(priorities);
            writeResponse(httpExchange,
                    response,
                    200);
        } else {
            writeResponse(httpExchange,
                    "Нет приоритетных задач",
                    204);
        }
    }

    private int getIdFromQuery(String query) {
        String id = "";
        if (Pattern.matches("^id=\\d+$", query)) {
            id = query.replaceFirst("id=", "");
        } else if (Pattern.matches("^subtaskId=\\d+$", query)) {
            id = query.replaceFirst("subtaskId=", "");
        } else if (Pattern.matches("^&epicId=\\d+$", query)) {
            id = query.replaceFirst("&epicId=", "");
        }
        return Integer.parseInt(id);
    }

    public void start() {
        System.out.println("Запускаем сервер на порту " + PORT);
        System.out.println("Принимаем запросы на хосте http://localhost:" + PORT + "/api/v1/tasks/");
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
        System.out.println("Сервер остановлен");
    }

    private void writeResponse(HttpExchange exchange,
                               String responseString,
                               int responseCode) throws IOException {
        if (responseString.isBlank()) {
            exchange.sendResponseHeaders(responseCode, 0);
        } else {
            byte[] bytes = responseString.getBytes(UTF_8);
            exchange.sendResponseHeaders(responseCode, bytes.length);
            exchange.getResponseBody().write(bytes);
        }
        exchange.close();
    }
}
