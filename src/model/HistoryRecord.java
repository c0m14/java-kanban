package model;

import java.util.Objects;

public class HistoryRecord<T extends Task> {
    private final T item;
    private HistoryRecord prevRecord;
    private HistoryRecord nextRecord;

    public HistoryRecord(T item) {
        this.item = item;
        this.prevRecord = null;
        this.nextRecord = null;
    }

    public HistoryRecord getPrevRecord() {
        return prevRecord;
    }

    public void setPrevRecord(HistoryRecord prevRecord) {
        this.prevRecord = prevRecord;
    }

    public HistoryRecord getNextRecord() {
        return nextRecord;
    }

    public void setNextRecord(HistoryRecord nextRecord) {
        this.nextRecord = nextRecord;
    }

    public T getItem() {
        return item;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        if (this.prevRecord != null) {
            hash += prevRecord.hashCode();
        }
        hash *= 31;
        if (this.nextRecord != null) {
            hash += nextRecord.hashCode();
        }
        hash *= 47;
        if (this.item != null) {
            hash += item.hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;

        if (this == obj) return true;
        if (this.getClass() != obj.getClass()) return false;
        if (obj == null) return false;
        HistoryRecord otherRecord = (HistoryRecord) obj;
        if ((this.nextRecord == null && otherRecord.nextRecord != null) ||
                (this.nextRecord != null && otherRecord.nextRecord == null)) {
            return false;
        } else if ((this.prevRecord == null && otherRecord.prevRecord != null) ||
                (this.prevRecord != null && otherRecord.prevRecord == null)) {
            return false;
        } else if ((this.prevRecord != null && otherRecord.prevRecord != null) &&
                (this.prevRecord != null && otherRecord.prevRecord != null)) {
            return Objects.equals(this.nextRecord, otherRecord.nextRecord) &&
                    Objects.equals(this.prevRecord, otherRecord.prevRecord) &&
                    Objects.equals(this.item, otherRecord.item);
        } else {
            return Objects.equals(this.item, otherRecord.item);
        }
    }
}
