package managers;

import model.HistoryRecord;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager<T extends Task> implements HistoryManager<T> {

    private final HistoryRecordsLinkedList historyList;
    private List<T> allItemsHistory;

    public InMemoryHistoryManager() {
        this.allItemsHistory = new ArrayList<>();
        this.historyList = new HistoryRecordsLinkedList();
    }

    @Override
    public void add(T anyItem) {
        if (anyItem != null) {
            Map<Integer, HistoryRecord<T>> taskIdMap = historyList.getTaskIdMap();

            if (taskIdMap.keySet() != null && taskIdMap.containsKey(anyItem.getId())) {
                HistoryRecord<T> oldHistoryRecord = taskIdMap.get(anyItem.getId());
                historyList.removeNode(oldHistoryRecord);
            }
            historyList.linkLast(anyItem);
        }
    }

    @Override
    public List<T> getHistory() {
        allItemsHistory = historyList.getItems();
        return allItemsHistory;
    }

    @Override
    public void remove(int anyItemId) {
        HistoryRecord<T> currRecord = (HistoryRecord<T>) historyList.getTaskIdMap().get(anyItemId);
        historyList.removeNode(currRecord);
    }

    private class HistoryRecordsLinkedList<T extends Task> {
        private HistoryRecord<T> head;
        private HistoryRecord<T> tail;
        private final Map<Integer, HistoryRecord<T>> taskIdMap;

        public HistoryRecordsLinkedList() {
            this.head = null;
            this.tail = null;
            this.taskIdMap = new HashMap<>();
        }

        public void linkLast(T item) {
            HistoryRecord<T> historyRecord = new HistoryRecord<>(item);

            if (head == null) {
                head = historyRecord;
            } else {
                tail.setNextRecord(historyRecord);
                historyRecord.setPrevRecord(tail);
            }
            tail = historyRecord;
            taskIdMap.put(item.getId(), historyRecord);
        }

        public List<T> getItems() {
            List<T> updatedAllItemsHistory = new ArrayList<>();
            HistoryRecord<T> currentRecord = historyList.head;

            while (currentRecord != null) {
                updatedAllItemsHistory.add(currentRecord.getItem());
                currentRecord = currentRecord.getNextRecord();
            }

            return updatedAllItemsHistory;
        }

        private void removeNode(HistoryRecord record) {
            if (taskIdMap.containsValue(record)) {
                if (record.equals(this.head) && !record.equals(this.tail)) {
                    record.getNextRecord().setPrevRecord(null);
                    this.head = record.getNextRecord();
                    record.setNextRecord(null);
                } else if (record.equals(this.tail) && !record.equals(this.head)) {
                    record.getPrevRecord().setNextRecord(null);
                    this.tail = record.getPrevRecord();
                    record.setPrevRecord(null);
                } else if (record.equals(this.head) && record.equals(this.tail)) {
                    this.head = null;
                    this.tail = null;
                } else {
                    record.getPrevRecord().setNextRecord(record.getNextRecord());
                    record.getNextRecord().setPrevRecord(record.getPrevRecord());
                    record.setPrevRecord(null);
                    record.setNextRecord(null);
                }
                taskIdMap.remove(record);
            }

        }

        public Map<Integer, HistoryRecord<T>> getTaskIdMap() {
            return taskIdMap;
        }

    }
}
