import java.util.Objects;

public class Task {
    protected int id;
    protected String name;
    protected String description;

    protected String status;

    public Task(int id, String name, String description) {
        this(id, name);
        this.description = description;
    }

    public Task(int id, String name) {
        this.name = name;
        this.id = id;
        this.description = "";
        this.status = "NEW";
    }

    @Override
    public String toString() {
        String result = "Task{" +
                        "name='" + name + '\'';
        if (description != null) {
            result = result + ", description.length()='" + description.length() + '\'';
        } else {
            result = result + ", description.length()='null'";
        }
        result = result + ", id=" + id +
                            ", status='" + status + '\'' +
                            '}';
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && name.equals(task.name) && Objects.equals(description, task.description) && status.equals(task.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, id, status);
    }
}
