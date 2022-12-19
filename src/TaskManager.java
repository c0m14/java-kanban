import java.util.ArrayList;
import java.util.List;

public interface TaskManager {

    int createItem(Object anyItem);

    int getIdCounter();

    ArrayList<Object> getAllItemsByType(String itemType);

    void updateItem(Object anyItem, int id);

    void removeItemById(int id);

    void removeAllItemsByType(String itemType);

    List<Task> getHistory();
}