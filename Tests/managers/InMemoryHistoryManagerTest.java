package managers;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class InMemoryHistoryManagerTest {

    static InMemoryTaskManager taskManager;
    static HistoryManager historyManager;
    static Task task1;
    static Task task2;
    static Subtask subtask1;
    static Subtask subtask2;
    static Epic epic1;
    static Epic epic2;

    @BeforeEach
    public void beforeEach() {
        taskManager = (InMemoryTaskManager) Managers.getDefault();
        historyManager = taskManager.getHistoryManager();

        task1 = new Task("task1");
        task2 = new Task("task2");
        subtask1 = new Subtask("subtask1");
        subtask2 = new Subtask("subtask2");
        epic1 = new Epic("epic1");
        epic2 = new Epic("epic2");

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
    public void shouldAddNewRecordsToHistory() {
        //Первая уникальная запись
        taskManager.getItemById(task1.getId()); //[0]
        assertEquals(task1, historyManager.getHistory().get(0));

        //Вторая уникальная запись
        taskManager.getItemById(task2.getId());//[1]
        assertEquals(task1, historyManager.getHistory().get(0));
        assertEquals(task2, historyManager.getHistory().get(1));

        taskManager.getItemById(subtask1.getId());//[2]
        taskManager.getItemById(subtask2.getId());//[3]
        taskManager.getItemById(epic2.getId());//[4]
        taskManager.getItemById(epic1.getId());//[5]

        //Неуникальная запись
        taskManager.getItemById(task2.getId());//[1] -> [5]
        assertNotEquals(task2, historyManager.getHistory().get(0));
        assertEquals(task2, historyManager.getHistory().get(5));
    }

    @Test
    public void shouldRemoveRecordsFromHistoryIdItemIsRemoved() {
        taskManager.getItemById(task1.getId()); //[0]

        //Удаление единственной записи
        taskManager.removeItemById(task1.getId());
        assertEquals(0, historyManager.getHistory().size());

        taskManager.getItemById(task2.getId());//[0]
        taskManager.getItemById(subtask1.getId());//[1]
        taskManager.getItemById(subtask2.getId());//[2]
        taskManager.getItemById(epic2.getId());//[3]
        taskManager.getItemById(epic1.getId());//[4]

        //Удаление записи из head списка
        taskManager.removeItemById(task2.getId());
        assertEquals(subtask1, historyManager.getHistory().get(0));
        assertEquals(epic1, historyManager.getHistory().get(3));

        //Удаление записи из tail списка
        taskManager.removeItemById(epic1.getId());
        assertEquals(epic2, historyManager.getHistory().get(2));

        //Удаление записи из середины списка
        taskManager.removeItemById(subtask2.getId());
        assertEquals(subtask1, historyManager.getHistory().get(0));
        assertEquals(epic2, historyManager.getHistory().get(1));
    }

}
