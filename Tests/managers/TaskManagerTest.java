package managers;

import exceptions.NoSuchTaskExists;
import exceptions.TaskTimeIntersectionException;
import model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected static DateTimeFormatter formatter;
    protected T taskManager;

    @BeforeAll
    public static void beforeAll() {
        formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    }

    protected void setTaskManager() {
        this.taskManager = (T) Managers.getDefault();
    }

    @BeforeEach
    public void beforeEach() {
        setTaskManager();
        ((InMemoryTaskManager) taskManager).setIdCounter(1);
    }

    //Тесты создания задач
    @Test
    public void shouldCreateNewTask() {
        //создание первой задачи с типом Task
        Task testTask1 = new Task("Test_task1");
        int testTask1Id = taskManager.createItem(testTask1);

        Task savedTask = taskManager.getItemById(testTask1Id);
        assertNotNull(savedTask, "Задача не найдена");
        assertEquals(testTask1, savedTask, "Задачи не совпадают");

        List<Task> createdTasks = taskManager.getAllItemsByType(ItemType.TASK);

        assertNotNull(createdTasks, "Список задач пуст");
        assertEquals(1, createdTasks.size(), "Неверное количество элементов в списке задач");
        assertTrue(createdTasks.contains(testTask1), "Задача не найдена");

        //создание второй и более задач с типом Task
        Task testTask2 = new Task("Test_task2");
        int testTask2Id = taskManager.createItem(testTask2);

        savedTask = taskManager.getItemById(testTask2Id);
        assertNotNull(savedTask, "Задача не найдена");
        assertEquals(testTask2, savedTask, "Задачи не совпадают");

        createdTasks = taskManager.getAllItemsByType(ItemType.TASK);
        assertNotNull(createdTasks, "Список задач пуст");
        assertEquals(2, createdTasks.size(), "Неверное количество элементов в списке задач");
        assertTrue(createdTasks.contains(testTask2), "Задача не найдена");
        assertTrue(createdTasks.contains(testTask1), "Задача не найдена");
    }

    @Test
    public void shouldCreateNewSubtask() {
        //создание первой задачи с типом Subtask
        Subtask testSubtask1 = new Subtask("Test_subtask1");
        int testSubtask1Id = taskManager.createItem(testSubtask1);

        Task savedSubtask = taskManager.getItemById(testSubtask1Id);
        assertNotNull(savedSubtask, "Подзадача не найдена");
        assertEquals(testSubtask1, savedSubtask, "Подзадачи не совпадают");

        List<Task> createdSubtasks = taskManager.getAllItemsByType(ItemType.SUBTASK);

        assertNotNull(createdSubtasks, "Список подзадач пуст");
        assertEquals(1, createdSubtasks.size(), "Неверное количество элементов в списке подзадач");
        assertTrue(createdSubtasks.contains(testSubtask1), "Подзадача не найдена");

        //создание второй и более задач с типом Subtask
        Subtask testSubtask2 = new Subtask("Test_subtask2");
        int testSubtask2Id = taskManager.createItem(testSubtask2);

        savedSubtask = taskManager.getItemById(testSubtask2Id);
        assertNotNull(savedSubtask, "Подзадача не найдена");
        assertEquals(testSubtask2, savedSubtask, "Подзадачи не совпадают");

        createdSubtasks = taskManager.getAllItemsByType(ItemType.SUBTASK);
        assertNotNull(createdSubtasks, "Список подзадач пуст");
        assertEquals(2, createdSubtasks.size(), "Неверное количество элементов в списке подзадач");
        assertTrue(createdSubtasks.contains(testSubtask2), "Подзадача не найдена");
        assertTrue(createdSubtasks.contains(testSubtask1), "Подзадача не найдена");
    }

    @Test
    public void shouldCreateNewEpic() {
        //создание первой задачи с типом Epic
        Epic testEpic1 = new Epic("Test_epic1");
        int testEpic1Id = taskManager.createItem(testEpic1);

        Task savedEpic = taskManager.getItemById(testEpic1Id);
        assertNotNull(savedEpic, "Эпик не найден");
        assertEquals(testEpic1, savedEpic, "Эпики не совпадают");

        List<Task> createdEpics = taskManager.getAllItemsByType(ItemType.EPIC);

        assertNotNull(createdEpics, "Список эпиков пуст");
        assertEquals(1, createdEpics.size(), "Неверное количество элементов в списке эпиков");
        assertTrue(createdEpics.contains(testEpic1), "Эпик не найден");

        //создание второй и более задач с типом Epic
        Epic testEpic2 = new Epic("Test_epic2");
        int testEpic2Id = taskManager.createItem(testEpic2);

        savedEpic = taskManager.getItemById(testEpic2Id);
        assertNotNull(savedEpic, "Эпик не найден");
        assertEquals(testEpic2, savedEpic, "Эпики не совпадают");

        createdEpics = taskManager.getAllItemsByType(ItemType.EPIC);
        assertNotNull(createdEpics, "Список эпиков пуст");
        assertEquals(2, createdEpics.size(), "Неверное количество элементов в списке эпиков");
        assertTrue(createdEpics.contains(testEpic2), "Эпик не найден");
        assertTrue(createdEpics.contains(testEpic1), "Эпик не найден");
    }

    @Test
    public void shouldIncrementItemCounterAfterCreatingTask() {
        Task testTask1 = new Task("Test_task1");
        taskManager.createItem(testTask1);

        assertEquals(2, taskManager.getIdCounter(), "Неверный id задачи");

        Task testTask2 = new Task("Test_task2");
        taskManager.createItem(testTask2);

        assertEquals(3, taskManager.getIdCounter(), "Неверный id задачи");
    }

    @Test
    public void shouldNotCreateTaskOfNull() {
        Task task = null;

        assertThrows(NullPointerException.class,
                () -> taskManager.createItem(task),
                "Задача создана из null");
    }

    //Получение списка задач
    @Test
    public void shouldReturnEmptyListIfTasksOfGivenTypeDoNotExist() {

        assertThrows(NullPointerException.class,
                () -> taskManager.getAllItemsByType(ItemType.TASK),
                "Список не пустой");

        assertThrows(NullPointerException.class,
                () -> taskManager.getAllItemsByType(ItemType.SUBTASK),
                "Список не пустой");

        assertThrows(NullPointerException.class,
                () -> taskManager.getAllItemsByType(ItemType.EPIC),
                "Список не пустой");
    }

    //Обновление задач
    @Test
    public void shouldUpdateTask() {
        Task task1 = new Task("Задача, которая будет изменена");
        Task task2 = new Task("Неизменяемая задача");

        int task1Id = taskManager.createItem(task1);
        taskManager.createItem(task2);

        task1.setName("Новое имя задачи");
        taskManager.updateItem(task1, task1Id);
        Task updatedTask = taskManager.getItemById(task1Id);

        assertNotNull(updatedTask, "Обновленная задача не существует");
        assertEquals(updatedTask, task1, "Задачи не совпадают");
        assertEquals("Новое имя задачи", updatedTask.getName(), "Задача не изменена");

        List<Task> tasks = taskManager.getAllItemsByType(ItemType.TASK);

        assertTrue(tasks.contains(task1), "Изменяемая задача отсутствует в списке задач");
        assertTrue(tasks.contains(task2), "Неизменяемая задача отсутствует в списке задач");
        assertEquals("Неизменяемая задача", task2.getName(), "Изменена не та задача");
    }

    @Test
    public void shouldUpdateEpic() {
        Epic epic1 = new Epic("Эпик, который будет изменен");
        Epic epic2 = new Epic("Неизменяемый эпик");

        int epic1Id = taskManager.createItem(epic1);
        taskManager.createItem(epic2);

        epic1.setName("Новое имя эпика");
        taskManager.updateItem(epic1, epic1Id);
        Task updatedEpic = taskManager.getItemById(epic1Id);

        assertNotNull(updatedEpic, "Обновленный эпик не существует");
        assertEquals(updatedEpic, epic1, "Эпики не совпадают");
        assertEquals("Новое имя эпика", updatedEpic.getName(), "Эпик не изменен");

        List<Task> tasks = taskManager.getAllItemsByType(ItemType.EPIC);

        assertTrue(tasks.contains(epic1), "Изменяемый эпик отсутствует в списке");
        assertTrue(tasks.contains(epic2), "Неизменяемый эпик отсутствует в списке");
        assertEquals("Неизменяемый эпик", epic2.getName(), "Изменен не тот эпик");
    }

    @Test
    public void shouldUpdateSubtask() {
        Subtask subtask1 = new Subtask("Подзадача, которая будет изменена");
        Subtask subtask2 = new Subtask("Неизменяемая задача");

        int subtask1Id = taskManager.createItem(subtask1);
        taskManager.createItem(subtask2);

        subtask1.setName("Новое имя подзадачи");
        taskManager.updateItem(subtask1, subtask1Id);
        Task updatedSubtask = taskManager.getItemById(subtask1Id);

        assertNotNull(updatedSubtask, "Обновленная подзадача не существует");
        assertEquals(updatedSubtask, subtask1, "Подзадачи не совпадают");
        assertEquals("Новое имя подзадачи", updatedSubtask.getName(), "Подзадача не изменена");

        List<Task> tasks = taskManager.getAllItemsByType(ItemType.SUBTASK);

        assertTrue(tasks.contains(subtask1), "Изменяемая подзадача отсутствует в списке");
        assertTrue(tasks.contains(subtask2), "Неизменяемая подзадача отсутствует в списке");
        assertEquals("Неизменяемая задача", subtask2.getName(), "Изменена не та подзадача");
    }

    @Test
    public void shouldNotUpdateNullTask() {
        taskManager.createItem(new Task("Первая задача"));
        Task task = null;

        assertThrows(NullPointerException.class,
                () -> taskManager.updateItem(task, 1),
                "Обновлена пустая задача");
    }

    @Test
    public void shouldNotUpdateTaskWithNotExistingId() {

        assertThrows(NoSuchTaskExists.class,
                () -> taskManager.updateItem(new Task("Новая задача"), 1),
                "Обновлена задача с несуществующим Id");
    }

    @Test
    public void shouldUpdateEpicStatus() {
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        subtask1.setStatus(Status.NEW);

        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        subtask2.setStatus(Status.NEW);

        Subtask subtask3 = new Subtask("subtask3");
        taskManager.createItem(subtask3);
        subtask3.setStatus(Status.NEW);

        Epic epic = new Epic("epic");
        taskManager.createItem(epic);

        //[1:'NEW']
        ((InMemoryTaskManager) taskManager).addSubtask(subtask1, epic);
        assertEquals(Status.NEW, epic.getStatus(), "Неверный статус эпика");

        //[1: 'IN_PROGRESS']
        subtask1.setStatus(Status.IN_PROGRESS);
        taskManager.updateItem(subtask1, subtask1.getId());
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Неверный статус эпика");

        //[1: 'IN_PROGRESS', 2: 'NEW']
        ((InMemoryTaskManager) taskManager).addSubtask(subtask2, epic);
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Неверный статус эпика");

        //[1: 'NEW', 2: 'NEW']
        subtask1.setStatus(Status.NEW);
        taskManager.updateItem(subtask1, subtask1.getId());
        assertEquals(Status.NEW, epic.getStatus(), "Неверный статус эпика");

        //[1: 'DONE', 2: 'NEW']
        subtask1.setStatus(Status.DONE);
        taskManager.updateItem(subtask1, subtask1.getId());
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Неверный статус эпика");

        //[1: 'DONE', 2: 'DONE']
        subtask2.setStatus(Status.DONE);
        taskManager.updateItem(subtask2, subtask2.getId());
        assertEquals(Status.DONE, epic.getStatus(), "Неверный статус эпика");

        //[1: 'DONE', 2: 'DONE', 3: 'NEW']
        ((InMemoryTaskManager) taskManager).addSubtask(subtask3, epic);
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Неверный статус эпика");

        ////[1: 'DONE', 2: 'DONE']
        taskManager.removeItemById(subtask3.getId());
        assertEquals(Status.DONE, epic.getStatus(), "Неверный статус эпика");
    }

    //Удаление задач
    @Test
    public void shouldRemoveItemById() {
        Task task = new Task("task");
        taskManager.createItem(task);
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        Epic epicWithSubtasks = new Epic("epicWithSubtasks");
        taskManager.createItem(epicWithSubtasks);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask1, epicWithSubtasks);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask2, epicWithSubtasks);
        Epic emptyEpic = new Epic("emptyEpic");
        taskManager.createItem(emptyEpic);

        taskManager.removeItemById(task.getId());
        List<Task> allTasks = ((InMemoryTaskManager) taskManager).getAllItemsOfAllTypes();
        assertFalse(allTasks.contains(task), "Задача не удалена");

        taskManager.removeItemById(subtask2.getId());
        allTasks = ((InMemoryTaskManager) taskManager).getAllItemsOfAllTypes();
        assertFalse(allTasks.contains(subtask2), "Задача не удалена");

        taskManager.removeItemById(emptyEpic.getId());
        allTasks = ((InMemoryTaskManager) taskManager).getAllItemsOfAllTypes();
        assertFalse(allTasks.contains(emptyEpic), "Задача не удалена");

        taskManager.removeItemById(epicWithSubtasks.getId());
        allTasks = ((InMemoryTaskManager) taskManager).getAllItemsOfAllTypes();
        assertFalse(allTasks.contains(epicWithSubtasks), "Задача не удалена");
        assertFalse(allTasks.contains(subtask1), "Задача не удалена");
    }

    @Test
    public void shouldNotRemoveIfIdNotExist() {
        taskManager.createItem(new Task("task"));

        assertThrows(NoSuchTaskExists.class,
                () -> taskManager.removeItemById(2),
                "Допуск удаления задачи с некорректным id");
    }

    @Test
    public void shouldRemoveAllItemsByType() {
        Task task = new Task("task");
        taskManager.createItem(task);
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        Epic epicWithSubtasks = new Epic("epicWithSubtasks");
        taskManager.createItem(epicWithSubtasks);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask1, epicWithSubtasks);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask2, epicWithSubtasks);
        Epic emptyEpic = new Epic("emptyEpic");
        taskManager.createItem(emptyEpic);

        //удаление всех Task
        taskManager.removeAllItemsByType(ItemType.TASK);
        assertEquals(0,
                taskManager.getAllItemsByType(ItemType.TASK).size(),
                "Не все задачи удалены");

        //удаление всех Epic
        taskManager.removeAllItemsByType(ItemType.EPIC);
        assertEquals(0,
                taskManager.getAllItemsByType(ItemType.EPIC).size(),
                "Не все эпики удалены");
        assertEquals(0,
                taskManager.getAllItemsByType(ItemType.SUBTASK).size(),
                "Не все подзадачи эпика удалены");

        //удаление всех Subtask
        taskManager.createItem(subtask1);
        taskManager.createItem(epicWithSubtasks);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask1, epicWithSubtasks);
        taskManager.removeAllItemsByType(ItemType.SUBTASK);

        assertEquals(0,
                taskManager.getAllItemsByType(ItemType.SUBTASK).size(),
                "Не все подзадачи эпика удалены");
        assertEquals(1,
                taskManager.getAllItemsByType(ItemType.EPIC).size(),
                "Удалены лишние задачи");
    }

    //Тесты расчета startTime для Epic
    @Test
    public void shouldSetMinOfEpicSubtasksStartTime() {
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);

        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask1, epic);

        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask2, epic);

        //Расчет при создании задачи
        Subtask subtask3 = new Subtask("subtask3");
        subtask3.setStartTime(LocalDateTime.parse("02-01-2023 12:00", formatter));
        subtask3.setEpicId(epic.getId());
        taskManager.createItem(subtask3);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask3, epic);

        assertEquals(LocalDateTime.of(2023, 1, 2, 12, 0),
                epic.getStartTime(),
                "startTime неверно для Epic");

        //Расчет при обновлении задач
        subtask1.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        taskManager.updateItem(subtask1, subtask1.getId());

        subtask2.setStartTime(LocalDateTime.parse("03-02-2023 16:20", formatter));
        taskManager.updateItem(subtask2, subtask2.getId());

        ((InMemoryTaskManager) taskManager).updateEpicStartTimeDurationEndTime(epic.getId());

        assertEquals(LocalDateTime.of(2023, 1, 1, 12, 0),
                epic.getStartTime(),
                "startTime неверно для Epic");

        //Расчет при удалении задач
        taskManager.removeItemById(subtask1.getId());

        assertEquals(LocalDateTime.of(2023, 1, 2, 12, 0),
                epic.getStartTime(),
                "startTime неверно для Epic");

    }

    @Test
    public void shouldReturnNullIfEpicSubtasksStartTimeIsNull() {
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);

        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask1, epic);

        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask2, epic);

        ((InMemoryTaskManager) taskManager).updateEpicStartTimeDurationEndTime(epic.getId());

        Assertions.assertNull(epic.getStartTime(),
                "startTime не null для Epic");
    }

    //Тесты расчета duration для Epic
    @Test
    public void shouldSetSumOfEpicSubtasksDuration() {
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);

        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask1, epic);

        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask2, epic);

        //Расчет при создании
        Subtask subtask3 = new Subtask("subtask3");
        subtask3.setDurationMinutes(Duration.of(10, ChronoUnit.MINUTES));
        subtask3.setEpicId(epic.getId());
        taskManager.createItem(subtask3);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask3, epic);

        Assertions.assertEquals(Duration.of(10, ChronoUnit.MINUTES),
                epic.getDurationMinutes(),
                "Неверно расcчитано duration для Epic");

        //Расчет при обновлении
        subtask1.setDurationMinutes(Duration.of(30, ChronoUnit.MINUTES));
        taskManager.updateItem(subtask1, subtask1.getId());

        subtask2.setDurationMinutes(Duration.of(120, ChronoUnit.MINUTES));
        taskManager.updateItem(subtask2, subtask2.getId());

        ((InMemoryTaskManager) taskManager).updateEpicStartTimeDurationEndTime(epic.getId());

        Assertions.assertEquals(Duration.of(160, ChronoUnit.MINUTES),
                epic.getDurationMinutes(),
                "Неверно расcчитано duration для Epic");

        //Расчет при удалении
        taskManager.removeItemById(subtask2.getId());

        Assertions.assertEquals(Duration.of(40, ChronoUnit.MINUTES),
                epic.getDurationMinutes(),
                "Неверно расcчитано duration для Epic");
    }

    @Test
    public void shouldReturnZeroIfEpicSubtasksDurationIsNull() {
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);

        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask1, epic);

        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask2, epic);

        ((InMemoryTaskManager) taskManager).updateEpicStartTimeDurationEndTime(epic.getId());

        Assertions.assertEquals(0,
                epic.getDurationMinutes().toMinutes(),
                "duration не равно 0");
    }

    //Тесты для расчета endTime для Epic
    @Test
    public void shouldSetCorrectEndTimeForEpic() {
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);

        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask1, epic);

        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask2, epic);

        Subtask subtask3WithOutStartDate = new Subtask("subtask3WithOutStartDate");
        taskManager.createItem(subtask3WithOutStartDate);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask3WithOutStartDate, epic);

        //Расчет при создании
        Subtask subtask4 = new Subtask("subtask4");
        subtask4.setStartTime(LocalDateTime.parse("02-01-2023 12:00", formatter));
        subtask4.setDurationMinutes(Duration.of(10, ChronoUnit.MINUTES));
        subtask4.setEpicId(epic.getId());
        taskManager.createItem(subtask4);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask4, epic);

        Assertions.assertEquals(LocalDateTime.parse("02-01-2023 12:10", formatter),
                epic.getEndTime().get(),
                "Неверно расcчитан endTime для Epic");

        //Расчет при обновлении
        subtask1.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        subtask1.setDurationMinutes(Duration.of(30, ChronoUnit.MINUTES));
        taskManager.updateItem(subtask1, subtask1.getId());

        subtask2.setStartTime(LocalDateTime.parse("03-02-2023 16:20", formatter));
        subtask2.setDurationMinutes(Duration.of(120, ChronoUnit.MINUTES));
        taskManager.updateItem(subtask2, subtask2.getId());

        ((InMemoryTaskManager) taskManager).updateEpicStartTimeDurationEndTime(epic.getId());

        Assertions.assertEquals(LocalDateTime.parse("03-02-2023 18:20", formatter),
                epic.getEndTime().get(),
                "Неверно расcчитан endTime для Epic");

        //Расчет при удалении
        taskManager.removeItemById(subtask2.getId());
        Assertions.assertEquals(LocalDateTime.parse("02-01-2023 12:10", formatter),
                epic.getEndTime().get(),
                "Неверно расcчитан endTime для Epic");
    }

    @Test
    public void shouldSetCorrectEntTimeIfDurationIsAbsentInSubtask() {
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);

        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask1, epic);

        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask2, epic);

        Subtask subtask3WithOutStartDate = new Subtask("subtask3WithOutStartDate");
        taskManager.createItem(subtask3WithOutStartDate);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask3WithOutStartDate, epic);

        subtask1.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        subtask1.setDurationMinutes(Duration.of(30, ChronoUnit.MINUTES));
        taskManager.updateItem(subtask1, subtask1.getId());

        subtask2.setStartTime(LocalDateTime.parse("03-02-2023 16:20", formatter));

        ((InMemoryTaskManager) taskManager).updateEpicStartTimeDurationEndTime(epic.getId());

        Assertions.assertEquals(LocalDateTime.parse("01-01-2023 12:30", formatter),
                epic.getEndTime().get(),
                "Неверно расcчитан endTime для Epic");
    }

    @Test
    public void shouldSetCorrectEntTimeIfStartTimeIsAbsentInSubtask() {
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);

        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask1, epic);

        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask2, epic);

        Subtask subtask3WithOutStartDate = new Subtask("subtask3WithOutStartDate");
        taskManager.createItem(subtask3WithOutStartDate);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask3WithOutStartDate, epic);

        subtask1.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        subtask1.setDurationMinutes(Duration.of(30, ChronoUnit.MINUTES));
        taskManager.updateItem(subtask1, subtask1.getId());

        subtask2.setDurationMinutes(Duration.of(120, ChronoUnit.MINUTES));
        taskManager.updateItem(subtask2, subtask2.getId());

        ((InMemoryTaskManager) taskManager).updateEpicStartTimeDurationEndTime(epic.getId());

        Assertions.assertEquals(LocalDateTime.parse("01-01-2023 12:30", formatter),
                epic.getEndTime().get(),
                "Неверно расcчитан endTime для Epic");
    }

    //Тесты приоритизации задач
    @Test
    public void shouldPrioritizeByStartTimeWhenCreate() {
        //Эпик не попадает в список
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);

        //Подзадачи и проверка сортировки
        Subtask subtask1 = new Subtask("subtask1");
        subtask1.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        taskManager.createItem(subtask1);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask1, epic);

        Subtask subtask2 = new Subtask("subtask2");
        subtask2.setStartTime(LocalDateTime.parse("03-02-2023 16:20", formatter));
        taskManager.createItem(subtask2);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask2, epic);

        //null сравнивается с задачей
        Subtask subtask3WithOutStartDate = new Subtask("subtask3WithOutStartDate");
        taskManager.createItem(subtask3WithOutStartDate);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask3WithOutStartDate, epic);

        //Задача сравнивается с null
        Task task = new Task("task");
        task.setStartTime(LocalDateTime.parse("05-02-2023 12:20", formatter));
        taskManager.createItem(task);

        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        Assertions.assertEquals(subtask1, prioritizedTasks.get(0), "Ошибка сортировки задач");
        Assertions.assertEquals(subtask2, prioritizedTasks.get(1), "Ошибка сортировки задач");
        Assertions.assertEquals(task, prioritizedTasks.get(2), "Ошибка сортировки задач");
        Assertions.assertEquals(subtask3WithOutStartDate, prioritizedTasks.get(3), "Ошибка сортировки задач");
    }

    @Test
    public void shouldPrioritizeByStartTimeWhenUpdate() {
        Task task = new Task("task");
        task.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        taskManager.createItem(task);
        Subtask subtask = new Subtask("subtask");
        subtask.setStartTime(LocalDateTime.parse("02-01-2023 12:00", formatter));
        taskManager.createItem(subtask);

        //Обновление задачи
        Task updatedTask = new Task("task");
        updatedTask.setStartTime(LocalDateTime.parse("03-01-2023 12:00", formatter));
        updatedTask.setId(task.getId());
        taskManager.updateItem(updatedTask, updatedTask.getId());

        assertEquals(taskManager.getPrioritizedTasks().get(0), subtask, "Ошибка приоритезации");
        assertEquals(taskManager.getPrioritizedTasks().get(1), updatedTask, "Ошибка приоритезации");

        //Обновление подзадачи
        Subtask updatedSubtask = new Subtask("subtask");
        updatedSubtask.setStartTime(LocalDateTime.parse("04-01-2023 12:00", formatter));
        updatedSubtask.setId(subtask.getId());
        taskManager.updateItem(updatedSubtask, updatedSubtask.getId());

        assertEquals(taskManager.getPrioritizedTasks().get(0), updatedTask, "Ошибка приоритезации");
        assertEquals(taskManager.getPrioritizedTasks().get(1), updatedSubtask, "Ошибка приоритезации");
    }

    @Test
    public void shouldPrioritizeByStartTimeWhenRemove() {
        Task task = new Task("task");
        task.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        taskManager.createItem(task);
        Subtask subtask = new Subtask("subtask");
        subtask.setStartTime(LocalDateTime.parse("02-01-2023 12:00", formatter));
        taskManager.createItem(subtask);

        taskManager.removeItemById(task.getId());

        assertEquals(taskManager.getPrioritizedTasks().get(0), subtask, "Ошибка приоритезации");
    }

    //Тесты проверки пересечения задач
    @Test
    public void shouldThrowExceptionIfStartTimeIntersectOtherTask() {
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);

        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask1, epic);

        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask2, epic);

        subtask1.setStartTime(LocalDateTime.parse("01-01-2023 11:30", formatter));
        subtask1.setDurationMinutes(Duration.of(30, ChronoUnit.MINUTES));
        taskManager.updateItem(subtask1, subtask1.getId());

        //Время начала пересекается с первой
        subtask2.setStartTime(LocalDateTime.parse("01-01-2023 11:40", formatter));
        subtask2.setDurationMinutes(Duration.of(120, ChronoUnit.MINUTES));

        Assertions.assertThrows(TaskTimeIntersectionException.class,
                () -> taskManager.updateItem(subtask2, subtask2.getId()),
                "Задачи пересекаются");
    }

    @Test
    public void shouldThrowExceptionIfEndTimeIntersectOtherTask() {
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);

        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask1, epic);

        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask2, epic);

        subtask1.setStartTime(LocalDateTime.parse("01-01-2023 11:20", formatter));
        subtask1.setDurationMinutes(Duration.of(30, ChronoUnit.MINUTES));
        taskManager.updateItem(subtask1, subtask1.getId());

        //Время конца пересекается с первой
        subtask2.setStartTime(LocalDateTime.parse("01-01-2023 11:10", formatter));
        subtask2.setDurationMinutes(Duration.of(30, ChronoUnit.MINUTES));

        Assertions.assertThrows(TaskTimeIntersectionException.class,
                () -> taskManager.updateItem(subtask2, subtask2.getId()),
                "Задачи пересекаются");
    }

    @Test
    public void shouldThrowExceptionIfStartAndEndTimeIntersectOtherTask() {
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);

        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask1, epic);

        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        ((InMemoryTaskManager) taskManager).addSubtask(subtask2, epic);

        subtask1.setStartTime(LocalDateTime.parse("01-01-2023 11:50", formatter));
        subtask1.setDurationMinutes(Duration.of(120, ChronoUnit.MINUTES));
        taskManager.updateItem(subtask1, subtask1.getId());

        //Полностью пересекается с первой
        subtask2.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        subtask2.setDurationMinutes(Duration.of(30, ChronoUnit.MINUTES));

        Assertions.assertThrows(TaskTimeIntersectionException.class,
                () -> taskManager.updateItem(subtask2, subtask2.getId()),
                "Задачи пересекаются");
    }

}
