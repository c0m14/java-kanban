public class Main {

    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        //тест кейс №1: создание задач
        Task testTask1 = new Task(manager.getIdCounter(),
                "Test task 1 with description",
                "Some description of " + "the test task 1");
        manager.createTask(testTask1);

        Task testTask2 = new Task(manager.getIdCounter(),
                "Test task 2 without description");
        manager.createTask(testTask2);

        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        //тест кейс №2: изменение статуса задач
        testTask1.setStatus("IN_PROGRESS");
        manager.updateTask(testTask1, testTask1.getId());

        testTask2.setStatus("SOME_WRONG_STATUS");
        manager.updateTask(testTask2, testTask2.getId());

        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }
    }
}
