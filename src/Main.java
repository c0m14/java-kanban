public class Main {

    public static void main(String[] args) {
        InMemoryTaskManager manager = new InMemoryTaskManager();

        //тест кейс: создание задач
        //Task
        System.out.println("Добавление Task");
        Task testTask1 = new Task(manager.getIdCounter(),
                "Test task 1 with description",
                "Some description of the test task 1");
        int id = manager.createItem(testTask1);

        Task testTask2 = new Task(manager.getIdCounter(),
                "Test task 2 without description");
        manager.createItem(testTask2);

        for (Object task : manager.getAllItemsByType("Task")) {
            System.out.println(task);
        }

        //SubTask и Epic
        System.out.println("\nДобавление Epic и Subtask");
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

        for (Object epic : manager.getAllItemsByType("Epic")) {
            System.out.println(epic);
        }


        //тест кейс: получениее задачи по id
        System.out.println("\nПолучение Task по id");
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
        System.out.println("\nИзменение статуса Task");
        testTask1.setStatus(Status.IN_PROGRESS);
        manager.updateItem(testTask1, testTask1.getId());

        testTask2.setStatus(Status.NEW);
        manager.updateItem(testTask2, testTask2.getId());

        for (Object task : manager.getAllItemsByType("Task")) {
            System.out.println(task);
        }

        System.out.println("\nИзменение статуса Subtask: ");
        testSubtask1.setStatus(Status.IN_PROGRESS);
        testSubtask2.setStatus(Status.NEW);
        manager.updateItem(testSubtask1, testSubtask1.getId());
        manager.updateItem(testSubtask2, testSubtask2.getId());
        System.out.println(testEpic1);
        System.out.println("Возвращаем статус NEW Subtask 1: ");
        testSubtask1.setStatus(Status.NEW);
        manager.updateItem(testSubtask1, testSubtask1.getId());
        System.out.println(testEpic1);
        System.out.println("Проставить статус DONE одной из Subtask: ");
        testSubtask2.setStatus(Status.DONE);
        manager.updateItem(testSubtask2, testSubtask2.getId());
        System.out.println(testEpic1);
        System.out.println("Все Subtask в статусе DONE :");
        testSubtask1.setStatus(Status.DONE);
        manager.updateItem(testSubtask1, testSubtask1.getId());
        System.out.println(testEpic1);

        System.out.println("\nИзменение статуса Subtask (у Epic только 1 Subtask): ");
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
        System.out.println("\nУдаление одной Task");
        manager.removeItemById(testTask2.getId());

        for (Object task : manager.getAllItemsByType("Task")) {
            System.out.println(task);
        }

        System.out.println("\nУдаление одной Subtask");
        manager.removeItemById(testSubtask3.getId());
        System.out.println(testEpic2);

        System.out.println("\nУдаление Epic");
        manager.removeItemById(testEpic2.getId());
        for (Object epic : manager.getAllItemsByType("Epic")) {
            System.out.println(epic);
        }

        //тест кейс: удаление всех задач по типу
        System.out.println("\nУдаление всех задач по типу Task");
        manager.removeAllItemsByType("Task");
        for (Object task : manager.getAllItemsByType("Task")) {
            System.out.println(task);
        }

        System.out.println("\nУдаление всех задач по типу Subtask");
        manager.removeAllItemsByType("Subtask");
        for (Object epic : manager.getAllItemsByType("Epic")) {
            System.out.println(epic);
        }

        System.out.println("\nУдаление всех задач по типу Epic");
        manager.removeAllItemsByType("Epic");
        for (Object epic : manager.getAllItemsByType("Epic")) {
            System.out.println(epic);
        }
    }
}
