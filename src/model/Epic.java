package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
                ItemType itemType,
                Duration durationMinutes,
                LocalDateTime startTime) {
        super(id, name, description, status, itemType, durationMinutes, startTime);
        this.epicSubtaskIds = new ArrayList<>();
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        StringBuilder sb = new StringBuilder("EPIC{");
        sb.append("id=")
                .append(id)
                .append(", name='")
                .append(name)
                .append('\'');
        if (description != null) {
            sb.append(", description.length()='")
                    .append(description.length())
                    .append('\'');
        } else {
            sb.append(", description.length()='null'");
        }
        sb.append(", status='")
                .append(status)
                .append('\'');
        if (startTime != null) {
            sb.append(", startTime='")
                    .append(startTime.format(formatter))
                    .append('\'');
        } else {
            sb.append(", startTime='null'");
        }
        if (durationMinutes !=null) {
            sb.append(", duration='")
                    .append(durationMinutes)
                    .append('\'');
        } else {
            sb.append(", duration='null");
        }
        if (endTime != null) {
            sb.append(", endTime='")
                    .append(endTime.format(formatter))
                    .append('\'');
        } else {
            sb.append(", endTime='null'");
        }
        if (!epicSubtaskIds.isEmpty()) {
            sb.append(", epicSubtaskIds={")
                    .append(epicSubtaskIds);
        } else {
            sb.append(", epicSubtaskIds='{empty}'");
        }
        sb.append('}');

        return sb.toString();
    }
}
