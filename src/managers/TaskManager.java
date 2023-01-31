package managers;

import model.ItemType;
import model.Task;

import java.util.ArrayList;

public interface TaskManager {

    int createItem(Task anyItem);


    int getIdCounter();

    ArrayList<Task> getAllItemsByType(ItemType itemType);

    void updateItem(Task anyItem, int id);

    void removeItemById(int id);

    void removeAllItemsByType(ItemType itemType);

    Task getItemById(int id);
}