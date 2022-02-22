package com.swissre.taskmanagement;

import com.swissre.taskmanagement.entity.InitTask;
import com.swissre.taskmanagement.entity.Task;
import com.swissre.taskmanagement.exception.*;
import com.swissre.taskmanagement.options.TaskPriority;
import com.swissre.taskmanagement.options.TaskType;
import com.swissre.taskmanagement.verification.TaskQueueManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;



class TaskManagerTest {
    private static TaskManager out;
    private TaskQueueManager taskQueueManagerMock;

    @BeforeEach
    void initTests() throws CommandRunException, MaxProcessesException, TaskBuildException {
        ProcessRunner runnerMock = mockCorrectCommandProcessRunner();
        List<Task> tasks = createThreeExampleTasks();
        TaskQueueManager taskQueueManagerMock = mockCorrectTaskQueueManager(tasks);
        this.taskQueueManagerMock = taskQueueManagerMock;
        out = new TaskManager(runnerMock, taskQueueManagerMock);
    }


    @Test
    void checkIfCreateTaskAndRunAssignedProcessFailForNotCorrectTaskPriority() {
        InitTask in = new InitTask();
        in.setCommand("ls .");
        in.setPriority("veryhigh");
        in.setType("bash");

        try {
            out.createTaskAndAssignedProcess(in);
        } catch (TaskBuildException | MaxProcessesException e) {
            assertThat(e.getMessage()).isEqualTo("Create Task require object containing: priority(LOW, MEDIUM, HIGH) and type(BASH, WINDOWS)");
        }
    }

    @Test
    void checkIfCreateTaskAndRunAssignedProcessFailForNotCorrectTaskType() {
        InitTask in = new InitTask();
        in.setCommand("ls .");
        in.setPriority("high");
        in.setType("android");

        try {
            out.createTaskAndAssignedProcess(in);
        } catch (TaskBuildException | MaxProcessesException e) {
            assertThat(e.getMessage()).isEqualTo("Create Task require object containing: priority(LOW, MEDIUM, HIGH) and type(BASH, WINDOWS)");
        }
    }

    @Test
    void checkIfCreateTaskAndRunAssignedProcessCreateCorrectTask() throws TaskBuildException, CommandRunException, MaxProcessesException {
        InitTask in = new InitTask();
        LocalDateTime currentDateTime = LocalDateTime.now();
        String command = "ls .";
        in.setCommand(command);
        in.setPriority("high");
        in.setType("bash");

        mockCorrectCommandProcessRunner();
        Task result = out.createTaskAndAssignedProcess(in);

        assertThat(result.getCommand()).isEqualTo(command);
        assertThat(result.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(result.getType()).isEqualTo(TaskType.BASH);
        assertThat(result.getCreationTime()).isAfter(currentDateTime);
        assertThat(result.getSid()).isNotEmpty();
    }

    @Test
    void checkIfListAllActiveTasksListAllNotSortedActiveTasks() throws TaskBuildException, CommandRunException, TaskSortException, MaxProcessesException {
        mockCorrectCommandProcessRunner();

        List<Task> results = out.listAllActiveTasks("NOT_SET");
        assertThat(results).hasSize(3);
    }

    @Test
    void checkIfListAllActiveTasksListAllSortedBySidActiveTasks() throws TaskBuildException, CommandRunException, TaskSortException, MaxProcessesException {
        mockCorrectCommandProcessRunner();

        List<Task> results = out.listAllActiveTasks("Sid");

        assertThat(results).hasSize(3);
        assertThat(results.get(0).getSid()).isLessThan(results.get(1).getSid());
        assertThat(results.get(1).getSid()).isLessThan(results.get(2).getSid());
    }

    @Test
    void checkIfListAllActiveTasksListAllSortedByCreationTimeActiveTasks() throws TaskBuildException, CommandRunException, TaskSortException, MaxProcessesException {
        mockCorrectCommandProcessRunner();

        List<Task> results = out.listAllActiveTasks("CreationTime");

        assertThat(results).hasSize(3);
        assertThat(results.get(0).getCreationTime()).isBefore(results.get(1).getCreationTime());
        assertThat(results.get(1).getCreationTime()).isBefore(results.get(2).getCreationTime());
    }

    @Test
    void checkIfListAllActiveTasksListAllSortedByPriorityActiveTasks() throws TaskBuildException, CommandRunException, TaskSortException, MaxProcessesException {
        mockCorrectCommandProcessRunner();

        List<Task> results = out.listAllActiveTasks("Priority");

        assertThat(results).hasSize(3);
        assertThat(results.get(0).getPriority().value).isGreaterThan(results.get(1).getPriority().value);
        assertThat(results.get(1).getPriority().value).isGreaterThan(results.get(2).getPriority().value);
    }

    @Test
    void checkIfListAllActiveTasksListAllSortedByNotCorrectValueWillThrowExeption() throws TaskBuildException, CommandRunException, TaskSortException, MaxProcessesException {
        mockCorrectCommandProcessRunner();

        String notCorrectSortField = "Some Other Field";
        try {
            out.listAllActiveTasks(notCorrectSortField);
        } catch (TaskSortException e){
            assertThat(e.getMessage()).isEqualTo(String.format("Sorting by %s,allowed fields to sort are sid, creationTime and priority or no value.", notCorrectSortField));
        }
    }

    @Test
    void checkIfKillTasksKillAllTasksWhenSidsAreEmpty() throws TaskBuildException, CommandRunException, TaskKillException, MaxProcessesException, TaskSortException {
        mockCorrectCommandProcessRunner();
        List<Task> tasks = out.listAllActiveTasks("NOT_SET");
        List<String> removedSids = tasks.stream().map(Task::getSid).collect(toList());
        mockTaskRemoval(removedSids);

        List<String> results = out.killTasks("", Collections.emptyList());

        assertThat(results).hasSize(3);
        assertThat(results).containsAll(removedSids);
    }

    @Test
    void checkIfKillTasksKillTaskWithProvidedSid() throws TaskBuildException, CommandRunException, TaskKillException, MaxProcessesException, TaskSortException {
        mockCorrectCommandProcessRunner();
        List<Task> tasks = out.listAllActiveTasks("NOT_SET");
        String sidRemovedTask = tasks.get(0).getSid();
        mockTaskRemoval(Collections.singletonList(sidRemovedTask));

        List<String> results = out.killTasks(sidRemovedTask, Collections.emptyList());

        assertThat(results).hasSize(1);
        assertThat(results).contains(sidRemovedTask);
    }

    @Test
    void checkIfKillTasksKillTasksWithProvidedSids() throws TaskBuildException, CommandRunException, TaskKillException, MaxProcessesException, TaskSortException {
        mockCorrectCommandProcessRunner();
        List<Task> tasks = out.listAllActiveTasks("NOT_SET");
        List<String> sidsToRemove = new ArrayList<>();
        sidsToRemove.add(tasks.get(0).getSid());
        sidsToRemove.add(tasks.get(1).getSid());
        mockTaskRemoval(sidsToRemove);

        List<String> results = out.killTasks(String.join(",",sidsToRemove), Collections.emptyList());

        assertThat(results).hasSize(2);
        assertThat(results).containsAll(sidsToRemove);
    }

    @Test
    void checkIfKillTasksTrowExceptionWhenProvidedOnlyNotExistingSids() throws TaskBuildException, CommandRunException, TaskKillException, MaxProcessesException {
        mockCorrectCommandProcessRunner();
        createThreeExampleTasks();
        String notExistingSid = "hdjsdhdshsdhjsdjhds, kdkdkdkdkdddd";
        try {
            out.killTasks(notExistingSid, Collections.emptyList());
        } catch (TaskKillException e){
            assertThat(e.getMessage()).isEqualTo(String.format("Tasks with sids %s not exist! ", notExistingSid));
        }

    }

    @Test
    void checkIfKillTasksTrowExceptionWhenProvidedNotExistingAndExistingSids() throws TaskBuildException, CommandRunException, TaskKillException, MaxProcessesException {
        mockCorrectCommandProcessRunner();
        List<Task> tasks = createThreeExampleTasks();
        String notExistingSid = "hdjsdhdshsdhjsdjhds, kdkdkdkdkdddd";
        String existingSid = tasks.get(0).getSid();
        mockTaskRemoval(Collections.singletonList(existingSid));

        try {
            out.killTasks(notExistingSid+","+existingSid, Collections.emptyList());
        } catch (TaskKillException e){
            assertThat(e.getMessage()).isEqualTo(String.format("Tasks with sids %s not exist! Tasks for sids %s were removed.", notExistingSid, existingSid));
        }

    }

    private List<Task> createThreeExampleTasks() throws TaskBuildException, MaxProcessesException {
        String command = "ls .";
        Task task1 = new Task.TaskBuilder().priority(TaskPriority.HIGH).type(TaskType.BASH).command(command).build();
        Task task2 = new Task.TaskBuilder().priority(TaskPriority.MEDIUM).type(TaskType.BASH).command(command).build();
        Task task3 = new Task.TaskBuilder().priority(TaskPriority.LOW).type(TaskType.BASH).command(command).build();

        Process process = mock(Process.class);
        doNothing().when(process).destroy();
        when(mock(Process.class).isAlive()).thenReturn(false);
        task1.setProcess(process);
        task2.setProcess(process);
        task3.setProcess(process);
        return Arrays.asList(task1, task2, task3);
    }

    private ProcessRunner mockCorrectCommandProcessRunner() throws CommandRunException {
        ProcessRunner runnerMock = mock(ProcessRunner.class);
        when(runnerMock.run(any(Task.class))).thenAnswer((Answer<Task>) invocation -> {
            Object[] args = invocation.getArguments();
            return (Task) args[0];
        });
        return runnerMock;
    }

    private TaskQueueManager mockCorrectTaskQueueManager(List<Task> tasks) throws MaxProcessesException {
        TaskQueueManager taskQueueManagerMock = mock(TaskQueueManager.class);
        doNothing().when(taskQueueManagerMock).verifyPlaceInQueue(any());
        doNothing().when(taskQueueManagerMock).addTaskToQueue(any());
        doNothing().when(taskQueueManagerMock).verifyPlaceInQueue(any());
        doNothing().when(taskQueueManagerMock).addTaskToQueue(any());
        when(taskQueueManagerMock.removeFinishedTasks()).thenReturn(Collections.emptyList());
        when(taskQueueManagerMock.getTasks()).thenReturn(tasks);

        return taskQueueManagerMock;
    }

    private void mockTaskRemoval(List<String> sidsToRemove){
        when(taskQueueManagerMock.removeSelectedTasks(anyList())).thenReturn(sidsToRemove);
        when(taskQueueManagerMock.removeAllTasks()).thenReturn(sidsToRemove);
    }

}