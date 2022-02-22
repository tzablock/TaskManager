package com.swissre.taskmanagement;

import com.swissre.taskmanagement.entity.InitTask;
import com.swissre.taskmanagement.entity.Task;
import com.swissre.taskmanagement.exception.*;
import com.swissre.taskmanagement.options.SortField;
import com.swissre.taskmanagement.options.TaskPriority;
import com.swissre.taskmanagement.options.TaskType;
import com.swissre.taskmanagement.verification.TaskQueueManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;


@Service
public class TaskManager {
    private final ProcessRunner processRunner;
    private final TaskQueueManager taskQueueManager;

    @Autowired
    public TaskManager(ProcessRunner processRunner, TaskQueueManager creationVerifier) {
        this.processRunner = processRunner;
        this.taskQueueManager = creationVerifier;
    }

    public Task createTaskAndAssignedProcess(InitTask initTask) throws TaskBuildException, MaxProcessesException {
        Task task;
        try {
            taskQueueManager.verifyPlaceInQueue(initTask);
            task = transformInitIntoFinalTask(initTask);
            task = processRunner.run(task);
        } catch (IllegalArgumentException e ){
            throw new TaskBuildException("Create Task require object containing: priority(LOW, MEDIUM, HIGH) and type(BASH, WINDOWS)");
        } catch (CommandRunException e){
            throw new TaskBuildException(e.getMessage());
        }
        taskQueueManager.addTaskToQueue(task);
        return task;
    }


    public List<Task> listAllActiveTasks(String sort) throws TaskSortException {
        try {
            Stream<Task> tasksStream = taskQueueManager.getTasks().stream();
            tasksStream = sortAccordingToChosenField(tasksStream, sort);
            return tasksStream.collect(toList());
        }catch (IllegalArgumentException e){
            throw new TaskSortException(String.format("Sorting by %s,allowed fields to sort are sid, creationTime and priority or no value.", sort));
        }
    }


    public List<String> killTasks(String sids, List<String> finishedSids) throws TaskKillException {
        List<String> removedSids;
        List<String> sepSids = new ArrayList<>();
        if (noSidsSelected(sids)){
            removedSids = taskQueueManager.removeAllTasks();
        } else {
            sepSids = separateSids(sids);
            removedSids = taskQueueManager.removeSelectedTasks(sepSids);
            filterOutRemovedSids(sepSids, finishedSids, removedSids);
        }
        if (anySIDsNotFound(sepSids)){
            failOnNoExistingSIDs(sepSids, removedSids);
        }
        return removedSids;
    }

    public List<String> removeFinishedTasks() {
        return taskQueueManager.removeFinishedTasks();
    }

    private Stream<Task> sortAccordingToChosenField(Stream<Task> tasksStream, String sort) {
        SortField sortField = SortField.valueOf(sort.toUpperCase());
        if (sortField == SortField.SID){
            tasksStream = tasksStream.sorted(comparing(Task::getSid));
        }
        if (sortField == SortField.PRIORITY){
            tasksStream = tasksStream.sorted(comparing((Task t) -> t.getPriority().value).reversed());
        }
        if (sortField == SortField.CREATIONTIME){
            tasksStream = tasksStream.sorted(comparing(Task::getCreationTime));
        }
        return tasksStream;
    }

    private Task transformInitIntoFinalTask(InitTask initTask) throws TaskBuildException {
        TaskPriority taskPriority = TaskPriority.valueOf(initTask.getPriority().toUpperCase());
        TaskType taskType = TaskType.valueOf(initTask.getType().toUpperCase());
        return new Task.TaskBuilder().priority(taskPriority).type(taskType).command(initTask.getCommand()).build();
    }

    private boolean noSidsSelected(String sids) {
        return sids.isEmpty();
    }

    private List<String> separateSids(String sids){
        List<String> sepSids = new ArrayList<>();
        if (sids.contains(",")){
            Collections.addAll(sepSids, sids.split(","));
        } else {
            sepSids.add(sids);
        }
        return sepSids;
    }

    private void filterOutRemovedSids(List<String> sepSids, List<String> finishedSids, List<String> removedSids) {
        finishedSids = sepSids.stream().filter(finishedSids::contains).collect(toList());
        sepSids.removeAll(finishedSids);
        sepSids.removeAll(removedSids);
    }

    private boolean anySIDsNotFound(List<String> sepSids) {
        return !sepSids.isEmpty();
    }

    private void failOnNoExistingSIDs(List<String> sepSids, List<String> removedSids) throws TaskKillException {
        throw new TaskKillException(
                String.format("Tasks with sids %s not exist! ",
                        String.join(",", sepSids))
                        +
                        (removedSids.isEmpty() ? "":
                                String.format("Tasks for sids %s were removed.",
                                        String.join(",", removedSids))));
    }
}
