package managers;

import model.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileBackedTaskManager extends InMemoryTaskManager {
    Path backupFilePath;

    public FileBackedTaskManager(Path backupFilePath) {
        super();
        this.backupFilePath = backupFilePath;
    }

    public static void main(String[] args) {
        Path autosaveFile = Paths.get("/Users/nikkomochkov/Documents/Coding/dev/java-kanban/project_files/autosave.txt");

        try {
            if (Files.exists(autosaveFile)) {
                Files.delete(autosaveFile);
            }
            Files.createFile(autosaveFile);
        } catch (IOException e) {
            System.out.println("Ошибка при создании файла автосохранения");
        }

        FileBackedTaskManager fileManager = (FileBackedTaskManager) Managers.getDefault(autosaveFile);

        //Создание Task
        Task testTask1 = new Task(fileManager.getIdCounter(),
                "Test task 1 with description",
                "Some description of the test task 1");
        int id = fileManager.createItem(testTask1);


        //Cоздание Epic и Subtask
        Epic testEpic1 = new Epic(fileManager.getIdCounter(),
                "Test epic 1 with description",
                "Some description for the test epic 1");
        fileManager.createItem(testEpic1);
        Subtask testSubtask1 = new Subtask(fileManager.getIdCounter(),
                "Test subtask for epic 1 with description",
                "Some description for the test subtask 1");
        fileManager.createItem(testSubtask1);
        Subtask testSubtask2 = new Subtask(fileManager.getIdCounter(),
                "Test subtask 2 for epic 1 with description");
        fileManager.createItem(testSubtask2);

        fileManager.addSubtask(testSubtask1, testEpic1);
        fileManager.addSubtask(testSubtask2, testEpic1);

    }

    private void save() throws ManagerSaveException {
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(backupFilePath.toFile()))) {
            String header = String.join(",",
                    "id", "type", "name", "status", "description", "epic\n");

            fileWriter.write(header);
            for (Object task : super.getAllItemsOfAllTypes()) {
                fileWriter.write(toString((Task) task));
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Произошла ошибка при записи в файл");
        }
    }

    private String toString(Task task) {
        String[] lineElements =
                {
                        String.valueOf(task.getId()),
                        String.valueOf(task.getItemType()),
                        task.getName(),
                        String.valueOf(task.getStatus()),
                        task.getDescription(),
                };

        String line = String.join(",", lineElements);
        StringBuilder backupLineBuilder = new StringBuilder(line);

        if (task.getItemType().equals(ItemType.SUBTASK)) {
            Subtask thisTask = (Subtask) task;
            backupLineBuilder.append("," + thisTask.getEpicId() + "\n");
        } else {
            backupLineBuilder.append(", \n");
        }

        return backupLineBuilder.toString();
    }

    @Override
    public int createItem(Task anyItem) {
        int itemId = super.createItem(anyItem);
        save();
        return itemId;
    }

    @Override
    public void updateItem(Task anyItem, int id) {
        super.updateItem(anyItem, id);
        save();
    }

    @Override
    public void removeItemById(int id) {
        super.removeItemById(id);
        save();
    }

    @Override
    public void removeAllItemsByType(ItemType itemType) {
        super.removeAllItemsByType(itemType);
        save();
    }

    @Override
    public Task getItemById(int id) {
        Task task = super.getItemById(id);
        save();
        return task;
    }

    @Override
    public void addSubtask(Subtask subtask, Epic epic) {
        super.addSubtask(subtask, epic);
        save();
    }
}
