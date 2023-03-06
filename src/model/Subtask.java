package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Subtask extends Task {
    private int epicId;

    public Subtask(int id, String name) {
        super(id, name);
        this.itemType = ItemType.SUBTASK;
    }

    public Subtask(String name) {
        super(name);
        this.itemType = ItemType.SUBTASK;
    }

    public Subtask(int id, String name, String description) {
        super(id, name, description);
        this.itemType = ItemType.SUBTASK;
    }

    public Subtask(String name, String description) {
        super(name, description);
        this.itemType = ItemType.SUBTASK;
    }

    public Subtask(int id,
                   String name,
                   String description,
                   Status status,
                   ItemType itemType,
                   Duration durationMinutes,
                   LocalDateTime startTime,
                   int epicId) {
        super(id, name, description, status, itemType, durationMinutes, startTime);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        StringBuilder sb = new StringBuilder("SUBTASK{");
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
        if (durationMinutes != null) {
            sb.append(", duration='")
                    .append(durationMinutes)
                    .append('\'');
        } else {
            sb.append(", duration='null', ");
        }
        sb.append(epicId)
                .append('}');

        return sb.toString();

    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epicId);
    }
}
