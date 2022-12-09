public class Main {

    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        //тест кейс №1
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
    }
}
