package managers;

import exceptions.ManagerSaveException;
import model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private final Path backupFilePath;

    public FileBackedTaskManager(Path backupFilePath) {
        super();
        this.backupFilePath = backupFilePath;

    }

    public FileBackedTaskManager(int idCounter,
                                 HashMap<ItemType, HashMap<Integer, Task>> allItems,
                                 HistoryManager historyManager,
                                 TreeSet<Task> prioritizedItems,
                                 Path backupFilePath) {
        super(idCounter, allItems, historyManager, prioritizedItems);
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

        testSubtask1.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        testSubtask2.setStartTime(LocalDateTime.parse("03-02-2023 16:20", formatter));
        testSubtask1.setDurationMinutes(Duration.of(30, ChronoUnit.MINUTES));
        testSubtask2.setDurationMinutes(Duration.of(120, ChronoUnit.MINUTES));

        //Запись в историю
        fileManager.getItemById(testTask1.getId());
        fileManager.getItemById(testEpic1.getId());
        fileManager.getItemById((testSubtask1.getId()));
        fileManager.getItemById(testTask1.getId());


        //Восстановление из файла
        FileBackedTaskManager restoredFileManager = loadFromFile(autosaveFile);

        System.out.println("Восстановленные задачи:");
        for (Task task : restoredFileManager.getAllItemsOfAllTypes()) {
            System.out.println(task);
        }
        System.out.println();

        System.out.println("Восстановленная история:");
        for (Task historyRecord : restoredFileManager.getHistoryManager().getHistory()) {
            System.out.println(historyRecord);
        }
    }

    public static FileBackedTaskManager loadFromFile(Path file) {
        List<Task> tasksFromFile = new ArrayList<>(); //временное хранилище всех items из файла
        int restoredIdCounter = -1;

        HashMap<ItemType, HashMap<Integer, Task>> restoredAllItems = new HashMap<>();
        HashMap<Integer, Task> items;
        TreeSet<Task> restoredPrioritizedItems = new TreeSet<>((task1, task2) -> {
            if (task1.getStartTime() == null && task2.getStartTime() == null) {
                return 2;
            } else if (task1.getStartTime() == null && task2.getStartTime() != null) {
                return 1;
            } else if (task1.getStartTime() != null && task2.getStartTime() == null) {
                return -1;
            } else return task1.getStartTime().compareTo(task2.getStartTime());
        });

        HashMap<Integer, List<Integer>> epicsSubtasksIds = new HashMap<>();
        List<Integer> subtasksIdsForEpic;

        String historyIdsLine = "";
        HistoryManager restoredHistoryManager = Managers.getDefaultHistory();

        //вычитываем данные из файла
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
                restoredPrioritizedItems.add(task);
            }
            if (task.getItemType().equals(ItemType.SUBTASK)) {
                if (restoredAllItems.get(ItemType.SUBTASK) != null) {
                    items = restoredAllItems.get(ItemType.SUBTASK);
                } else {
                    items = new HashMap<>();
                }
                items.put(task.getId(), task);
                restoredAllItems.put(ItemType.SUBTASK, items);
                restoredPrioritizedItems.add(task);

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
        if (historyIdsFromLine.equals("")){
            for (String id : historyIdsFromLine) {
                for (HashMap<Integer, Task> entrySet : restoredAllItems.values()) {
                    restoredHistoryManager.add(entrySet.get(Integer.parseInt(id)));
                }
            }
        }

        FileBackedTaskManager restoredFileManager = new FileBackedTaskManager(restoredIdCounter,
                restoredAllItems,
                restoredHistoryManager,
                restoredPrioritizedItems,
                file);

        //Восстанавливаем startTime / duration / EndTime для Epic
        for (Integer epicId : epicsSubtasksIds.keySet()) {
            restoredFileManager.updateEpicStartTimeDurationEndTime(epicId);
        }

        return restoredFileManager;
    }

    private static String historyToString(HistoryManager historyManager) {
        StringBuilder historyIdBuilder = new StringBuilder();
        List<Task> historyList = historyManager.getHistory();
        for (int i = 0; i < historyList.size(); i++) {
            if (i < historyList.size() - 1) {
                historyIdBuilder.append(historyList.get(i).getId())
                        .append(",");
            } else {
                historyIdBuilder.append(historyList.get(i).getId());
            }
        }
        return historyIdBuilder.toString();
    }

    private static Task fromString(String value) {
        String[] taskParams = value.split(",");
        LocalDateTime startTime = taskParams[5].equals("") ? null : LocalDateTime.parse(taskParams[5], formatter);
        Duration duration = taskParams[6].equals("") ? null : Duration.parse(taskParams[6]);
        // [0]:id,
        // [1]:type,
        // [2]:name,
        // [3]:status,
        // [4]:description,
        // [5]:duration "PT8H6M12"
        // [6]:startTime "dd-MM-yyyy HH:mm"
        // [7]:epic
        switch (taskParams[1]) {
            case "TASK":
                return new Task(Integer.parseInt(taskParams[0]),
                        taskParams[2],
                        taskParams[4],
                        Status.valueOf(taskParams[3]),
                        ItemType.valueOf(taskParams[1]),
                        duration,
                        startTime);
            case "SUBTASK":
                return new Subtask(Integer.parseInt(taskParams[0]),
                        taskParams[2],
                        taskParams[4],
                        Status.valueOf(taskParams[3]),
                        ItemType.valueOf(taskParams[1]),
                        duration,
                        startTime,
                        Integer.parseInt(taskParams[7]));
            case "EPIC":
                return new Epic(Integer.parseInt(taskParams[0]),
                        taskParams[2],
                        taskParams[4],
                        Status.valueOf(taskParams[3]),
                        ItemType.valueOf(taskParams[1]),
                        duration,
                        startTime);
            default:
                return null;
        }
    }

    private void save() throws ManagerSaveException {
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(backupFilePath.toFile()))) {
            String header = String.join(",",
                    "id",
                    "type",
                    "name",
                    "status",
                    "description",
                    "duration",
                    "startTime",
                    "epic\n");

            fileWriter.write(header);
            for (Task task : super.getAllItemsOfAllTypes()) {
                fileWriter.write(toString((task)));
            }
            fileWriter.write("\n");
            fileWriter.write(historyToString(historyManager));
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
                        (task.getStartTime() == null) ? "" : task.getStartTime().format(formatter),
                        (task.getDurationMinutes() == null) ? "" : task.getDurationMinutes().toString()
                };

        String line = String.join(",", lineElements);
        StringBuilder backupLineBuilder = new StringBuilder(line);

        if (task.getItemType().equals(ItemType.SUBTASK)) {
            Subtask thisTask = (Subtask) task;
            backupLineBuilder.append(",")
                    .append(thisTask.getEpicId())
                    .append("\n");
        } else {
            backupLineBuilder.append(", ")
                    .append("\n");
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
