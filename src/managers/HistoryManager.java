package managers;

import model.Task;

import java.util.List;

public interface HistoryManager {

    void add(Task anyItem);

    List<Task> getHistory();

    void remove(int anyItemId);
}
