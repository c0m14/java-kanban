public class Main {

    public static void main(String[] args) {
        Task testTask = new Task("Продумать структуру проекта",
                "Прежде чем приступить к написанию кода"
                + " нужно понять какие классы у нас будут и как они будут связаны между собой", 5);
        System.out.println(testTask);
    }
}
