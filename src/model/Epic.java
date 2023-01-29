package model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> epicSubtaskIds;

    public Epic(int id, String name) {
        super(id, name);
        this.epicSubtaskIds = new ArrayList<>();
        this.itemType = ItemType.EPIC;
    }

    public Epic(int id, String name, String description) {
        super(id, name, description);
        this.epicSubtaskIds = new ArrayList<>();
        this.itemType = ItemType.EPIC;
    }

    public Epic(int id,
                String name,
                String description,
                Status status,
                ItemType itemType) {
        super(id, name, description, status, itemType);
        this.epicSubtaskIds = new ArrayList<>();
    }

    public void addSubtask(Subtask subtask) {
        this.epicSubtaskIds.add(subtask.getId());
    }

    public void deleteSubtaskById(Integer id) {
        epicSubtaskIds.remove(id);
    }

    public List<Integer> getEpicSubtaskIds() {
        return epicSubtaskIds;
    }

    public void loadEpicSubtasksIds(List<Integer> epicSubtaskIds) {
        this.epicSubtaskIds = epicSubtaskIds;
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
        if (!epicSubtaskIds.isEmpty()) {
            result = result + ", epicSubtaskIds={" + epicSubtaskIds +
                    '}';
        } else {
            result = result + ", epicSubtaskIds='{empty}'" +
                    '}';
        }
        return result;
    }
}
