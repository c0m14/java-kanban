package model;

public enum ItemType {
    TASK,
    SUBTASK,
    EPIC;

    public static ItemType stringToItemType(String itemType) {
        switch (itemType) {
            case "TASK":
                return ItemType.TASK;
            case "SUBTASK":
                return ItemType.SUBTASK;
            case "EPIC":
                return ItemType.EPIC;
            default:
                return null;
        }
    }
}
