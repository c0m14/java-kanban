public class Main {

    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        //тест кейс: создание задач
        //Task
        System.out.println("Добавление Task");
        Task testTask1 = new Task(manager.getIdCounter(),
                "Test task 1 with description",
                "Some description of the test task 1");
        manager.createItem(testTask1);

        Task testTask2 = new Task(manager.getIdCounter(),
                "Test task 2 without description");
        manager.createItem(testTask2);

        for (Object task : manager.getAllTasks("Task")) {
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

        testEpic1.addSubtask(testEpic1.getId(), testSubtask1);
        testSubtask1.setEpicId(testEpic1.getId());
        testEpic1.addSubtask(testEpic1.getId(), testSubtask2);
        testSubtask2.setEpicId(testEpic1.getId());

        Epic testEpic2 = new Epic(manager.getIdCounter(),
                "Test epic 2");
        manager.createItem(testEpic2);
        Subtask testSubtask3 = new Subtask(manager.getIdCounter(),
                "Test subtask 3 for epic 2");
        manager.createItem(testSubtask3);
        testEpic2.addSubtask(testEpic2.getId(), testSubtask3);
        testSubtask3.setEpicId(testEpic2.getId());

        for (Object epic : manager.getAllTasks("Epic")) {
            System.out.println(epic);
        }

        //тест кейс: получениее задачи по id
        System.out.println("\nПолучение Task по id");
        System.out.println(manager.getItemById(testTask1.getId()));

        //тест кейс: изменение статуса задач
        System.out.println("\nИзменение статуса Task");
        testTask1.setStatus("IN_PROGRESS");
        manager.updateTask(testTask1, testTask1.getId());

        testTask2.setStatus("SOME_WRONG_STATUS");
        manager.updateTask(testTask2, testTask2.getId());

        for (Object task : manager.getAllTasks("Task")) {
            System.out.println(task);
        }

        //тест кейс: удаление 1 задачи
        System.out.println("\nУдаление одной Task");
        manager.removeItemById(testTask2.getId());

        for (Object task : manager.getAllTasks("Task")) {
            System.out.println(task);
        }

        //тест кейс: удаление всех задач по типу
        System.out.println("\nУдаление всех задач по типу Task");
        manager.removeAllItemsByType("Task");

        for (Object task : manager.getAllTasks("Task")) {
            System.out.println(task);
        }
    }
}
