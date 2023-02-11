package managers;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class InMemoryTaskManagerTest {

    static InMemoryTaskManager inMemoryTaskManage;
    static Epic epic;
    static Subtask subtask1;
    static Subtask subtask2;
    static Subtask subtask3WithOutStartDate;
    static DateTimeFormatter formatter;

    @BeforeAll
    public static void beforeAll() {
        formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    }

    @BeforeEach
    public void beforeEach() {
        inMemoryTaskManage = (InMemoryTaskManager) Managers.getDefault();
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
        subtask1.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        subtask2.setStartTime(LocalDateTime.parse("03-02-2023 16:20", formatter));

        inMemoryTaskManage.updateEpicStartTimeDurationEndTime(epic.getId());

        Assertions.assertEquals(LocalDateTime.of(2023, 1, 1, 12, 0),
                epic.getStartTime(),
                "startTime неверно для Epic");
    }

    @Test
    public void shouldReturnNullIfEpicSubtasksStartTimeIsNull() {
        inMemoryTaskManage.updateEpicStartTimeDurationEndTime(epic.getId());

        Assertions.assertNull(epic.getStartTime(),
                "startTime не null для Epic");
    }

    //Тесты расчета duration для Epic
    @Test
    public void shouldSetMaxOfEpicSubtasksDuration() {
        subtask1.setDurationMinutes(Duration.of(30, ChronoUnit.MINUTES));
        subtask2.setDurationMinutes(Duration.of(120, ChronoUnit.MINUTES));

        inMemoryTaskManage.updateEpicStartTimeDurationEndTime(epic.getId());

        Assertions.assertEquals(Duration.of(150, ChronoUnit.MINUTES),
                epic.getDurationMinutes(),
                "Неверно расчитано duration для Epic");
    }

    @Test
    public void shouldReturnZeroIfEpicSubtasksDurationIsNull() {
        inMemoryTaskManage.updateEpicStartTimeDurationEndTime(epic.getId());

        Assertions.assertEquals(0,
                epic.getDurationMinutes().toMinutes(),
                "duration не равно 0");
    }

    //Тесты для расчета endTime для Epic
    @Test
    public void shouldSetCorrectEndTimeForEpic() {
        subtask1.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        subtask2.setStartTime(LocalDateTime.parse("03-02-2023 16:20", formatter));
        subtask1.setDurationMinutes(Duration.of(30, ChronoUnit.MINUTES));
        subtask2.setDurationMinutes(Duration.of(120, ChronoUnit.MINUTES));

        inMemoryTaskManage.updateEpicStartTimeDurationEndTime(epic.getId());

        Assertions.assertEquals(LocalDateTime.parse("03-02-2023 18:20", formatter),
                epic.getEndTime().get(),
                "Неверно расчитан endTime для Epic");
    }

    @Test
    public void shouldSetCorrectEntTimeIfDurationIsAbsentInSubtask() {
        subtask1.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        subtask2.setStartTime(LocalDateTime.parse("03-02-2023 16:20", formatter));
        subtask1.setDurationMinutes(Duration.of(30, ChronoUnit.MINUTES));


        inMemoryTaskManage.updateEpicStartTimeDurationEndTime(epic.getId());

        Assertions.assertEquals(LocalDateTime.parse("01-01-2023 12:30", formatter),
                epic.getEndTime().get(),
                "Неверно расчитан endTime для Epic");
    }

    @Test
    public void shouldSetCorrectEntTimeIfStartTimeIsAbsentInSubtask() {
        subtask1.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        subtask1.setDurationMinutes(Duration.of(30, ChronoUnit.MINUTES));
        subtask2.setDurationMinutes(Duration.of(120, ChronoUnit.MINUTES));

        inMemoryTaskManage.updateEpicStartTimeDurationEndTime(epic.getId());

        Assertions.assertEquals(LocalDateTime.parse("01-01-2023 12:30", formatter),
                epic.getEndTime().get(),
                "Неверно расcчитан endTime для Epic");
    }

    //Тесты приоритезации задач

    @Test
    public void shouldReturnRightOrderByStartTimeASC() {
        subtask1.setStartTime(LocalDateTime.parse("01-01-2023 12:00", formatter));
        subtask2.setStartTime(LocalDateTime.parse("03-02-2023 16:20", formatter));

        List<Task> prioritizedTasks = inMemoryTaskManage.getPrioritizedTasks();

        Assertions.assertEquals(prioritizedTasks.get(0), subtask1);
        Assertions.assertEquals(prioritizedTasks.get(1), subtask2);
        Assertions.assertEquals(prioritizedTasks.get(2), subtask3WithOutStartDate);
    }

}
