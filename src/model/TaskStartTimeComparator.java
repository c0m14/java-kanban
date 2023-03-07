package model;

import java.util.Comparator;

public class TaskStartTimeComparator implements Comparator<Task> {
    @Override
    public int compare(Task task1, Task task2) {
        if (task1.getStartTime() == null) {
            return 1;
        } else if (task2.getStartTime() == null) {
            return -1;
        } else {
            return task1.getStartTime().compareTo(task2.getStartTime());
        }
    }
}
