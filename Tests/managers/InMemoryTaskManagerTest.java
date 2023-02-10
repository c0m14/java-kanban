package managers;

import model.Epic;
import model.Subtask;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class InMemoryTaskManagerTest {

    static InMemoryTaskManager inMemoryTaskManage;
    static Epic epic;
    static Subtask subtask1;
    static Subtask subtask2;
    static Subtask subtask3WithOutStartDate;

    @BeforeAll
    public static void beforeAll() {
        inMemoryTaskManage = (InMemoryTaskManager) Managers.getDefault();
    }

    @BeforeEach
    public void beforeEach() {
        epic = new Epic(inMemoryTaskManage.getIdCounter(),
                "epicName");
        inMemoryTaskManage.createItem(epic);
        subtask1 = new Subtask(inMemoryTaskManage.getIdCounter(),
                "Test Subtask N1");
        inMemoryTaskManage.createItem(subtask1);
        epic.addSubtask(subtask1);
        subtask1.setEpicId(epic.getId());

        subtask2 = new Subtask(inMemoryTaskManage.getIdCounter(),
                "Test Subtask N2");
        inMemoryTaskManage.createItem(subtask2);
        epic.addSubtask(subtask2);
        subtask2.setEpicId(epic.getId());

        subtask3WithOutStartDate = new Subtask((inMemoryTaskManage.getIdCounter()),
                "Test Subtask N3 without startDate");
        inMemoryTaskManage.createItem(subtask3WithOutStartDate);
        epic.addSubtask(subtask3WithOutStartDate);
        subtask3WithOutStartDate.setEpicId(epic.getId());
    }

    //Тесты расчета startTime для Epic
    @Test
    public void shouldSetMinOfEpicSubtasksStartTime() {
        String startTimeForSubtask1 = "01-01-2023 12:00";
        String startTimeForSubtask2 = "03-02-2023 16:20";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        subtask1.setStartTime(LocalDateTime.parse(startTimeForSubtask1, formatter));
        subtask2.setStartTime(LocalDateTime.parse(startTimeForSubtask2, formatter));

        inMemoryTaskManage.updateEpicStartTimeAndDuration(epic.getId());

        Assertions.assertEquals(LocalDateTime.of(2023, 1, 1, 12, 0), epic.getStartTime());
    }

    @Test
    public void shouldReturnNullIfEpicSubtasksStartTimeIsNull() {
        inMemoryTaskManage.updateEpicStartTimeAndDuration(epic.getId());

        Assertions.assertNull(epic.getStartTime());
    }

    //Тесты расчета duration для Epic

    @Test
    public void shouldSetMaxOfEpicSubtasksDuration() {
        subtask1.setDurationMin(Duration.of(30, ChronoUnit.MINUTES));
        subtask2.setDurationMin(Duration.of(120, ChronoUnit.MINUTES));

        inMemoryTaskManage.updateEpicStartTimeAndDuration(epic.getId());

        Assertions.assertEquals(Duration.of(120, ChronoUnit.MINUTES), epic.getDurationMin());
    }

    @Test
    public void shouldReturnNullIfEpicSubtasksDurationIsNull() {
        inMemoryTaskManage.updateEpicStartTimeAndDuration(epic.getId());

        Assertions.assertNull(epic.getDurationMin());
    }

}
