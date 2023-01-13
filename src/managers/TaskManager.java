package managers;

import model.ItemType;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;

public interface TaskManager<T extends Task> {

    int createItem(T anyItem);

    int getIdCounter();

    ArrayList<T> getAllItemsByType(ItemType itemType);

    void updateItem(T anyItem, int id);

    void removeItemById(int id);

    void removeAllItemsByType(ItemType itemType);

    T getItemById(int id);
}