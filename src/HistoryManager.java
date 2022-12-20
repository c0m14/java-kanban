import java.util.List;

public interface HistoryManager<T extends Task> {

    void add(T anyItem);

    List<T> getHistory();
}
