package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class Task {
    protected int id;
    protected String name;
    protected String description;
    protected Status status;
    protected ItemType itemType;
    protected Duration durationMinutes;
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
                String description,
                Status status,
                ItemType itemType) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.itemType = itemType;
    }

    public Task(int id,
                String name,
                String description,
                Status status,
                ItemType itemType,
                Duration durationMinutes,
                LocalDateTime startTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.itemType = itemType;
        this.durationMinutes = durationMinutes;
        this.startTime = startTime;
    }

    public void setDurationMinutes(Duration durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Optional<LocalDateTime> getEndTime() {
        if (this.startTime != null && this.durationMinutes != null) {
            return Optional.of(startTime.plusMinutes(durationMinutes.toMinutes()));
        } else {
            return Optional.empty();
        }
    }

    public Duration getDurationMinutes() {
        return durationMinutes;
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
