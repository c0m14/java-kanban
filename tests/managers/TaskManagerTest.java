package managers;

import exceptions.NoSuchTaskExists;
import exceptions.TaskTimeIntersectionException;
import model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

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
    public void shouldCreateFirstNewTask() {
        //Подготовка данных
        Task testTask1 = new Task("Test_task1");

        //Тестируемые операции
        taskManager.createItem(testTask1);

        //Проверяем ожидаемое количество элементов
        List<Task> createdTasks = taskManager.getAllItemsByType(ItemType.TASK);
        assertNotNull(createdTasks, "Список задач пуст");
        assertEquals(1, createdTasks.size(), "Неверное количество элементов в списке задач");
        assertTrue(createdTasks.contains(testTask1), "Задача не найдена");
        //Проверяем, что создана нужная задача
        Task savedTask = taskManager.getItemById(testTask1.getId());
        assertNotNull(savedTask, "Задача не найдена");
        assertEquals(testTask1, savedTask, "Задачи не совпадают");
    }

    @Test
    public void shouldCreateSecondPlusNewTasks() {
        //Подготовка данных
        Task testTask1 = new Task("Test_task1");
        Task testTask2 = new Task("Test_task2");

        //Тестируемые операции
        taskManager.createItem(testTask1);
        taskManager.createItem(testTask2);

        //Проверяем ожидаемое количество элементов
        List<Task> createdTasks = taskManager.getAllItemsByType(ItemType.TASK);
        assertNotNull(createdTasks, "Список задач пуст");
        assertEquals(2, createdTasks.size(), "Неверное количество элементов в списке задач");
        assertTrue(createdTasks.contains(testTask2), "Задача не найдена");
        assertTrue(createdTasks.contains(testTask1), "Задача не найдена");
        //Проверяем, что создана нужная задача
        Task savedTask = taskManager.getItemById(testTask2.getId());
        assertNotNull(savedTask, "Задача не найдена");
        assertEquals(testTask2, savedTask, "Задачи не совпадают");
    }

    @Test
    public void shouldCreateFirstNewSubtask() {
        //Подготовка данных
        Subtask testSubtask1 = new Subtask("Test_subtask1");

        //Тестируемые операции
        taskManager.createItem(testSubtask1);

        //Проверяем ожидаемое количество элементов
        List<Task> createdSubtasks = taskManager.getAllItemsByType(ItemType.SUBTASK);
        assertNotNull(createdSubtasks, "Список подзадач пуст");
        assertEquals(1, createdSubtasks.size(), "Неверное количество элементов в списке подзадач");
        assertTrue(createdSubtasks.contains(testSubtask1), "Подзадача не найдена");
        //Проверяем, что создана нужная задача
        Subtask savedSubtask = (Subtask) ((InMemoryTaskManager) taskManager)
                .getItemByIdWithoutSavingHistory(testSubtask1.getId());
        assertNotNull(savedSubtask, "Подзадача не найдена");
        assertEquals(testSubtask1, savedSubtask, "Подзадачи не совпадают");
    }

    @Test
    public void shouldCreateSecondPlusNewSubtasks() {
        //Подготовка данных
        Subtask testSubtask1 = new Subtask("Test_subtask1");
        Subtask testSubtask2 = new Subtask("Test_subtask2");

        //Тестируемые операции
        taskManager.createItem(testSubtask1);
        taskManager.createItem(testSubtask2);

        //Проверяем ожидаемое количество элементов
        List<Task> createdSubtasks = taskManager.getAllItemsByType(ItemType.SUBTASK);
        assertNotNull(createdSubtasks, "Список подзадач пуст");
        assertEquals(2, createdSubtasks.size(), "Неверное количество элементов в списке подзадач");
        assertTrue(createdSubtasks.contains(testSubtask2), "Подзадача не найдена");
        assertTrue(createdSubtasks.contains(testSubtask1), "Подзадача не найдена");
        //Проверяем, что создана нужная задача
        Subtask savedSubtask = (Subtask) ((InMemoryTaskManager) taskManager)
                .getItemByIdWithoutSavingHistory(testSubtask2.getId());
        assertNotNull(savedSubtask, "Подзадача не найдена");
        assertEquals(testSubtask2, savedSubtask, "Подзадачи не совпадают");
    }

    @Test
    public void shouldCreateFirstNewEpic() {
        //Подготовка данных
        Epic testEpic1 = new Epic("Test_epic1");

        //Тестируемые операции
        taskManager.createItem(testEpic1);

        //Проверяем ожидаемое количество элементов
        List<Task> createdEpics = taskManager.getAllItemsByType(ItemType.EPIC);
        assertNotNull(createdEpics, "Список эпиков пуст");
        assertEquals(1, createdEpics.size(), "Неверное количество элементов в списке эпиков");
        assertTrue(createdEpics.contains(testEpic1), "Эпик не найден");
        //Проверяем, что создана нужная задача
        Epic savedEpic = (Epic) ((InMemoryTaskManager) taskManager).getItemByIdWithoutSavingHistory(testEpic1.getId());
        assertNotNull(savedEpic, "Эпик не найден");
        assertEquals(testEpic1, savedEpic, "Эпики не совпадают");
    }

    @Test
    public void shouldCreateSecondPlusEpic() {
        //Подготовка данных
        Epic testEpic1 = new Epic("Test_epic1");
        Epic testEpic2 = new Epic("Test_epic2");

        //Тестируемые операции
        taskManager.createItem(testEpic1);
        taskManager.createItem(testEpic2);

        //Проверяем ожидаемое количество элементов
        List<Task> createdEpics = taskManager.getAllItemsByType(ItemType.EPIC);
        assertNotNull(createdEpics, "Список эпиков пуст");
        assertEquals(2, createdEpics.size(), "Неверное количество элементов в списке эпиков");
        assertTrue(createdEpics.contains(testEpic2), "Эпик не найден");
        assertTrue(createdEpics.contains(testEpic1), "Эпик не найден");

        //Проверяем, что создана нужная задача
        Epic savedEpic = (Epic) ((InMemoryTaskManager) taskManager).getItemByIdWithoutSavingHistory(testEpic2.getId());
        assertNotNull(savedEpic, "Эпик не найден");
        assertEquals(testEpic2, savedEpic, "Эпики не совпадают");
    }

    @Test
    public void shouldIncrementItemCounterAfterCreatingFirstTask() {
        //Подготовка данных
        Task testTask1 = new Task("Test_task1");

        //Тестируемые операции
        taskManager.createItem(testTask1);

        //Проверка изменения счетчика
        assertEquals(2, taskManager.getIdCounter(), "Неверный id задачи");
    }

    @Test
    public void shouldIncrementItemCounterAfterCreatingSecondPlusTask() {
        //Подготовка данных
        Task testTask1 = new Task("Test_task1");
        Task testTask2 = new Task("Test_task2");

        //Тестируемые операции
        taskManager.createItem(testTask1);
        taskManager.createItem(testTask2);

        //Проверка изменения счетчика
        assertEquals(3, taskManager.getIdCounter(), "Неверный id задачи");
    }

    @Test
    public void shouldNotCreateTaskOfNull() {
        Task task = null;

        Executable executable = () -> taskManager.createItem(task);

        assertThrows(NullPointerException.class,
                executable,
                "Задача создана из null");
    }

    //Получение списка задач
    @Test
    public void shouldReturnEmptyListIfTasksDoNotExist() {

        Executable executable = () -> taskManager.getAllItemsByType(ItemType.TASK);

        assertThrows(NullPointerException.class,
                executable,
                "Список не пустой");
    }

    @Test
    public void shouldReturnEmptyListIfSubtasksDoNotExist() {

        Executable executable = () -> taskManager.getAllItemsByType(ItemType.SUBTASK);

        assertThrows(NullPointerException.class,
                executable,
                "Список не пустой");
    }

    @Test
    public void shouldReturnEmptyListIfEpicsDoNotExist() {

        Executable executable = () -> taskManager.getAllItemsByType(ItemType.EPIC);

        assertThrows(NullPointerException.class,
                executable,
                "Список не пустой");
    }

    //Обновление задач
    @Test
    public void shouldUpdateTask() {
        //Подготовка данных
        Task task1 = new Task("Задача, которая будет изменена");
        Task task2 = new Task("Неизменяемая задача");
        taskManager.createItem(task1);
        taskManager.createItem(task2);
        task1.setName("Новое имя задачи");

        //Тестируемая логика
        taskManager.updateItem(task1, task1.getId());

        //Проверка корректности обновления задачи
        Task updatedTask = taskManager.getItemById(task1.getId());
        assertNotNull(updatedTask, "Обновленная задача не существует");
        assertEquals(updatedTask, task1, "Задачи не совпадают");
        assertEquals("Новое имя задачи", updatedTask.getName(), "Задача не изменена");

        //Проверка консистентности данных
        List<Task> tasks = taskManager.getAllItemsByType(ItemType.TASK);
        assertTrue(tasks.contains(task1), "Изменяемая задача отсутствует в списке задач");
        assertTrue(tasks.contains(task2), "Неизменяемая задача отсутствует в списке задач");
        assertEquals("Неизменяемая задача", task2.getName(), "Изменена не та задача");
    }

    @Test
    public void shouldUpdateEpic() {
        //Подготовка данных
        Epic epic1 = new Epic("Эпик, который будет изменен");
        Epic epic2 = new Epic("Неизменяемый эпик");
        taskManager.createItem(epic1);
        taskManager.createItem(epic2);
        epic1.setName("Новое имя эпика");

        //Тестируемая логика
        taskManager.updateItem(epic1, epic1.getId());

        //Проверка корректности обновления задачи
        Task updatedEpic = ((InMemoryTaskManager) taskManager).getItemByIdWithoutSavingHistory(epic1.getId());
        assertNotNull(updatedEpic, "Обновленный эпик не существует");
        assertEquals(updatedEpic, epic1, "Эпики не совпадают");
        assertEquals("Новое имя эпика", updatedEpic.getName(), "Эпик не изменен");

        //Проверка консистентности данных
        List<Task> tasks = taskManager.getAllItemsByType(ItemType.EPIC);
        assertTrue(tasks.contains(epic1), "Изменяемый эпик отсутствует в списке");
        assertTrue(tasks.contains(epic2), "Неизменяемый эпик отсутствует в списке");
        assertEquals("Неизменяемый эпик", epic2.getName(), "Изменен не тот эпик");
    }

    @Test
    public void shouldUpdateSubtask() {
        //Подготовка данных
        Subtask subtask1 = new Subtask("Подзадача, которая будет изменена");
        Subtask subtask2 = new Subtask("Неизменяемая задача");
        taskManager.createItem(subtask1);
        taskManager.createItem(subtask2);
        subtask1.setName("Новое имя подзадачи");

        //Проверяемая логика
        taskManager.updateItem(subtask1, subtask1.getId());

        //Проверка корректности обновления задачи
        Task updatedSubtask = ((InMemoryTaskManager) taskManager).getItemByIdWithoutSavingHistory(subtask1.getId());
        assertNotNull(updatedSubtask, "Обновленная подзадача не существует");
        assertEquals(updatedSubtask, subtask1, "Подзадачи не совпадают");
        assertEquals("Новое имя подзадачи", updatedSubtask.getName(), "Подзадача не изменена");

        //Проверка консистентности данных
        List<Task> tasks = taskManager.getAllItemsByType(ItemType.SUBTASK);
        assertTrue(tasks.contains(subtask1), "Изменяемая подзадача отсутствует в списке");
        assertTrue(tasks.contains(subtask2), "Неизменяемая подзадача отсутствует в списке");
        assertEquals("Неизменяемая задача", subtask2.getName(), "Изменена не та подзадача");
    }

    @Test
    public void shouldNotUpdateNullTask() {
        taskManager.createItem(new Task("Первая задача"));
        Task task = null;

        Executable executable = () -> taskManager.updateItem(task, 1);

        assertThrows(NullPointerException.class,
                executable,
                "Обновлена пустая задача");
    }

    @Test
    public void shouldNotUpdateTaskWithNotExistingId() {

        Executable executable = () -> taskManager.updateItem(new Task("Новая задача"), 1);

        assertThrows(NoSuchTaskExists.class,
                executable,
                "Обновлена задача с несуществующим Id");
    }

    //Удаление задач
    @Test
    public void shouldRemoveTaskById() {
        //Подготовка данных
        Task task = new Task("task");
        taskManager.createItem(task);

        //Тестируемая логика
        taskManager.removeItemById(task.getId());

        //Проверка наличия удаленной задачи
        List<Task> allTasks = ((InMemoryTaskManager) taskManager).getAllItemsOfAllTypes();
        assertFalse(allTasks.contains(task), "Задача не удалена");
    }

    @Test
    public void shouldRemoveSubtaskById() {
        //Подготовка данных
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        Epic epicWithSubtasks = new Epic("epicWithSubtasks");
        taskManager.createItem(epicWithSubtasks);
        ((InMemoryTaskManager) taskManager).linkSubtaskToEpic(subtask1, epicWithSubtasks);

        //Тестируемая логика
        taskManager.removeItemById(subtask1.getId());

        //Проверка наличия удаленной задачи
        List<Task> allTasks = ((InMemoryTaskManager) taskManager).getAllItemsOfAllTypes();
        assertFalse(allTasks.contains(subtask1), "Задача не удалена");
    }

    @Test
    public void shouldRemoveEmptyEpicById() {
        //Подготовка данных
        Epic emptyEpic = new Epic("emptyEpic");
        taskManager.createItem(emptyEpic);

        //Тестируемая логика
        taskManager.removeItemById(emptyEpic.getId());

        //Проверка наличия удаленной задачи
        List<Task> allTasks = ((InMemoryTaskManager) taskManager).getAllItemsOfAllTypes();
        assertFalse(allTasks.contains(emptyEpic), "Задача не удалена");
    }

    @Test
    public void shouldRemoveEmptyEpicWithSubtasksById() {
        //Подготовка данных
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        Epic epicWithSubtasks = new Epic("epicWithSubtasks");
        taskManager.createItem(epicWithSubtasks);
        ((InMemoryTaskManager) taskManager).linkSubtaskToEpic(subtask1, epicWithSubtasks);
        ((InMemoryTaskManager) taskManager).linkSubtaskToEpic(subtask2, epicWithSubtasks);

        //Тестируемая логика
        taskManager.removeItemById(epicWithSubtasks.getId());

        //Проверка наличия удаленной задачи
        List<Task> allTasks = ((InMemoryTaskManager) taskManager).getAllItemsOfAllTypes();
        assertFalse(allTasks.contains(epicWithSubtasks), "Задача не удалена");
        //Проверка наличия привязанных подзадач
        assertFalse(allTasks.contains(subtask1), "Подзадача не удалена");
        assertFalse(allTasks.contains(subtask2), "Подзадача не удалена");
    }

    @Test
    public void shouldNotRemoveIfIdNotExist() {
        //Подготовка данных
        taskManager.createItem(new Task("task"));

        //Тестируемая логика
        Executable executable = () -> taskManager.removeItemById(2);

        //Проверка
        assertThrows(NoSuchTaskExists.class,
                executable,
                "Допуск удаления задачи с некорректным id");
    }

    @Test
    public void shouldRemoveTasksByType() {
        //Подготовка данных
        Task task = new Task("task");
        taskManager.createItem(task);

        //Тестируемая логика
        taskManager.removeAllItemsByType(ItemType.TASK);

        //Проверка наличия задач
        assertEquals(0,
                taskManager.getAllItemsByType(ItemType.TASK).size(),
                "Не все задачи удалены");
    }

    @Test
    public void shouldRemoveSubtasksByType() {
        //Подготовка данных
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);

        //Тестируемая логика
        taskManager.removeAllItemsByType(ItemType.SUBTASK);

        //Проверка наличия задач
        assertEquals(0,
                taskManager.getAllItemsByType(ItemType.SUBTASK).size(),
                "Не все задачи удалены");
    }

    @Test
    public void shouldRemoveEmptyEpicsByType() {
        //Подготовка данных
        Epic emptyEpic = new Epic("emptyEpic");
        taskManager.createItem(emptyEpic);

        //Тестируемая логика
        taskManager.removeAllItemsByType(ItemType.EPIC);

        //Проверка наличия задач
        assertEquals(0,
                taskManager.getAllItemsByType(ItemType.EPIC).size(),
                "Не все задачи удалены");
    }

    @Test
    public void shouldRemoveEpicsWithSubtasksByType() {
        //Подготовка данных
        Epic epicWithSubtasks = new Epic("epicWithSubtasks");
        taskManager.createItem(epicWithSubtasks);
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        ((InMemoryTaskManager) taskManager).linkSubtaskToEpic(subtask1, epicWithSubtasks);

        //Тестируемая логика
        taskManager.removeAllItemsByType(ItemType.EPIC);

        //Проверка наличия задач
        assertEquals(0,
                taskManager.getAllItemsByType(ItemType.EPIC).size(),
                "Не все задачи удалены");
        //Проверка наличия подзадач
        assertEquals(0,
                taskManager.getAllItemsByType(ItemType.SUBTASK).size(),
                "Не все подзадачи удалены");
    }

    //Тесты приоритизации задач
    @Test
    public void shouldNotAddEpicToPrioritizedList() {
        //Подготовка данных
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);

        //Проверка отсутствия эпика
        assertFalse(taskManager.getPrioritizedTasks().contains(epic));
    }

    @Test
    public void shouldSetRightOrderIfTaskStartTimeIsComparedToNull() {
        //Подготовка данных
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        Subtask subtask1 = new Subtask("subtask1");
        subtask1.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        taskManager.createItem(subtask1);
        ((InMemoryTaskManager) taskManager).linkSubtaskToEpic(subtask1, epic);
        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        ((InMemoryTaskManager) taskManager).linkSubtaskToEpic(subtask2, epic);

        //Проверка сортировки
        assertEquals(subtask1, taskManager.getPrioritizedTasks().get(0), "Ошибка сортировки задач");
        assertEquals(subtask2, taskManager.getPrioritizedTasks().get(1), "Ошибка сортировки задач");
    }

    @Test
    public void shouldSetRightOrderIfNullIsComparedToTaskStartTime() {
        //Подготовка данных
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        ((InMemoryTaskManager) taskManager).linkSubtaskToEpic(subtask1, epic);
        Subtask subtask2 = new Subtask("subtask2");
        subtask2.setStartTime(LocalDateTime.parse("03-02-2023 16:20", formatter));
        taskManager.createItem(subtask2);
        ((InMemoryTaskManager) taskManager).linkSubtaskToEpic(subtask2, epic);

        //Проверка сортировки
        assertEquals(subtask2, taskManager.getPrioritizedTasks().get(0), "Ошибка сортировки задач");
        assertEquals(subtask1, taskManager.getPrioritizedTasks().get(1), "Ошибка сортировки задач");
    }

    @Test
    public void shouldSetRightOrderForTasksStartTime() {
        //Подготовка данных
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        Subtask subtask1 = new Subtask("subtask1");
        subtask1.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        taskManager.createItem(subtask1);
        ((InMemoryTaskManager) taskManager).linkSubtaskToEpic(subtask1, epic);
        Task task = new Task("task");
        task.setStartTime(LocalDateTime.parse("05-02-2023 12:20", formatter));
        taskManager.createItem(task);

        //Проверка сортировки
        assertEquals(subtask1, taskManager.getPrioritizedTasks().get(0), "Ошибка сортировки задач");
        assertEquals(task, taskManager.getPrioritizedTasks().get(1), "Ошибка сортировки задач");
    }

    @Test
    public void shouldPrioritizeByStartTimeWhenTaskIsUpdated() {
        //Подготовка данных
        Task task1 = new Task("task");
        task1.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        taskManager.createItem(task1);
        Task task2 = new Task("task");
        task2.setStartTime(LocalDateTime.parse("02-01-2023 12:00", formatter));
        taskManager.createItem(task2);

        //Тестируемая логика
        Task updatedTask = new Task("task");
        updatedTask.setStartTime(LocalDateTime.parse("03-01-2023 12:00", formatter));
        updatedTask.setId(task1.getId());
        taskManager.updateItem(updatedTask, updatedTask.getId());

        //Проверка сортировки
        assertEquals(taskManager.getPrioritizedTasks().get(0), task2, "Ошибка приоритезации");
        assertEquals(taskManager.getPrioritizedTasks().get(1), updatedTask, "Ошибка приоритезации");
    }

    @Test
    public void shouldPrioritizeByStartTimeWhenSubtaskIsUpdated() {
        //Подготовка данных
        Subtask subtask1 = new Subtask("subtask1");
        subtask1.setStartTime(LocalDateTime.parse("02-01-2023 12:00", formatter));
        taskManager.createItem(subtask1);
        Subtask subtask2 = new Subtask("subtask2");
        subtask2.setStartTime(LocalDateTime.parse("03-01-2023 12:00", formatter));
        taskManager.createItem(subtask2);

        //Тестируемая логика
        Subtask updatedSubtask = new Subtask("subtask1");
        updatedSubtask.setStartTime(LocalDateTime.parse("04-01-2023 12:00", formatter));
        updatedSubtask.setId(subtask1.getId());
        taskManager.updateItem(updatedSubtask, updatedSubtask.getId());

        //Проверка сортировки
        assertEquals(taskManager.getPrioritizedTasks().get(0), subtask2, "Ошибка приоритезации");
        assertEquals(taskManager.getPrioritizedTasks().get(1), updatedSubtask, "Ошибка приоритезации");
    }

    @Test
    public void shouldPrioritizeByStartTimeWhenRemove() {
        //Подготовка данных
        Task task = new Task("task");
        task.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        taskManager.createItem(task);
        Subtask subtask = new Subtask("subtask");
        subtask.setStartTime(LocalDateTime.parse("02-01-2023 12:00", formatter));
        taskManager.createItem(subtask);

        //Тестируемая логика
        taskManager.removeItemById(task.getId());

        //Проверка сортировки
        assertEquals(taskManager.getPrioritizedTasks().get(0), subtask, "Ошибка приоритезации");
    }

    //Тесты проверки пересечения задач
    @Test
    public void shouldThrowExceptionIfStartTimeIntersectOtherTask() {
        //Подготовка данных
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        ((InMemoryTaskManager) taskManager).linkSubtaskToEpic(subtask1, epic);
        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        ((InMemoryTaskManager) taskManager).linkSubtaskToEpic(subtask2, epic);
        subtask1.setStartTime(LocalDateTime.parse("01-01-2023 11:30", formatter));
        subtask1.setDurationMinutes(Duration.of(30, ChronoUnit.MINUTES));
        taskManager.updateItem(subtask1, subtask1.getId());
        subtask2.setStartTime(LocalDateTime.parse("01-01-2023 11:40", formatter));
        subtask2.setDurationMinutes(Duration.of(120, ChronoUnit.MINUTES));

        //Проверка срабатывания исключения
        Assertions.assertThrows(TaskTimeIntersectionException.class,
                () -> taskManager.updateItem(subtask2, subtask2.getId()),
                "Задачи пересекаются");
    }

    @Test
    public void shouldThrowExceptionIfEndTimeIntersectOtherTask() {
        //Подготовка данных
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        ((InMemoryTaskManager) taskManager).linkSubtaskToEpic(subtask1, epic);
        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        ((InMemoryTaskManager) taskManager).linkSubtaskToEpic(subtask2, epic);
        subtask1.setStartTime(LocalDateTime.parse("01-01-2023 11:20", formatter));
        subtask1.setDurationMinutes(Duration.of(30, ChronoUnit.MINUTES));
        taskManager.updateItem(subtask1, subtask1.getId());
        subtask2.setStartTime(LocalDateTime.parse("01-01-2023 11:10", formatter));
        subtask2.setDurationMinutes(Duration.of(30, ChronoUnit.MINUTES));

        //Проверка срабатывания исключения
        Assertions.assertThrows(TaskTimeIntersectionException.class,
                () -> taskManager.updateItem(subtask2, subtask2.getId()),
                "Задачи пересекаются");
    }

    @Test
    public void shouldThrowExceptionIfStartAndEndTimeIntersectOtherTask() {
        //Подготовка данных
        Epic epic = new Epic("epic");
        taskManager.createItem(epic);
        Subtask subtask1 = new Subtask("subtask1");
        taskManager.createItem(subtask1);
        ((InMemoryTaskManager) taskManager).linkSubtaskToEpic(subtask1, epic);
        Subtask subtask2 = new Subtask("subtask2");
        taskManager.createItem(subtask2);
        ((InMemoryTaskManager) taskManager).linkSubtaskToEpic(subtask2, epic);
        subtask1.setStartTime(LocalDateTime.parse("01-01-2023 11:50", formatter));
        subtask1.setDurationMinutes(Duration.of(120, ChronoUnit.MINUTES));
        taskManager.updateItem(subtask1, subtask1.getId());
        subtask2.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        subtask2.setDurationMinutes(Duration.of(30, ChronoUnit.MINUTES));

        //Проверка срабатывания исключения
        Assertions.assertThrows(TaskTimeIntersectionException.class,
                () -> taskManager.updateItem(subtask2, subtask2.getId()),
                "Задачи пересекаются");
    }

}
