import managers.HistoryManager;
import managers.InMemoryTaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

public class Main {

    public static void main(String[] args) {
        InMemoryTaskManager manager = new InMemoryTaskManager();

        //тест кейс: создание задач
        //model.Task
        System.out.println("Добавление model.Task");
        Task testTask1 = new Task(manager.getIdCounter(),
                "Test task 1 with description",
                "Some description of the test task 1");
        int id = manager.createItem(testTask1);

        Task testTask2 = new Task(manager.getIdCounter(),
                "Test task 2 without description");
        manager.createItem(testTask2);

        for (Object task : manager.getAllItemsByType("model.Task")) {
            System.out.println(task);
        }

        //SubTask и model.Epic
        System.out.println("\nДобавление model.Epic и model.Subtask");
        Epic testEpic1 = new Epic(manager.getIdCounter(),
                "Test epic 1 with description",
                "Some description for the test epic 1");
        manager.createItem(testEpic1);
        Subtask testSubtask1 = new Subtask(manager.getIdCounter(),
                "Test subtask for epic 1 with description",
                "Some description for the test subtask 1");
        manager.createItem(testSubtask1);
        Subtask testSubtask2 = new Subtask(manager.getIdCounter(),
                "Test subtask 2 for epic 1 with description");
        manager.createItem(testSubtask2);

        testEpic1.addSubtask(testSubtask1);
        testSubtask1.setEpicId(testEpic1.getId());
        testEpic1.addSubtask(testSubtask2);
        testSubtask2.setEpicId(testEpic1.getId());

        Epic testEpic2 = new Epic(manager.getIdCounter(),
                "Test epic 2");
        manager.createItem(testEpic2);
        Subtask testSubtask3 = new Subtask(manager.getIdCounter(),
                "Test subtask 3 for epic 2");
        manager.createItem(testSubtask3);
        testEpic2.addSubtask(testSubtask3);
        testSubtask3.setEpicId(testEpic2.getId());

        for (Object epic : manager.getAllItemsByType("model.Epic")) {
            System.out.println(epic);
        }


        //тест кейс: получениее задачи по id
        System.out.println("\nПолучение model.Task по id");
        System.out.println(manager.getItemById(testTask1.getId()));
        System.out.println(manager.getItemById(testTask2.getId()));
        System.out.println(manager.getItemById(testEpic1.getId()));
        System.out.println(manager.getItemById(testSubtask1.getId()));


        //тест кейс: получение истории просмотра задач (меньше 10)
        System.out.println("\nПолучение истории (меньше 10 записей)");
        HistoryManager historyManager = manager.getHistoryManager();
        for (Object o : historyManager.getHistory()) {
            System.out.println(o);
        }

        //тест кейс: получение истории просмотра задач (больше 10)
        System.out.println("\nПолучение истории (больше 10 записей)");
        for (int i = 0; i < 10; i++) {
            manager.getItemById(testTask1.getId());
        }
        for (Object o : historyManager.getHistory()) {
            System.out.println(o);
        }

        //тест кейс: изменение статуса задач
        System.out.println("\nИзменение статуса model.Task");
        testTask1.setStatus(Status.IN_PROGRESS);
        manager.updateItem(testTask1, testTask1.getId());

        testTask2.setStatus(Status.NEW);
        manager.updateItem(testTask2, testTask2.getId());

        for (Object task : manager.getAllItemsByType("model.Task")) {
            System.out.println(task);
        }

        System.out.println("\nИзменение статуса model.Subtask: ");
        testSubtask1.setStatus(Status.IN_PROGRESS);
        testSubtask2.setStatus(Status.NEW);
        manager.updateItem(testSubtask1, testSubtask1.getId());
        manager.updateItem(testSubtask2, testSubtask2.getId());
        System.out.println(testEpic1);
        System.out.println("Возвращаем статус NEW model.Subtask 1: ");
        testSubtask1.setStatus(Status.NEW);
        manager.updateItem(testSubtask1, testSubtask1.getId());
        System.out.println(testEpic1);
        System.out.println("Проставить статус DONE одной из model.Subtask: ");
        testSubtask2.setStatus(Status.DONE);
        manager.updateItem(testSubtask2, testSubtask2.getId());
        System.out.println(testEpic1);
        System.out.println("Все model.Subtask в статусе DONE :");
        testSubtask1.setStatus(Status.DONE);
        manager.updateItem(testSubtask1, testSubtask1.getId());
        System.out.println(testEpic1);

        System.out.println("\nИзменение статуса model.Subtask (у model.Epic только 1 model.Subtask): ");
        System.out.println("Меняем статус на IN_PROGRESS:");
        testSubtask3.setStatus(Status.IN_PROGRESS);
        manager.updateItem(testSubtask3, testSubtask3.getId());
        System.out.println(testEpic2);
        System.out.println("Возвращаем NEW:");
        testSubtask3.setStatus(Status.NEW);
        manager.updateItem(testSubtask3, testSubtask3.getId());
        System.out.println(testEpic2);
        System.out.println("Меняем на DONE:");
        testSubtask3.setStatus(Status.DONE);
        manager.updateItem(testSubtask3, testSubtask3.getId());
        System.out.println(testEpic2);

        //тест кейс: удаление 1 задачи
        System.out.println("\nУдаление одной model.Task");
        manager.removeItemById(testTask2.getId());

        for (Object task : manager.getAllItemsByType("model.Task")) {
            System.out.println(task);
        }

        System.out.println("\nУдаление одной model.Subtask");
        manager.removeItemById(testSubtask3.getId());
        System.out.println(testEpic2);

        System.out.println("\nУдаление model.Epic");
        manager.removeItemById(testEpic2.getId());
        for (Object epic : manager.getAllItemsByType("model.Epic")) {
            System.out.println(epic);
        }

        //тест кейс: удаление всех задач по типу
        System.out.println("\nУдаление всех задач по типу model.Task");
        manager.removeAllItemsByType("model.Task");
        for (Object task : manager.getAllItemsByType("model.Task")) {
            System.out.println(task);
        }

        System.out.println("\nУдаление всех задач по типу model.Subtask");
        manager.removeAllItemsByType("model.Subtask");
        for (Object epic : manager.getAllItemsByType("model.Epic")) {
            System.out.println(epic);
        }

        System.out.println("\nУдаление всех задач по типу model.Epic");
        manager.removeAllItemsByType("model.Epic");
        for (Object epic : manager.getAllItemsByType("model.Epic")) {
            System.out.println(epic);
        }
    }
}
