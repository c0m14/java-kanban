package managers;

import model.Epic;
import model.ItemType;
import model.Subtask;
import model.Task;

import java.io.IOException;
import java.util.ArrayList;

public interface TaskManager {

    int createItem(Task anyItem);

    int getIdCounter();

    ArrayList<Task> getAllItemsByType(ItemType itemType);

    void updateItem(Task anyItem, int id);

    void removeItemById(int id);

    void removeAllItemsByType(ItemType itemType);

    Task getItemById(int id);

    ArrayList<Task> getPrioritizedTasks();

    void linkSubtaskToEpic(Subtask subtask, Epic epic);

    ArrayList<Subtask> getEpicSubtasks(int epicId);
}