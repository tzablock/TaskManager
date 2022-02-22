package com.swissre.taskmanagement.verification;

import com.swissre.taskmanagement.entity.InitTask;
import com.swissre.taskmanagement.entity.Task;
import com.swissre.taskmanagement.exception.MaxProcessesException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

abstract public class TaskQueueManager {
    @Getter
    protected List<Task> tasks;
    protected boolean queueFull;

    @Value("${processes.max:5}")
    protected int maxProcesses;

    public TaskQueueManager() {
        this.tasks = new ArrayList<>();
    }

    public abstract void verifyPlaceInQueue(InitTask initTask) throws MaxProcessesException;
    public abstract void addTaskToQueue(Task task);

    public List<String> removeFinishedTasks(){
        List<Task> aliveTasks = this.tasks.stream().filter(Task::isAlive).collect(toList());
        List<Task> removedTasks = this.tasks;
        removedTasks.removeAll(aliveTasks);
        this.tasks = aliveTasks;
        return removedTasks.stream().map(Task::getSid).collect(toList());
    }

    public List<String> removeAllTasks(){
        List<String> removedSids = tasks.stream().map(Task::getSid).collect(toList());
        tasks.forEach(Task::kill);
        this.tasks = new ArrayList<>();
        return removedSids;
    }

    public List<String> removeSelectedTasks(List<String> toRemoveSids){
        List<String> removedSids = new ArrayList<>();
        List<Task> leftTasks = new ArrayList<>();
        for (Task task: tasks){
            String currentTaskSid = task.getSid();
            if (toRemoveSids.contains(currentTaskSid)){
                task.kill();
                removedSids.add(currentTaskSid);
            } else {
                leftTasks.add(task);
            }
        }
        this.tasks = leftTasks;
        return removedSids;
    }

    protected boolean verifyIfQueueIsFull(){
        return this.tasks.size() >= maxProcesses;
    }
}
