public class Main {

    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        //тест кейс: создание задач
        System.out.println("Добавление задач");
        Task testTask1 = new Task(manager.getIdCounter(),
                "Test task 1 with description",
                "Some description of " + "the test task 1");
        manager.createTask(testTask1);

        Task testTask2 = new Task(manager.getIdCounter(),
                "Test task 2 without description");
        manager.createTask(testTask2);

        for (Object task : manager.getAllTasks("Task")) {
            System.out.println(task);
        }

        //тест кейс: получениее задачи по id
        System.out.println("\nПолучение задачи по id");
        System.out.println(manager.getItemById(1));

        //тест кейс: изменение статуса задач
        System.out.println("\nИзменение статуса задач");
        testTask1.setStatus("IN_PROGRESS");
        manager.updateTask(testTask1, testTask1.getId());

        testTask2.setStatus("SOME_WRONG_STATUS");
        manager.updateTask(testTask2, testTask2.getId());

        for (Object task : manager.getAllTasks("Task")) {
            System.out.println(task);
        }

        //тест кейс: удаление 1 задачи
        System.out.println("\nУдаление одной задачи");
        manager.removeItemById(testTask2.getId());

        for (Object task : manager.getAllTasks("Task")) {
            System.out.println(task);
        }

        //тест кейс: удаление всех задач по типу
        System.out.println("\nУдаление всех задач по типу");
        manager.removeAllItemsByType("Task");

        for (Object task : manager.getAllTasks("Task")) {
            System.out.println(task);
        }
    }
}
