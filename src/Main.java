public class Main {

    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        //тест кейс №1
        manager.createTask(new Task("Test task 1 with description",
                "Some description of " + "the test task 1",
                manager.getIdCounter()));
        manager.createTask(new Task("Test task 2 without description",
                manager.getIdCounter()));

        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }
    }
}
