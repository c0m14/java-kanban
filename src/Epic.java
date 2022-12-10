import java.util.ArrayList;
import java.util.HashMap;

public class Epic extends Task {
    HashMap<Integer, ArrayList<Subtask>> epicSubtasks;

    public Epic(int id, String name) {
        super(id, name);
        this.epicSubtasks = new HashMap<>();
    }

    public Epic(int id, String name, String description) {
        super(id, name, description);
        this.epicSubtasks = new HashMap<>();
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
            result = result + ", epicSubtasks={" + epicSubtasks +
                    '}';
        } else {
            result = result + ", epicSubtasks='null'" +
                    '}';
        }
        return result;
    }
}
