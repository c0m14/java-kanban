package managers;

import model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileBackedTaskManager<T extends Task> extends InMemoryTaskManager {
    private Path backupFilePath;

    public FileBackedTaskManager(Path backupFilePath) {
        super();
        this.backupFilePath = backupFilePath;

    }

    public FileBackedTaskManager(int idCounter, HashMap<ItemType, HashMap<Integer, T>> allItems, HistoryManager<T> historyManager, Path backupFilePath) {
        super(idCounter, allItems, historyManager);
        this.backupFilePath = backupFilePath;
    }

    public static void main(String[] args) {

        Path autosaveFile = Paths.get("project_files/autosave.txt");

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


        //Запись в историю
        fileManager.getItemById(testTask1.getId());
        fileManager.getItemById(testEpic1.getId());
        fileManager.getItemById((testSubtask1.getId()));
        fileManager.getItemById(testTask1.getId());


        //Восстановление из файла
        FileBackedTaskManager restoredFileManager = loadFromFile(autosaveFile);

        System.out.println("Восстановленные задачи:");
        for (Object task : restoredFileManager.getAllItemsOfAllTypes()) {
            System.out.println(task);
        }
        System.out.println();

        System.out.println("Восстановленная история:");
        for (Object historyRecord : restoredFileManager.getHistoryManager().getHistory()) {
            System.out.println(historyRecord);
        }
    }

    public static FileBackedTaskManager loadFromFile(Path file) {
        List<Task> tasksFromFile = new ArrayList<>(); //временное хранилище всех items из файла
        int restoredIdCounter = -1;

        HashMap<ItemType, HashMap<Integer, Task>> restoredAllItems = new HashMap<>();
        HashMap<Integer, Task> items;

        HashMap<Integer, List<Integer>> epicsSubtasksIds = new HashMap<>();
        List<Integer> subtasksIdsForEpic;

        String historyIdsLine = "";
        HistoryManager<Task> restoredHistoryManager = Managers.getDefaultHistory();

        //вычитываем даные из файла
        try (BufferedReader fileReader = new BufferedReader(new FileReader(file.toFile()))) {
            while (fileReader.ready()) {
                String line = fileReader.readLine();
                if (line.contains("id")) {
                    continue;
                } else if (line.contains("TASK") || line.contains("SUBTASK") || line.contains("EPIC")) {
                    tasksFromFile.add(fromString(line));
                } else if (line.equals("")) {
                    continue;
                } else {
                    historyIdsLine = line; //читаем пустую строку-разделитель
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка чтения файла");
        }

        //Восстанавливаем структуру items
        for (Task task : tasksFromFile) {
            if (task.getItemType().equals(ItemType.TASK)) {
                if (restoredAllItems.get(ItemType.TASK) != null) {
                    items = restoredAllItems.get(ItemType.TASK);
                } else {
                    items = new HashMap<>();
                }
                items.put(task.getId(), task);
                restoredAllItems.put(ItemType.TASK, items);
            }
            if (task.getItemType().equals(ItemType.SUBTASK)) {
                if (restoredAllItems.get(ItemType.SUBTASK) != null) {
                    items = restoredAllItems.get(ItemType.SUBTASK);
                } else {
                    items = new HashMap<>();
                }
                items.put(task.getId(), task);
                restoredAllItems.put(ItemType.SUBTASK, items);

                int epicId = ((Subtask) task).getEpicId();
                if (epicsSubtasksIds.get(epicId) == null) {
                    subtasksIdsForEpic = new ArrayList<>();
                } else {
                    subtasksIdsForEpic = epicsSubtasksIds.get(epicId);
                }
                subtasksIdsForEpic.add(task.getId());
                epicsSubtasksIds.put(epicId, subtasksIdsForEpic);
            }
            if (task.getItemType().equals(ItemType.EPIC)) {
                if (restoredAllItems.get(ItemType.EPIC) != null) {
                    items = restoredAllItems.get(ItemType.EPIC);
                } else {
                    items = new HashMap<>();
                }
                items.put(task.getId(), task);
                restoredAllItems.put(ItemType.EPIC, items);
            }
            //Актуализируем idCounter
            restoredIdCounter = Integer.max(task.getId(), restoredIdCounter);
        }

        //Восстанавливаем для Epic знание о своих Subtask
        for (Integer epicId : epicsSubtasksIds.keySet()) {
            ((Epic) restoredAllItems.get(ItemType.EPIC)
                    .get(epicId))
                    .loadEpicSubtasksIds(epicsSubtasksIds.get(epicId));
        }

        //Восстанавливаем историю
        String[] historyIdsFromLine = historyIdsLine.split(",");

        for (String id : historyIdsFromLine) {
            for (HashMap<Integer, Task> entrySet : restoredAllItems.values()) {
                restoredHistoryManager.add(entrySet.get(Integer.parseInt(id)));
            }
        }

        return new FileBackedTaskManager(restoredIdCounter, restoredAllItems, restoredHistoryManager, file);
    }

    public static String historyToString(HistoryManager<Task> historyManager) {
        StringBuilder historyIdBuilder = new StringBuilder();
        List<Task> historyList = historyManager.getHistory();
        for (int i = 0; i < historyList.size(); i++) {
            if (i < historyList.size() - 1) {
                historyIdBuilder.append(historyList.get(i).getId() + ",");
            } else {
                historyIdBuilder.append(historyList.get(i).getId());
            }
        }


        return historyIdBuilder.toString();
    }

    public static Task fromString(String value) {
        String[] taskParams = value.split(",");

        //[0]:id, [1]:type, [2]:name, [3]:status, [4]:description, [5]:epic
        switch (taskParams[1]) {
            case "TASK":
                return new Task(Integer.parseInt(taskParams[0]),
                        taskParams[2],
                        taskParams[4],
                        Status.stringToStatus(taskParams[3]),
                        ItemType.stringToItemType(taskParams[1]));
            case "SUBTASK":
                return new Subtask(Integer.parseInt(taskParams[0]),
                        taskParams[2],
                        taskParams[4],
                        Status.stringToStatus(taskParams[3]),
                        ItemType.stringToItemType(taskParams[1]),
                        Integer.parseInt(taskParams[5]));
            case "EPIC":
                return new Epic(Integer.parseInt(taskParams[0]),
                        taskParams[2],
                        taskParams[4],
                        Status.stringToStatus(taskParams[3]),
                        ItemType.stringToItemType(taskParams[1]));
            default:
                return null;

        }
    }

    private void save() throws ManagerSaveException {
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(backupFilePath.toFile()))) {
            String header = String.join(",",
                    "id", "type", "name", "status", "description", "epic\n");

            fileWriter.write(header);
            for (Object task : super.getAllItemsOfAllTypes()) {
                fileWriter.write(toString((T) task));
            }
            fileWriter.write("\n");
            fileWriter.write(historyToString(historyManager));
        } catch (IOException e) {
            throw new ManagerSaveException("Произошла ошибка при записи в файл");
        }
    }

    private String toString(T task) {
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
            backupLineBuilder.append(", " + "\n");
        }

        return backupLineBuilder.toString();
    }


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
