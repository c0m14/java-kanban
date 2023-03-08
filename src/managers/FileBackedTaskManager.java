package managers;

import exceptions.ManagerSaveException;
import model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private final Path backupFilePath;

    public FileBackedTaskManager(Path backupFilePath) {
        super();
        this.backupFilePath = backupFilePath;
    }

    private FileBackedTaskManager(int idCounter,
                                 HashMap<ItemType, HashMap<Integer, Task>> allItems,
                                 HistoryManager historyManager,
                                 TreeSet<Task> prioritizedItems,
                                 Path backupFilePath) {
        super(idCounter, allItems, historyManager, prioritizedItems);
        this.backupFilePath = backupFilePath;
    }

    protected FileBackedTaskManager() {
        super();
        backupFilePath = Path.of("project_files/autosave.txt");
    }

    protected FileBackedTaskManager(int idCounter,
                                    HashMap<ItemType, HashMap<Integer, Task>> allItems,
                                    HistoryManager historyManager,
                                    TreeSet<Task> prioritizedItems) {
        super(idCounter, allItems, historyManager, prioritizedItems);
        backupFilePath = Path.of("project_files/autosave.txt");
    }

    public static FileBackedTaskManager loadFromFile(Path file) {
        List<Task> tasksFromFile = new ArrayList<>(); //временное хранилище всех items из файла
        String historyIdsLine = "";
        int restoredIdCounter;
        HashMap<ItemType, HashMap<Integer, Task>> restoredAllItems;
        TreeSet<Task> restoredPrioritizedItems = new TreeSet<>(new TaskStartTimeComparator());
        HistoryManager restoredHistoryManager;

        //вычитываем данные из файла
        try {
            tasksFromFile = loadTasksFromFile(file);
            historyIdsLine = loadHistoryLineFromFile(file);
        } catch (IOException e) {
            System.out.println("Ошибка чтения файла");
        }
        //Восстанавливаем структуру и список приоритетов
        restoredAllItems = restoreAllItemsWithPriorities(tasksFromFile, restoredPrioritizedItems);
        //Восстанавливаем подзадачи для эпиков
        restoreSubtasksForEpics(restoredAllItems);
        //Актуализируем idCounter
        restoredIdCounter = restoredAllItems.values().stream()
                .flatMap(hashmap -> hashmap.keySet().stream())
                .mapToInt(Integer::intValue)
                .max().getAsInt();
        //Восстанавливаем HistoryManager
        restoredHistoryManager = restoreHistoryManager(historyIdsLine, restoredAllItems);
        //Конструируем FileBackedTaskManager
        FileBackedTaskManager restoredFileManager = new FileBackedTaskManager(restoredIdCounter,
                restoredAllItems,
                restoredHistoryManager,
                restoredPrioritizedItems,
                file);
        //Восстанавливаем startTime / duration / EndTime для Epic
        if (restoredAllItems.get(ItemType.EPIC) != null) {
            restoredAllItems.get(ItemType.EPIC).values().stream()
                    .map(Epic.class::cast)
                    .mapToInt(Epic::getId)
                    .forEach(restoredFileManager::updateEpicStartTimeDurationEndTime);
        }
        return restoredFileManager;
    }

    private  static List<Task> loadTasksFromFile(Path path) throws IOException {
        List<Task> tasksFromFile = new ArrayList<>();

        try (Stream<String> lines = Files.lines(path)) {
            lines.filter(line -> line.contains("TASK") || line.contains("SUBTASK") || line.contains("EPIC"))
                    .forEach(line -> tasksFromFile.add(fromString(line)));
        }

        return tasksFromFile;
    }

    private static String loadHistoryLineFromFile(Path path) throws IOException {
        StringBuilder historyLineBuilder = new StringBuilder();

        try (Stream<String> lines = Files.lines(path)) {
            lines.skip(1)
                    .filter(line -> !line.contains("TASK")
                                    && !line.contains("SUBTASK")
                                    && !line.contains("EPIC")
                    &&!line.equals(""))
                    .forEach(historyLineBuilder::append);
        }
        return historyLineBuilder.toString();
    }

    private static HashMap<ItemType, HashMap<Integer, Task>> restoreAllItemsWithPriorities(List<Task> tasksFromFile,
                                                                  TreeSet<Task> restoredPrioritizedItems) {
        HashMap<ItemType, HashMap<Integer, Task>> restoredAllItems = new HashMap<>();
        HashMap<Integer, Task> items;
        for (Task item : tasksFromFile) {
            ItemType currentItemType = item.getItemType();

            //Восстанавливаем структуру allItems
            if (restoredAllItems.get(currentItemType) != null) {
                items = restoredAllItems.get(currentItemType);
            } else {
                items = new HashMap<>();
            }
            items.put(item.getId(), item);
            restoredAllItems.put(currentItemType, items);
            //восстанавливаем prioritizedItems
            restoredPrioritizedItems.add(item);
        }
        return restoredAllItems;
    }

    private static void restoreSubtasksForEpics(HashMap<ItemType, HashMap<Integer, Task>> restoredAllItems) {
        if (restoredAllItems.get(ItemType.SUBTASK) == null) {
            return;
        }
        for (Task subtask : restoredAllItems.get(ItemType.SUBTASK).values()) {
            restoredAllItems.get(ItemType.EPIC).values().stream()
                    .map(Epic.class::cast)
                    .forEach(epic -> epic.addSubtask((Subtask) subtask));
        }
    }

    private static HistoryManager restoreHistoryManager(String historyIdsLine,
                                                        HashMap<ItemType, HashMap<Integer, Task>> restoredAllItems) {
        HistoryManager restoredHistoryManager = Managers.getDefaultHistory();

        String[] historyIdsFromLine = historyIdsLine.split(",");
        if (!historyIdsLine.equals("")) {
            for (String id : historyIdsFromLine) {
                for (HashMap<Integer, Task> entrySet : restoredAllItems.values()) {
                    restoredHistoryManager.add(entrySet.get(Integer.parseInt(id)));
                }
            }
        }
        return restoredHistoryManager;
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
                    "startTime",
                    "duration",
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
    public void linkSubtaskToEpic(Subtask subtask, Epic epic) {
        super.linkSubtaskToEpic(subtask, epic);
        save();
    }
}
