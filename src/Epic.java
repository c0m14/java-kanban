import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Epic extends Task {
    private ArrayList<Subtask> epicSubtasks;

    public Epic(int id, String name) {
        super(id, name);
        this.epicSubtasks = new ArrayList<>();
    }

    public Epic(int id, String name, String description) {
        super(id, name, description);
        this.epicSubtasks = new ArrayList<>();
    }

    public void addSubtask(Subtask subtask) {
        this.epicSubtasks.add(subtask);
    }

    public void deleteSubtask(Subtask subtask) {
        Iterator<Subtask> iterator = epicSubtasks.iterator();
        while (iterator.hasNext()){
           Subtask currSubtask = iterator.next();
           if (subtask.getId() == currSubtask.getId()) {
               iterator.remove();
           }
        }
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
        if (!epicSubtasks.isEmpty()) {
            result = result + ", epicSubtasks={" + epicSubtasks +
                    '}';
        } else {
            result = result + ", epicSubtasks='{empty}'" +
                    '}';
        }
        return result;
    }
}
