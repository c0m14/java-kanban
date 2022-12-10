import java.util.ArrayList;
import java.util.HashMap;

public class Epic extends Task {
    private HashMap<Integer, ArrayList<Subtask>> epicSubtasks;

    public Epic(int id, String name) {
        super(id, name);
        this.epicSubtasks = new HashMap<>();
    }

    public Epic(int id, String name, String description) {
        super(id, name, description);
        this.epicSubtasks = new HashMap<>();
    }

    public void addSubtask (int id, Subtask subtask){
        ArrayList<Subtask> linkSubtasks;
        if (epicSubtasks.get(id) != null) {
            linkSubtasks = epicSubtasks.get(id);
        } else {
            linkSubtasks = new ArrayList<>();
        }
        linkSubtasks.add(subtask);
        epicSubtasks.put(id,linkSubtasks);

    }

    @Override
    public String toString() {
        String result = "Epic{" +
                "id=" + id +
                ", name='" + name + '\'';
        if (description != null) {
            result = result + ", description.length()='" + description.length() + '\'';
        } else {
            result = result + ", description.length()='null'";
        }
        result = result +
                ", status='" + status + '\'';
        if (epicSubtasks != null) {
            result = result + ", epicSubtasks={" + epicSubtasks.get(this.id) +
                    '}';
        } else {
            result = result + ", epicSubtasks='null'" +
                    '}';
        }
        return result;
    }
}
