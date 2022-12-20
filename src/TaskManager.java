import java.util.ArrayList;
import java.util.List;

public interface TaskManager <T extends Task>{

    int createItem(T anyItem);

    int getIdCounter();

    ArrayList<T> getAllItemsByType(String itemType);

    void updateItem(T anyItem, int id);

    void removeItemById(int id);

    void removeAllItemsByType(String itemType);
}