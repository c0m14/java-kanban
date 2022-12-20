package managers;

import model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager<T extends Task> implements HistoryManager<T> {

    private final List<T> allItemsHistory;

    public InMemoryHistoryManager() {
        this.allItemsHistory = new ArrayList<>();
    }

    @Override
    public void add(T anyItem) {
        if (allItemsHistory.size() < 10) {
            allItemsHistory.add(anyItem);
        } else {
            allItemsHistory.remove(0);
            allItemsHistory.add(anyItem);
        }
    }

    @Override
    public List<T> getHistory() {
        return allItemsHistory;
    }
}
