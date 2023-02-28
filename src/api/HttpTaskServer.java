package api;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import exceptions.NoSuchTaskExists;
import managers.InMemoryTaskManager;
import managers.TaskManager;
import model.Epic;
import model.ItemType;
import model.Subtask;
import model.Task;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
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
        httpServer.createContext("/api/v1/tasks/", this::handleTasks);
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    private void handleTasks(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();
        String query = httpExchange.getRequestURI().getQuery();
        String method = httpExchange.getRequestMethod();

        try {
            switch (method) {
                case "GET":
                    if (Pattern.matches("^/api/v1/tasks/task/$", path)) {
                        //Вернуть список всех задач
                        try {
                            String response = gson.toJson(taskManager.getAllItemsByType(ItemType.TASK));
                            writeResponse(httpExchange, response, 200);
                        } catch (NullPointerException e) {
                            writeResponse(httpExchange, "Список задач пуст", 204);
                        }
                    } else if (Pattern.matches("^/api/v1/tasks/subtask/$", path)) {
                        //Вернуть список всех подзадач
                        try {
                            String response = gson.toJson(taskManager.getAllItemsByType(ItemType.SUBTASK));
                            writeResponse(httpExchange, response, 200);
                        } catch (NullPointerException e) {
                            writeResponse(httpExchange, "Список подзадач пуст", 204);
                        }
                    } else if (Pattern.matches("^/api/v1/tasks/epic/$", path)) {
                        //Вернуть список всех эпиков
                        try {
                            String response = gson.toJson(taskManager.getAllItemsByType(ItemType.EPIC));
                            writeResponse(httpExchange, response, 200);
                        } catch (NullPointerException e) {
                            writeResponse(httpExchange, "Список эпиков пуст", 204);
                        }

                    } else if (Pattern.matches("^/api/v1/tasks/", path)) {
                        if (Pattern.matches("^id=\\d+$", query)) {
                            //Получение item по id
                            String queryItemId = query.replaceFirst("id=", "");
                            Task task = taskManager.getItemById(getIdFromPath(queryItemId));
                            if (task != null) {
                                String response = gson.toJson(task);
                                writeResponse(httpExchange, response, 200);
                            } else {
                                writeResponse(httpExchange,
                                        "Задача с id " + queryItemId + " не найдена",
                                        204);
                            }
                        } else {
                            writeResponse(httpExchange, "Неверный формат параметров", 400);
                        }
                    } else if (Pattern.matches("^/api/v1/tasks/subtasks/epic/$", path)) {
                        if (Pattern.matches("^id=\\d+$", query)) {
                            //Получение списка подзадач для эпика
                            String queryEpicId = query.replaceFirst("id=", "");
                            Task requestedEpic = ((InMemoryTaskManager) taskManager)
                                    .getItemByIdWithoutSavingHistory(getIdFromPath(queryEpicId));
                            if (requestedEpic != null && requestedEpic.getItemType() == ItemType.EPIC) {
                                try {
                                    ArrayList<Subtask> epicSubtasks = taskManager
                                            .getEpicSubtasks(getIdFromPath(queryEpicId));
                                    String response = gson.toJson(epicSubtasks);
                                    writeResponse(httpExchange, response, 200);
                                } catch (NullPointerException e) {
                                    writeResponse(httpExchange,
                                            "К эпику не привязаны подзадачи",
                                            204);
                                }
                            } else {
                                writeResponse(httpExchange,
                                        "Эпик с id " + queryEpicId + " не найден",
                                        204);
                            }
                        } else {
                            writeResponse(httpExchange, "Неверный формат параметров", 400);
                        }
                    } else if (Pattern.matches("^/api/v1/tasks/history/$", path)) {
                        //Вернуть список истории задач
                        List<Task> history = ((InMemoryTaskManager) taskManager)
                                .getHistoryManager()
                                .getHistory();
                        if (history.size() != 0) {
                            String response = gson.toJson(history);
                            writeResponse(httpExchange, response, 200);
                        } else {
                            writeResponse(httpExchange, "В истории нет записей", 204);
                        }
                    } else if (Pattern.matches("^/api/v1/tasks/priorities/", path)) {
                        //Вернуть список задач по приоритетам
                        List<Task> priorities = taskManager.getPrioritizedTasks();
                        if (priorities.size() != 0) {
                            String response = gson.toJson(priorities);
                            writeResponse(httpExchange, response, 200);
                        } else {
                            writeResponse(httpExchange, "Нет приоритетных задач", 204);
                        }
                    } else {
                        //Отправить ошибку "Использован некорректный путь запроса"
                        writeResponse(httpExchange, "Некорректный путь", 404);
                    }
                    break;
                case "POST":
                    if (Pattern.matches("^/api/v1/tasks/", path)) {
                        if (httpExchange.getRequestBody() != null) {
                            //Создание item или update item
                            try (InputStream inputStream = httpExchange.getRequestBody()) {
                                String requestBody = new String(inputStream.readAllBytes(), UTF_8);
                                JsonElement jsonElement = JsonParser.parseString(requestBody);
                                if (jsonElement.isJsonObject()) {
                                    JsonObject item = jsonElement.getAsJsonObject();
                                    try {
                                        int itemId = item.get("id").getAsInt();
                                        String itemType = item.get("itemType").getAsString();
                                        //Проверяем id
                                        if (itemId == 0) {
                                            switch (itemType) {
                                                case "TASK":
                                                    taskManager.createItem(gson.fromJson(jsonElement, Task.class));
                                                    break;
                                                case "SUBTASK":
                                                    taskManager.createItem(gson.fromJson(jsonElement, Subtask.class));
                                                    break;
                                                case "EPIC":
                                                    taskManager.createItem(gson.fromJson(jsonElement, Epic.class));
                                                    break;
                                                default:
                                                    writeResponse(httpExchange,
                                                            "Некорректный тип задачи",
                                                            400);
                                            }
                                            writeResponse(httpExchange, "", 201);
                                            return;
                                        } else {
                                            if (((InMemoryTaskManager) taskManager)
                                                    .getItemByIdWithoutSavingHistory(itemId) != null) {
                                                switch (itemType) {
                                                    case "TASK":
                                                        taskManager.updateItem(gson.
                                                                fromJson(jsonElement, Task.class), itemId);
                                                        break;
                                                    case "SUBTASK":
                                                        taskManager.updateItem(gson.
                                                                fromJson(jsonElement, Subtask.class), itemId);
                                                        break;
                                                    case "EPIC":
                                                        taskManager.updateItem(gson.
                                                                fromJson(jsonElement, Epic.class), itemId);
                                                        break;
                                                    default:
                                                        writeResponse(httpExchange,
                                                                "Некорректный тип задачи",
                                                                400);
                                                }
                                                writeResponse(httpExchange, "", 201);
                                                return;
                                            } else {
                                                writeResponse(httpExchange,
                                                        "Ошибка обновления. Нет задач с id: " + itemId,
                                                        400);
                                                return;
                                            }
                                        }
                                    } catch (NumberFormatException e) {
                                        writeResponse(httpExchange,
                                                "В параметре id передано не число",
                                                400);
                                        return;
                                    }
                                } else {
                                    writeResponse(httpExchange, "Некорректный Json", 400);
                                    return;
                                }
                            } catch (JsonSyntaxException e) {
                                writeResponse(httpExchange, "Некорректный Json", 400);
                                return;
                            }
                        } else {
                            //Вернуть ошибку "Пустое тело запроса"
                            writeResponse(httpExchange,
                                    "Пустое тело запроса",
                                    400);
                        }
                    } else {
                        //Отправить ошибку "Использован некорректный путь запроса"
                        writeResponse(httpExchange, "Некорректный путь", 404);
                    }
                    break;
                case "DELETE":
                    if (Pattern.matches("^/api/v1/tasks/task/$", path)) {
                        //Удалить все задачи
                        try {
                            taskManager.removeAllItemsByType(ItemType.TASK);
                            writeResponse(httpExchange, "", 200);
                        } catch (NullPointerException e) {
                            writeResponse(httpExchange, "Cписок задач уже пуст", 204);
                        }
                    } else if (Pattern.matches("^/api/v1/tasks/subtask/$", path)) {
                        //Удалить все подзадачи
                        try {
                            taskManager.removeAllItemsByType(ItemType.SUBTASK);
                            writeResponse(httpExchange, "", 200);
                        } catch (NullPointerException e) {
                            writeResponse(httpExchange, "Cписок задач уже пуст", 204);
                        }

                    } else if (Pattern.matches("^/api/v1/tasks/epic/$", path)) {
                        //Удалить все эпики
                        try {
                            taskManager.removeAllItemsByType(ItemType.EPIC);
                            writeResponse(httpExchange, "", 200);
                        } catch (NullPointerException e) {
                            writeResponse(httpExchange, "Cписок задач уже пуст", 204);
                        }

                    } else if (Pattern.matches("^/api/v1/tasks/$", path)) {
                        if (Pattern.matches("^id=\\d+$", query)) {
                            //Удалить item по id
                            String queryItemId = query.replaceFirst("id=", "");

                            try {
                                taskManager.removeItemById(getIdFromPath(queryItemId));
                                writeResponse(httpExchange, "", 200);
                            } catch (NoSuchTaskExists e) {
                                writeResponse(httpExchange,
                                        "Задача с id " + queryItemId + " не найдена",
                                        204);
                            }
                        } else {
                            writeResponse(httpExchange, "Неверный формат параметров", 400);
                        }
                    } else {
                        //Отправить ошибку "Использован некорректный путь запроса"
                        writeResponse(httpExchange, "Некорректный путь", 404);
                    }
                    break;
                case "PATCH":
                    if (Pattern.matches("^/api/v1/tasks/subtask/$", path) && !query.isBlank()) {
                        if (Pattern.matches("^subtaskId=\\d+&epicId=\\d+$", query)) {
                            //Привязать подзадачу к эпику
                            String subtaskQueryId = query
                                    .replaceFirst("subtaskId=", "")
                                    .replaceFirst("&epicId=\\d+", "");
                            String epicQueryId = query
                                    .replaceFirst("subtaskId=\\d+", "")
                                    .replaceFirst("&epicId=", "");
                            try {
                                Subtask subtask = (Subtask) ((InMemoryTaskManager) taskManager)
                                        .getItemByIdWithoutSavingHistory(getIdFromPath(subtaskQueryId));
                                Epic epic = (Epic) ((InMemoryTaskManager) taskManager)
                                        .getItemByIdWithoutSavingHistory(getIdFromPath(epicQueryId));
                                taskManager.linkSubtaskToEpic(subtask, epic);
                                writeResponse(httpExchange, "", 200);
                            } catch (NullPointerException e) {
                                writeResponse(httpExchange,
                                        "Эпик или подзадача не найдены",
                                        404);
                            }
                        } else {
                            writeResponse(httpExchange, "Некорректный путь", 404);
                        }
                    } else {
                        //Отправить ошибку "Использован некорректный путь запроса"
                        writeResponse(httpExchange, "Некорректный путь", 404);
                    }
                default:
                    //Отправить ошибку "Метод не поддерживается"
                    writeResponse(httpExchange, "Метод не поддерживается", 400);
            }
        } catch (IOException e) {
            writeResponse(httpExchange, "Ошибка выполнения запроса", 500);
        } finally {
            httpExchange.close();
        }
    }

    private int getIdFromPath(String id) {
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
