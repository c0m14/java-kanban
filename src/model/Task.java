package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    protected int id;
    protected String name;
    protected String description;
    protected Status status;
    protected ItemType itemType;
    protected Duration durationMin;
    protected LocalDateTime startTime;

    //description, durationMin и startTime при создании опциональны

    public Task(int id, String name) {
        this.name = name;
        this.id = id;
        this.description = "";
        this.status = Status.NEW;
        this.itemType = ItemType.TASK;
    }

    public Task(int id, String name, String description) {
        this(id, name);
        this.description = description;
    }

    public Task(int id,
                String name,
                Duration durationMin,
                LocalDateTime startTime) {
        this.id = id;
        this.name = name;
        this.description = "";
        this.durationMin = durationMin;
        this.startTime = startTime;
        this.itemType = ItemType.TASK;
        this.status = Status.NEW;
    }

    public Task(int id,
                String name,
                String description,
                Duration durationMin,
                LocalDateTime startTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.durationMin = durationMin;
        this.startTime = startTime;
        this.itemType = ItemType.TASK;
        this.status = Status.NEW;
    }

    public Task(int id,
                String name,
                String description,
                Status status,
                ItemType itemType,
                Duration durationMin,
                LocalDateTime startTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.itemType = itemType;
        this.durationMin = durationMin;
        this.startTime = startTime;
    }

    public Task(int id,
                String name,
                String description,
                Status status,
                ItemType itemType) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.itemType = itemType;
    }

    public void setDurationMin(Duration durationMin) {
        this.durationMin = durationMin;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return startTime.minusMinutes(durationMin.toMinutes());
    }

    public Duration getDurationMin() {
        return durationMin;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        String result = "Task{" +
                "id=" + id +
                ", name='" + name + '\'';
        if (description != null) {
            result = result + ", description.length()='" + description.length() + '\'';
        } else {
            result = result + ", description.length()='null'";
        }
        result = result + ", status='" + status + '\'' +
                '}';
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Task task = (Task) o;
        return id == task.id && name.equals(task.name) && Objects.equals(description, task.description) && status.equals(task.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, id, status);
    }
}
