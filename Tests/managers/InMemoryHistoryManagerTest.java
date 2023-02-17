package managers;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class InMemoryHistoryManagerTest {

    private static InMemoryTaskManager taskManager;
    private static HistoryManager historyManager;
    private static Task task1;
    private static Task task2;
    private static Subtask subtask1;
    private static Subtask subtask2;
    private static Epic epic1;
    private static Epic epic2;

    @BeforeAll
    public static void beforeAll() {
        task1 = new Task("task1");
        task2 = new Task("task2");
        subtask1 = new Subtask("subtask1");
        subtask2 = new Subtask("subtask2");
        epic1 = new Epic("epic1");
        epic2 = new Epic("epic2");
    }

    @BeforeEach
    public void beforeEach() {
        taskManager = (InMemoryTaskManager) Managers.getDefault();
        historyManager = taskManager.getHistoryManager();
        taskManager.createItem(task1);
        taskManager.createItem(task2);
        taskManager.createItem(subtask1);
        taskManager.createItem(subtask2);
        taskManager.createItem(epic1);
        taskManager.createItem(epic2);
    }

    @Test
    public void shouldReturnEmptyListIfNoHistoryRecords() {
        assertEquals(0, historyManager.getHistory().size());
    }

    @Test
    public void shouldAddFirstUniqueRecordToHistory() {
        //Тестируемая логика
        taskManager.getItemById(task1.getId()); //[0]

        //Проверка записи
        assertEquals(task1, historyManager.getHistory().get(0));
    }

    @Test
    public void shouldAddSecondPlusUniqueRecordsToHistory() {
        //Тестируемая логика
        taskManager.getItemById(task1.getId()); //[0]
        taskManager.getItemById(task2.getId()); //[1]

        //Проверка записи
        assertEquals(task1, historyManager.getHistory().get(0));
        assertEquals(task2, historyManager.getHistory().get(1));
    }

    @Test
    public void shouldMoveDublicatedRecordToHistoryEnd() {
        //Подготовка данных
        taskManager.getItemById(task1.getId()); //[0]
        taskManager.getItemById(task2.getId()); //[1]
        taskManager.getItemById(subtask1.getId()); //[2]
        taskManager.getItemById(subtask2.getId()); //[3]
        taskManager.getItemById(epic2.getId()); //[4]
        taskManager.getItemById(epic1.getId()); //[5]

        //Тестируемая логика
        taskManager.getItemById(task2.getId());//[1] -> [5]

        //Проверка записи
        assertNotEquals(task2, historyManager.getHistory().get(0));
        assertEquals(task2, historyManager.getHistory().get(5));
    }

    @Test
    public void shouldRemoveOnlyRecordFromHistory() {
        //Подготовка данных
        taskManager.getItemById(task1.getId()); //[0]

        //Тестируемая логика
        taskManager.removeItemById(task1.getId());

        //Проверка удаления
        assertEquals(0, historyManager.getHistory().size());
    }

    @Test
    public void shouldRemoveHeadRecordFromHistory() {
        //Подготовка данных
        taskManager.getItemById(task2.getId());//[0]
        taskManager.getItemById(subtask1.getId());//[1]
        taskManager.getItemById(subtask2.getId());//[2]

        //Тестируемая логика
        taskManager.removeItemById(task2.getId());

        //Проверка удаления
        assertEquals(subtask1, historyManager.getHistory().get(0));
        assertEquals(subtask2, historyManager.getHistory().get(1));
    }

    @Test
    public void shouldRemoveTailRecordFromHistory() {
        //Подготовка данных
        taskManager.getItemById(task2.getId());//[0]
        taskManager.getItemById(subtask1.getId());//[1]
        taskManager.getItemById(subtask2.getId());//[2]

        //Тестируемая логика
        taskManager.removeItemById(subtask2.getId());

        //Проверка удаления
        assertEquals(task2, historyManager.getHistory().get(0));
        assertEquals(subtask1, historyManager.getHistory().get(1));
    }

    @Test
    public void shouldRemoveRecordInTheMiddleFromHistory() {
        //Подготовка данных
        taskManager.getItemById(task2.getId());//[0]
        taskManager.getItemById(subtask1.getId());//[1]
        taskManager.getItemById(subtask2.getId());//[2]

        //Тестируемая логика
        taskManager.removeItemById(subtask1.getId());

        //Проверка удаления
        assertEquals(task2, historyManager.getHistory().get(0));
        assertEquals(subtask2, historyManager.getHistory().get(1));
    }
}
