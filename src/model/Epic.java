package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Epic extends Task {
    private List<Integer> epicSubtaskIds;
    private LocalDateTime endTime;

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

    public Epic(int id,
                String name,
                String description,
                Status status,
                ItemType itemType,
                Duration durationMinutes,
                LocalDateTime startTime,
                LocalDateTime endTime) {
        super(id, name, description, status, itemType, durationMinutes, startTime);
        this.epicSubtaskIds = new ArrayList<>();
        this.endTime = endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public Optional<LocalDateTime> getEndTime() {
        return Optional.of(this.endTime);
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
