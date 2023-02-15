package managers;

import exceptions.ManagerSaveException;
import model.Epic;
import model.ItemType;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private final Path autosaveFile = Paths.get("project_files/autosaveTest.txt");
    private List<String> fileLines;

    @Override
    protected void setTaskManager() {
        taskManager = (FileBackedTaskManager) Managers.getDefault(autosaveFile);
    }

    @BeforeEach
    public void beforeEach() {
        setTaskManager();
        taskManager.setIdCounter(1);

        // очистка файла
        try {
            if (Files.exists(autosaveFile)) {
                Files.delete(autosaveFile);
            }
            Files.createFile(autosaveFile);
        } catch (IOException e) {
            System.out.println("Ошибка при создании файла автосохранения");
        }
    }

    //Тесты на создание файла
    @Test
    public void shouldCreateHeaderInFile() {
        taskManager.createItem(new Task("task1", "task1_description"));
        try {
            fileLines = Files.readAllLines(autosaveFile);
        } catch (IOException e) {

        }
        String[] header = fileLines.get(0).split(",");

        assertEquals(header[0], "id", "Неверное расположение полей в файле");
        assertEquals(header[1], "type", "Неверное расположение полей в файле");
        assertEquals(header[2], "name", "Неверное расположение полей в файле");
        assertEquals(header[3], "status", "Неверное расположение полей в файле");
        assertEquals(header[4], "description", "Неверное расположение полей в файле");
        assertEquals(header[5], "duration", "Неверное расположение полей в файле");
        assertEquals(header[6], "startTime", "Неверное расположение полей в файле");
        assertEquals(header[7], "epic", "Неверное расположение полей в файле");
    }

    @Test
    public void shouldWriteToFileTaskWithMinimumFields() {
        Task task1 = new Task("task1");
        taskManager.createItem(task1);

        try {
            fileLines = Files.readAllLines(autosaveFile);
        } catch (IOException e) {

        }

        String[] line1 = fileLines.get(1).split(",");

        assertEquals("1", line1[0], "Неверное расположение полей в файле");
        assertEquals("TASK", line1[1], "Неверное расположение полей в файле");
        assertEquals("task1", line1[2], "Неверное расположение полей в файле");
        assertEquals("NEW", line1[3], "Неверное расположение полей в файле");
        assertEquals("", line1[4], "Неверное расположение полей в файле");
        assertEquals("", line1[5], "Неверное расположение полей в файле");
        assertEquals("", line1[6], "Неверное расположение полей в файле");
    }

    @Test
    public void shouldWriteToFileTaskWithAllFields() {
        Task task1 = new Task("task1", "task1_description");
        task1.setStartTime(LocalDateTime.parse("02-01-2023 12:00", formatter));
        task1.setDurationMinutes(Duration.of(120, ChronoUnit.MINUTES));
        taskManager.createItem(task1);

        try {
            fileLines = Files.readAllLines(autosaveFile);
        } catch (IOException e) {

        }

        String[] line1 = fileLines.get(1).split(",");

        assertEquals("1", line1[0], "Неверное расположение полей в файле");
        assertEquals("TASK", line1[1], "Неверное расположение полей в файле");
        assertEquals("task1", line1[2], "Неверное расположение полей в файле");
        assertEquals("NEW", line1[3], "Неверное расположение полей в файле");
        assertEquals("task1_description", line1[4], "Неверное расположение полей в файле");
        assertEquals("02-01-2023 12:00", line1[5], "Неверное расположение полей в файле");
        assertEquals("PT2H", line1[6], "Неверное расположение полей в файле");
    }

    @Test
    public void shouldCreateSubtaskWithAllFields() {
        Subtask subtask1 = new Subtask("subtask1", "subtask1_description");
        subtask1.setStartTime(LocalDateTime.parse("05-01-2023 12:00", formatter));
        subtask1.setDurationMinutes(Duration.of(120, ChronoUnit.MINUTES));
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        taskManager.createItem(subtask1);
        taskManager.addSubtask(subtask1, epic);

        try {
            fileLines = Files.readAllLines(autosaveFile);
        } catch (IOException e) {

        }

        String[] line1 = fileLines.get(2).split(",");

        assertEquals("2", line1[0], "Неверное расположение полей в файле");
        assertEquals("SUBTASK", line1[1], "Неверное расположение полей в файле");
        assertEquals("subtask1", line1[2], "Неверное расположение полей в файле");
        assertEquals("NEW", line1[3], "Неверное расположение полей в файле");
        assertEquals("subtask1_description", line1[4], "Неверное расположение полей в файле");
        assertEquals("05-01-2023 12:00", line1[5], "Неверное расположение полей в файле");
        assertEquals("PT2H", line1[6], "Неверное расположение полей в файле");
        assertEquals("1", line1[7], "Неверное расположение полей в файле");

    }

    @Test
    public void shouldWriteHistoryToFileAfterBlancLine() {
        //Создание тестовых items
        Task task1 = new Task("task1", "task1_description");
        taskManager.createItem(task1);
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);

        //Запись истории
        taskManager.getItemById(subtask1.getId());
        taskManager.getItemById(task1.getId());

        try {
            fileLines = Files.readAllLines(autosaveFile);
        } catch (IOException e) {

        }

        String[] historyLine = fileLines.get(4).split(",");

        assertEquals("2", historyLine[0], "Неверное отображение истории");
        assertEquals("1", historyLine[1], "Неверное отображение истории");
    }

    @Test
    public void shouldDeleteItemFromFileIfItemIsRemovedById() {
        Task task = new Task("task");
        taskManager.createItem(task);
        taskManager.removeItemById(task.getId());

        try {
            fileLines = Files.readAllLines(autosaveFile);
        } catch (IOException e) {

        }

        assertEquals("", fileLines.get(1), "Задача не удалена из файла");
    }

    @Test
    public void shouldDeleteItemFromFileIfItemIsRemovedByType() {
        Task task1 = new Task("task1");
        Task task2 = new Task("task2");
        taskManager.createItem(task1);
        taskManager.createItem(task2);
        taskManager.removeAllItemsByType(ItemType.TASK);

        try {
            fileLines = Files.readAllLines(autosaveFile);
        } catch (IOException e) {

        }

        assertEquals("", fileLines.get(1), "Задачи не удалены из файла");
    }

    @Test
    public void shouldUpdateFileWhenItemIsUpdate() {
        Task task = new Task("task");
        taskManager.createItem(task);
        Task updatedTask = new Task("task", "added_description");
        updatedTask.setId(task.getId());
        taskManager.updateItem(updatedTask, task.getId());

        try {
            fileLines = Files.readAllLines(autosaveFile);
        } catch (IOException e) {

        }

        String[] historyLine = fileLines.get(1).split(",");

        assertEquals("added_description", historyLine[4], "Задача не обновлена");
    }

    @Test
    public void shouldThrowExceptionIfErrorWhileSavingToFile() {
        FileBackedTaskManager badManager = (FileBackedTaskManager) Managers.getDefault(
                Paths.get("C://ProgramFiles/project_files/autosaveTest.txt"));

        Task task = new Task("task");

        Assertions.assertThrows(ManagerSaveException.class,
                () -> badManager.createItem(task),
                "Пропущена ошибка сохранения в файл");
    }

    //Восстановление из файла
    @Test
    public void shouldRestoreAllItemsFromFile() {

        //Подготовка менеджера
        Task task = new Task("task");
        taskManager.createItem(task);
        Epic epic1 = new Epic("epic1");
        taskManager.createItem(epic1);
        Epic epic2 = new Epic("epic2");
        taskManager.createItem(epic2);
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        taskManager.addSubtask(subtask1, epic1);
        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        taskManager.addSubtask(subtask2, epic1);
        Subtask subtask3 = new Subtask("subtask3");
        taskManager.createItem(subtask3);
        taskManager.addSubtask(subtask3, epic1);

        FileBackedTaskManager restoredManager = FileBackedTaskManager.loadFromFile(autosaveFile);

        //Проверка полноты восстановления
        assertEquals(1, restoredManager.getAllItemsByType(ItemType.TASK).size(),
                "Не все задачи восстановлены");
        assertEquals(2, restoredManager.getAllItemsByType(ItemType.EPIC).size(),
                "Не все задачи восстановлены");
        assertEquals(3, restoredManager.getAllItemsByType(ItemType.SUBTASK).size(),
                "Не все задачи восстановлены");

        //Проверка знания подзадач об эпиках
        assertEquals(epic1.getId(), ((Subtask) restoredManager.getItemById(subtask1.getId())).getEpicId(),
                "Подзадача не привязана к эпику");
        assertEquals(epic1.getId(), ((Subtask) restoredManager.getItemById(subtask2.getId())).getEpicId(),
                "Подзадача не привязана к эпику");
        assertEquals(epic1.getId(), ((Subtask) restoredManager.getItemById(subtask3.getId())).getEpicId(),
                "Подзадача не привязана к эпику");

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
    public void shouldRestoreHistoryFromFile() {
        Task task = new Task("task");
        taskManager.createItem(task);
        Epic epic = new Epic("epic1");
        taskManager.createItem(epic);

        taskManager.getItemById(task.getId());
        taskManager.getItemById(epic.getId());

        FileBackedTaskManager restoredManager = FileBackedTaskManager.loadFromFile(autosaveFile);
        HistoryManager restoredHistoryManager = restoredManager.getHistoryManager();
        List<Task> history = restoredHistoryManager.getHistory();

        assertEquals(task.getId(), history.get(0).getId(),
                "Ошибка восстановления истории");
        assertEquals(epic.getId(), history.get(1).getId(),
                "Ошибка восстановления истории");
    }

    @Test
    public void shouldRestorePrioritizedListFromFile() {
        Task task1 = new Task("task1");
        task1.setStartTime(LocalDateTime.parse("01-01-2023 11:50", formatter));
        taskManager.createItem(task1);
        Task task2 = new Task("task2");
        task2.setStartTime(LocalDateTime.parse("02-01-2023 11:50", formatter));
        taskManager.createItem(task2);
        Task task3 = new Task("task3");
        task3.setStartTime(LocalDateTime.parse("03-01-2023 11:50", formatter));
        taskManager.createItem(task3);

        FileBackedTaskManager restoredManager = FileBackedTaskManager.loadFromFile(autosaveFile);
        ArrayList<Task> restoredPrioritizedList = restoredManager.getPrioritizedTasks();

        assertEquals(task1.getId(), restoredPrioritizedList.get(0).getId(),
                "Ошибка восстановления задач по приоритету");
        assertEquals(task2.getId(), restoredPrioritizedList.get(1).getId(),
                "Ошибка восстановления задач по приоритету");
        assertEquals(task3.getId(), restoredPrioritizedList.get(2).getId(),
                "Ошибка восстановления задач по приоритету");
    }

    @Test
    public void shouldCalculateEpicEndTimeWhileBeingRestored() {
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
        taskManager.addSubtask(subtask1, epic);
        taskManager.addSubtask(subtask2, epic);

        taskManager.updateEpicStartTimeDurationEndTime(epic.getId());

        FileBackedTaskManager restoredManager = FileBackedTaskManager.loadFromFile(autosaveFile);

        Epic restoredEpic = (Epic) restoredManager.getItemById(epic.getId());

        assertEquals(epic.getEndTime(), restoredEpic.getEndTime(),
                "Ошибка восстановления время завершения эпика");
    }
}
