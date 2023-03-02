package model;

import adapters.DurationAdapter;
import adapters.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import managers.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import server.KVServer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EpicTest {

    private TaskManager taskManager;
    private Gson gson;
    private static KVServer kvServer;
    private static DateTimeFormatter formatter;
    private Path autosaveFile;
    private String host;

    private void setTaskManager (TaskManagerType taskManagerType) {
        switch (taskManagerType) {
            case IN_MEMORY:
                taskManager = Managers.getDefault();
                break;
            case FILE:
                autosaveFile = Paths.get("project_files/autosaveTest.txt");
                taskManager = Managers.getDefault(autosaveFile);
                break;
            case HTTP:
                host = "http://localhost:8080";
                try {
                    taskManager = Managers.getDefault(host);
                } catch (IOException | InterruptedException e) {
                    System.out.println(Arrays.toString(e.getStackTrace()));
                }

                gson = new GsonBuilder()
                        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                        .registerTypeAdapter(Duration.class, new DurationAdapter())
                        .create();
                break;
        }
    }

    private enum TaskManagerType {
        IN_MEMORY,
        FILE,
        HTTP
    }
    @BeforeAll
    public static void beforeAll() throws IOException {
        kvServer = new KVServer();
        kvServer.start();
        formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    }

    @AfterAll
    public static void afterAll() {
        kvServer.stop();
    }

    @Test
    public void shouldUpdateEpicStatusToNewIfOnlySubtaskIsNew() {
        //Подготовка данных
        setTaskManager(TaskManagerType.IN_MEMORY);
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        subtask1.setStatus(Status.NEW);
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);

        //Тестируемая логика
        taskManager.linkSubtaskToEpic(subtask1, epic);

        //Проверка статуса [1:'NEW']
        assertEquals(Status.NEW, epic.getStatus(), "Неверный статус эпика");
    }

    @Test
    public void shouldUpdateEpicStatusToInProgressIfOnlySubtaskIsInProgress() {
        //Подготовка данных
        setTaskManager(TaskManagerType.IN_MEMORY);
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        subtask1.setStatus(Status.NEW);
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        taskManager.linkSubtaskToEpic(subtask1, epic);

        //Тестируемая логика
        subtask1.setStatus(Status.IN_PROGRESS);
        taskManager.updateItem(subtask1, subtask1.getId());
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Неверный статус эпика");

        //Проверка статуса [1: 'IN_PROGRESS']
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Неверный статус эпика");
    }

    @Test
    public void shouldUpdateEpicStatusToNewIfOneOfSubtaskIsNew() {
        //Подготовка данных
        setTaskManager(TaskManagerType.IN_MEMORY);
        Subtask subtask1 = new Subtask("subtask1");
        subtask1.setStatus(Status.IN_PROGRESS);
        taskManager.createItem(subtask1);
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        taskManager.linkSubtaskToEpic(subtask1, epic);
        Subtask subtask2 = new Subtask("subtask2");
        subtask2.setStatus(Status.NEW);
        taskManager.createItem(subtask2);

        //Тестируемая логика
        taskManager.linkSubtaskToEpic(subtask2, epic);

        //Проверка статуса [1: 'IN_PROGRESS', 2: 'NEW']
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Неверный статус эпика");
    }

    @Test
    public void shouldUpdateEpicStatusToNewIfAllSubtasksIsNew() {
        //Подготовка данных
        setTaskManager(TaskManagerType.IN_MEMORY);
        Subtask subtask1 = new Subtask("subtask1");
        subtask1.setStatus(Status.IN_PROGRESS);
        taskManager.createItem(subtask1);
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        taskManager.linkSubtaskToEpic(subtask1, epic);
        Subtask subtask2 = new Subtask("subtask2");
        subtask2.setStatus(Status.NEW);
        taskManager.createItem(subtask2);
        taskManager.linkSubtaskToEpic(subtask2, epic);

        //Тестируемая логика
        subtask1.setStatus(Status.NEW);
        taskManager.updateItem(subtask1, subtask1.getId());

        //Проверка статуса [1: 'NEW', 2: 'NEW']
        assertEquals(Status.NEW, epic.getStatus(), "Неверный статус эпика");
    }

    @Test
    public void shouldUpdateEpicStatusToInProgressIfOneOfSubtasksIsDone() {
        //Подготовка данных
        setTaskManager(TaskManagerType.IN_MEMORY);
        Subtask subtask1 = new Subtask("subtask1");
        subtask1.setStatus(Status.NEW);
        taskManager.createItem(subtask1);
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        taskManager.linkSubtaskToEpic(subtask1, epic);
        Subtask subtask2 = new Subtask("subtask2");
        subtask2.setStatus(Status.NEW);
        taskManager.createItem(subtask2);
        taskManager.linkSubtaskToEpic(subtask2, epic);

        //Тестируемая логика
        subtask1.setStatus(Status.DONE);
        taskManager.updateItem(subtask1, subtask1.getId());

        //Проверка статуса [1: 'DONE', 2: 'NEW']
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Неверный статус эпика");
    }

    @Test
    public void shouldUpdateEpicStatusToDoneIfAllSubtasksIsDone() {
        //Подготовка данных
        setTaskManager(TaskManagerType.IN_MEMORY);
        Subtask subtask1 = new Subtask("subtask1");
        subtask1.setStatus(Status.NEW);
        taskManager.createItem(subtask1);
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        taskManager.linkSubtaskToEpic(subtask1, epic);
        Subtask subtask2 = new Subtask("subtask2");
        subtask2.setStatus(Status.DONE);
        taskManager.createItem(subtask2);
        taskManager.linkSubtaskToEpic(subtask2, epic);

        //Тестируемая логика
        subtask1.setStatus(Status.DONE);
        taskManager.updateItem(subtask1, subtask1.getId());

        //Проверка статуса //[1: 'DONE', 2: 'DONE']
        assertEquals(Status.DONE, epic.getStatus(), "Неверный статус эпика");
    }

    @Test
    public void shouldUpdateEpicStatusWhenSubtaskIsRemovedById() {
        //Подготовка данных
        setTaskManager(TaskManagerType.IN_MEMORY);
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        Subtask subtask1 = new Subtask("subtask1");
        subtask1.setStatus(Status.NEW);
        taskManager.createItem(subtask1);
        taskManager.linkSubtaskToEpic(subtask1, epic);
        Subtask subtask2 = new Subtask("subtask2");
        subtask2.setStatus(Status.DONE);
        taskManager.createItem(subtask2);
        taskManager.linkSubtaskToEpic(subtask2, epic);

        //Тестируемая логика
        taskManager.removeItemById(subtask2.getId());

        //Проверка статуса [1: 'NEW']
        assertEquals(Status.NEW, epic.getStatus(), "Неверный статус эпика");
    }

    @Test
    public void shouldUpdateEpicStatusWhenSubtaskIsRemovedByType() {
        //Подготовка данных
        setTaskManager(TaskManagerType.IN_MEMORY);
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        Subtask subtask1 = new Subtask("subtask1");
        subtask1.setStatus(Status.DONE);
        taskManager.createItem(subtask1);
        taskManager.linkSubtaskToEpic(subtask1, epic);
        Subtask subtask2 = new Subtask("subtask2");
        subtask2.setStatus(Status.DONE);
        taskManager.createItem(subtask2);
        taskManager.linkSubtaskToEpic(subtask2, epic);

        //Тестируемая логика
        taskManager.removeAllItemsByType(ItemType.SUBTASK);

        //Проверка статуса [no subtasks]
        assertEquals(Status.NEW, epic.getStatus(), "Неверный статус эпика");
    }

    //Тесты расчета startTime для Epic
    @Test
    public void shouldSetMinOfEpicSubtasksStartTimeWhenCreate() {
        //Подготовка данных
        setTaskManager(TaskManagerType.IN_MEMORY);
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        taskManager.linkSubtaskToEpic(subtask1, epic);
        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        taskManager.linkSubtaskToEpic(subtask2, epic);
        Subtask subtask3 = new Subtask("subtask3");
        subtask3.setStartTime(LocalDateTime.parse("02-01-2023 12:00", formatter));
        subtask3.setEpicId(epic.getId());

        //Тестируемая логика
        taskManager.createItem(subtask3);
        taskManager.linkSubtaskToEpic(subtask3, epic);

        //Проверка расчета времени
        assertEquals(LocalDateTime.of(2023, 1, 2, 12, 0),
                epic.getStartTime(),
                "startTime неверно для Epic");
    }

    @Test
    public void shouldSetMinOfEpicSubtasksStartTimeWhenUpdate() {
        //Подготовка данных
        setTaskManager(TaskManagerType.IN_MEMORY);
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        taskManager.linkSubtaskToEpic(subtask1, epic);
        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        taskManager.linkSubtaskToEpic(subtask2, epic);
        Subtask subtask3 = new Subtask("subtask3");
        subtask3.setStartTime(LocalDateTime.parse("02-01-2023 12:00", formatter));
        subtask3.setEpicId(epic.getId());
        subtask1.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        subtask2.setStartTime(LocalDateTime.parse("03-02-2023 16:20", formatter));

        //Тестируемая логика
        taskManager.updateItem(subtask1, subtask1.getId());
        taskManager.updateItem(subtask2, subtask2.getId());

        //Проверка расчета времени
        assertEquals(LocalDateTime.of(2023, 1, 1, 12, 0),
                epic.getStartTime(),
                "startTime неверно для Epic");
    }

    @Test
    public void shouldSetMinOfEpicSubtasksStartTimeWhenRemove() {
        //Подготовка данных
        setTaskManager(TaskManagerType.IN_MEMORY);
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        taskManager.linkSubtaskToEpic(subtask1, epic);
        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        taskManager.linkSubtaskToEpic(subtask2, epic);
        Subtask subtask3 = new Subtask("subtask3");
        subtask3.setStartTime(LocalDateTime.parse("02-01-2023 12:00", formatter));
        taskManager.createItem(subtask3);
        taskManager.linkSubtaskToEpic(subtask3, epic);
        subtask1.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        subtask2.setStartTime(LocalDateTime.parse("03-02-2023 16:20", formatter));
        taskManager.updateItem(subtask1, subtask1.getId());
        taskManager.updateItem(subtask2, subtask2.getId());

        //Тестируемая логика
        taskManager.removeItemById(subtask1.getId());

        //Проверка расчета времени
        assertEquals(LocalDateTime.of(2023, 1, 2, 12, 0),
                epic.getStartTime(),
                "startTime неверно для Epic");
    }

    @Test
    public void shouldReturnNullIfEpicSubtasksStartTimeIsNull() {
        //Подготовка данных
        setTaskManager(TaskManagerType.IN_MEMORY);
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        taskManager.linkSubtaskToEpic(subtask1, epic);
        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        taskManager.linkSubtaskToEpic(subtask2, epic);

        //Проверка на null
        Assertions.assertNull(epic.getStartTime(),
                "startTime не null для Epic");
    }

    //Тесты расчета duration для Epic
    @Test
    public void shouldSetSumOfEpicSubtasksDurationWhenCreate() {
        //Подготовка данных
        setTaskManager(TaskManagerType.IN_MEMORY);
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        taskManager.linkSubtaskToEpic(subtask1, epic);
        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        taskManager.linkSubtaskToEpic(subtask2, epic);
        Subtask subtask3 = new Subtask("subtask3");
        subtask3.setDurationMinutes(Duration.of(10, ChronoUnit.MINUTES));
        subtask3.setEpicId(epic.getId());

        //Тестируемая логика
        taskManager.createItem(subtask3);
        taskManager.linkSubtaskToEpic(subtask3, epic);

        //Проверка расчета продолжительности
        Assertions.assertEquals(Duration.of(10, ChronoUnit.MINUTES),
                epic.getDurationMinutes(),
                "Неверно расcчитано duration для Epic");
    }

    @Test
    public void shouldSetSumOfEpicSubtasksDurationWhenUpdate() {
        //Подготовка данных
        setTaskManager(TaskManagerType.IN_MEMORY);
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        taskManager.linkSubtaskToEpic(subtask1, epic);
        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        taskManager.linkSubtaskToEpic(subtask2, epic);
        subtask1.setDurationMinutes(Duration.of(30, ChronoUnit.MINUTES));
        subtask2.setDurationMinutes(Duration.of(120, ChronoUnit.MINUTES));

        //Тестируемая логика
        taskManager.updateItem(subtask1, subtask1.getId());
        taskManager.updateItem(subtask2, subtask2.getId());

        //Проверка расчета продолжительности
        Assertions.assertEquals(Duration.of(150, ChronoUnit.MINUTES),
                epic.getDurationMinutes(),
                "Неверно расcчитано duration для Epic");
    }

    @Test
    public void shouldSetSumOfEpicSubtasksDurationWhenRemove() {
        //Подготовка данных
        setTaskManager(TaskManagerType.IN_MEMORY);
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        taskManager.linkSubtaskToEpic(subtask1, epic);
        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        taskManager.linkSubtaskToEpic(subtask2, epic);
        subtask1.setDurationMinutes(Duration.of(30, ChronoUnit.MINUTES));
        subtask2.setDurationMinutes(Duration.of(120, ChronoUnit.MINUTES));

        //Тестируемая логика
        taskManager.removeItemById(subtask2.getId());

        //Проверка расчета продолжительности
        Assertions.assertEquals(Duration.of(30, ChronoUnit.MINUTES),
                epic.getDurationMinutes(),
                "Неверно расcчитано duration для Epic");
    }

    @Test
    public void shouldReturnZeroIfEpicSubtasksDurationIsNull() {
        //Подготовка данных
        setTaskManager(TaskManagerType.IN_MEMORY);
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        taskManager.linkSubtaskToEpic(subtask1, epic);
        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        taskManager.linkSubtaskToEpic(subtask2, epic);

        //Проверка расчета продолжительности
        Assertions.assertEquals(0,
                epic.getDurationMinutes().toMinutes(),
                "duration не равно 0");
    }

    //Тесты для расчета endTime для Epic
    @Test
    public void shouldSetCorrectEndTimeForEpicWhenCreate() {
        //Подготовка данных
        setTaskManager(TaskManagerType.IN_MEMORY);
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        subtask1.setStartTime(LocalDateTime.parse("02-01-2023 12:00", formatter));
        subtask1.setDurationMinutes(Duration.of(10, ChronoUnit.MINUTES));
        taskManager.linkSubtaskToEpic(subtask1, epic);
        Subtask subtask2 = new Subtask("subtask4");
        subtask2.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        subtask2.setDurationMinutes(Duration.of(120, ChronoUnit.MINUTES));
        subtask2.setEpicId(epic.getId());

        //Тестируемая логика
        taskManager.createItem(subtask2);
        taskManager.linkSubtaskToEpic(subtask2, epic);

        //Проверка корректности расчета времени окончания
        Assertions.assertEquals(LocalDateTime.parse("02-01-2023 12:10", formatter),
                epic.getEndTime().get(),
                "Неверно расcчитан endTime для Epic");
    }

    @Test
    public void shouldSetCorrectEndTimeForEpicWhenUpdate() {
        //Подготовка данных
        setTaskManager(TaskManagerType.IN_MEMORY);
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        subtask1.setStartTime(LocalDateTime.parse("02-01-2023 12:00", formatter));
        subtask1.setDurationMinutes(Duration.of(10, ChronoUnit.MINUTES));
        taskManager.linkSubtaskToEpic(subtask1, epic);
        Subtask subtask2 = new Subtask("subtask4");
        subtask2.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        subtask2.setDurationMinutes(Duration.of(120, ChronoUnit.MINUTES));
        subtask2.setEpicId(epic.getId());
        taskManager.createItem(subtask2);

        //Тестируемая логика
        subtask2.setStartTime(LocalDateTime.parse("03-02-2023 16:20", formatter));
        subtask2.setDurationMinutes(Duration.of(120, ChronoUnit.MINUTES));
        taskManager.updateItem(subtask2, subtask2.getId());

        //Проверка корректности расчета времени окончания
        Assertions.assertEquals(LocalDateTime.parse("03-02-2023 18:20", formatter),
                epic.getEndTime().get(),
                "Неверно расcчитан endTime для Epic");
    }

    @Test
    public void shouldSetCorrectEndTimeForEpicWhenRemove() {
        //Подготовка данных
        setTaskManager(TaskManagerType.IN_MEMORY);
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        subtask1.setStartTime(LocalDateTime.parse("02-01-2023 12:00", formatter));
        subtask1.setDurationMinutes(Duration.of(10, ChronoUnit.MINUTES));
        taskManager.linkSubtaskToEpic(subtask1, epic);
        Subtask subtask2 = new Subtask("subtask4");
        subtask2.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        subtask2.setDurationMinutes(Duration.of(120, ChronoUnit.MINUTES));
        subtask2.setEpicId(epic.getId());
        taskManager.createItem(subtask2);

        //Тестируемая логика
        taskManager.removeItemById(subtask2.getId());

        //Проверка корректности расчета времени окончания
        Assertions.assertEquals(LocalDateTime.parse("02-01-2023 12:10", formatter),
                epic.getEndTime().get(),
                "Неверно расcчитан endTime для Epic");
    }

    @Test
    public void shouldSetCorrectEntTimeIfDurationIsAbsentInSubtask() {
        setTaskManager(TaskManagerType.IN_MEMORY);
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        taskManager.linkSubtaskToEpic(subtask1, epic);
        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        taskManager.linkSubtaskToEpic(subtask2, epic);
        Subtask subtask3WithOutStartDate = new Subtask("subtask3WithOutStartDate");
        taskManager.createItem(subtask3WithOutStartDate);
        taskManager.linkSubtaskToEpic(subtask3WithOutStartDate, epic);
        subtask1.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        subtask1.setDurationMinutes(Duration.of(30, ChronoUnit.MINUTES));
        taskManager.updateItem(subtask1, subtask1.getId());

        subtask2.setStartTime(LocalDateTime.parse("03-02-2023 16:20", formatter));

        Assertions.assertEquals(LocalDateTime.parse("01-01-2023 12:30", formatter),
                epic.getEndTime().get(),
                "Неверно расcчитан endTime для Epic");
    }

    @Test
    public void shouldSetCorrectEntTimeIfStartTimeIsAbsentInSubtask() {
        //Подготовка данных
        setTaskManager(TaskManagerType.IN_MEMORY);
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        taskManager.linkSubtaskToEpic(subtask1, epic);
        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        taskManager.linkSubtaskToEpic(subtask2, epic);
        Subtask subtask3WithOutDuration = new Subtask("subtask3WithOutStartDate");
        taskManager.createItem(subtask3WithOutDuration);
        taskManager.linkSubtaskToEpic(subtask3WithOutDuration, epic);
        subtask1.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        subtask1.setDurationMinutes(Duration.of(30, ChronoUnit.MINUTES));
        taskManager.updateItem(subtask1, subtask1.getId());
        subtask2.setDurationMinutes(Duration.of(120, ChronoUnit.MINUTES));
        taskManager.updateItem(subtask2, subtask2.getId());

        //Проверка логики расчета даты
        Assertions.assertEquals(LocalDateTime.parse("01-01-2023 12:30", formatter),
                epic.getEndTime().get(),
                "Неверно расcчитан endTime для Epic");
    }

    @Test
    public void shouldRestoreInfoAboutEpicsForSubtasks() {
        setTaskManager(TaskManagerType.FILE);
        //Подготовка данных
        Task task = new Task("task");
        taskManager.createItem(task);
        Epic epic1 = new Epic("epic1");
        taskManager.createItem(epic1);
        Epic epic2 = new Epic("epic2");
        taskManager.createItem(epic2);
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        taskManager.linkSubtaskToEpic(subtask1, epic1);
        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        taskManager.linkSubtaskToEpic(subtask2, epic1);
        Subtask subtask3 = new Subtask("subtask3");
        taskManager.createItem(subtask3);
        taskManager.linkSubtaskToEpic(subtask3, epic1);

        //Тестируемая логика
        FileBackedTaskManager restoredManager = FileBackedTaskManager.loadFromFile(autosaveFile);

        //Проверка знания подзадач об эпиках
        assertEquals(epic1.getId(), ((Subtask) restoredManager.getItemById(subtask1.getId())).getEpicId(),
                "Подзадача не привязана к эпику");
        assertEquals(epic1.getId(), ((Subtask) restoredManager.getItemById(subtask2.getId())).getEpicId(),
                "Подзадача не привязана к эпику");
        assertEquals(epic1.getId(), ((Subtask) restoredManager.getItemById(subtask3.getId())).getEpicId(),
                "Подзадача не привязана к эпику");
    }

    @Test
    public void shouldRestoreAllInfoAboutSubtasksInEpics() {
        //Подготовка данных
        setTaskManager(TaskManagerType.FILE);
        Task task = new Task("task");
        taskManager.createItem(task);
        Epic epic1 = new Epic("epic1");
        taskManager.createItem(epic1);
        Epic epic2 = new Epic("epic2");
        taskManager.createItem(epic2);
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        taskManager.linkSubtaskToEpic(subtask1, epic1);
        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        taskManager.linkSubtaskToEpic(subtask2, epic1);
        Subtask subtask3 = new Subtask("subtask3");
        taskManager.createItem(subtask3);
        taskManager.linkSubtaskToEpic(subtask3, epic1);

        //Тестируемая логика
        FileBackedTaskManager restoredManager = FileBackedTaskManager.loadFromFile(autosaveFile);

        //Проверка знания эпиков о подзадачах
        assertTrue(((Epic) restoredManager.
                        getItemById(epic1.getId()))
                        .getEpicSubtaskIds()
                        .contains(subtask1.getId()),
                "Подзадача не привязана к эпику");
        assertTrue(((Epic) restoredManager.
                        getItemById(epic1.getId()))
                        .getEpicSubtaskIds()
                        .contains(subtask2.getId()),
                "Подзадача не привязана к эпику");
        assertTrue(((Epic) restoredManager.
                        getItemById(epic1.getId()))
                        .getEpicSubtaskIds()
                        .contains(subtask3.getId()),
                "Подзадача не привязана к эпику");
    }

    @Test
    public void shouldCalculateEpicEndTimeWhileBeingRestored() {
        //Подготовка данных
        setTaskManager(TaskManagerType.FILE);
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        Subtask subtask1 = new Subtask("subtask1");
        subtask1.setStartTime(LocalDateTime.parse("01-01-2023 11:20", formatter));
        subtask1.setDurationMinutes(Duration.of(30, ChronoUnit.MINUTES));
        taskManager.createItem(subtask1);
        Subtask subtask2 = new Subtask("subtask2");
        subtask2.setStartTime(LocalDateTime.parse("02-01-2023 11:10", formatter));
        subtask2.setDurationMinutes(Duration.of(30, ChronoUnit.MINUTES));
        taskManager.createItem(subtask2);
        taskManager.linkSubtaskToEpic(subtask1, epic);
        taskManager.linkSubtaskToEpic(subtask2, epic);

        //Тестируемая логика
        FileBackedTaskManager restoredManager = FileBackedTaskManager.loadFromFile(autosaveFile);

        //Проверка времени эпика
        Epic restoredEpic = (Epic) restoredManager.getItemById(epic.getId());
        assertEquals(epic.getEndTime(), restoredEpic.getEndTime(),
                "Ошибка восстановления время завершения эпика");
    }

    @Test
    public void shouldRestoreInfoAboutEpicForSubtasks() throws IOException, InterruptedException {
        setTaskManager(TaskManagerType.HTTP);
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        taskManager.linkSubtaskToEpic(subtask1, epic);
        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        taskManager.linkSubtaskToEpic(subtask2, epic);

        TaskManager loadedTaskManager = HttpTaskManager.loadFromServer(host);

        Subtask loadedSubtask1 = (Subtask) loadedTaskManager.getItemById(subtask1.getId());
        Subtask loadedSubtask2 = (Subtask) loadedTaskManager.getItemById(subtask2.getId());
        assertEquals(epic.getId(), loadedSubtask1.getEpicId());
        assertEquals(epic.getId(), loadedSubtask2.getEpicId());
    }

    @Test
    public void shouldRestoreInfoAboutSubtasksForEpic() throws IOException, InterruptedException {
        setTaskManager(TaskManagerType.HTTP);
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        taskManager.linkSubtaskToEpic(subtask1, epic);
        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        taskManager.linkSubtaskToEpic(subtask2, epic);

        TaskManager loadedTaskManager = HttpTaskManager.loadFromServer(host);

        Epic loadedEpic = (Epic) loadedTaskManager.getItemById(epic.getId());
        assertTrue(loadedEpic.getEpicSubtaskIds().contains(subtask1.getId()));
        assertTrue(loadedEpic.getEpicSubtaskIds().contains(subtask2.getId()));
        assertEquals(2, epic.getEpicSubtaskIds().size());
    }

}
