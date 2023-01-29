package model;

public enum Status {
    NEW,
    IN_PROGRESS,
    DONE;

    public static Status stringToStatus(String status) {
        switch (status) {
            case "NEW":
                return Status.NEW;
            case "IN_PROGRESS":
                return Status.IN_PROGRESS;
            case "DONE":
                return Status.DONE;
            default:
                return Status.NEW;
        }
    }
}
