package com.swissre.taskmanagement.verification;

import com.swissre.taskmanagement.entity.InitTask;
import com.swissre.taskmanagement.entity.Task;
import com.swissre.taskmanagement.exception.MaxProcessesException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;

@Component
@ConditionalOnProperty(
        value="processes.management.strategy",
        havingValue = "remove_oldest",
        matchIfMissing = false)
public class TaskQueueManagerRemoveOldestStrategy extends TaskQueueManager {
    @Override
    public void verifyPlaceInQueue(InitTask initTask) throws MaxProcessesException {
        this.queueFull = verifyIfQueueIsFull();
    }

    @Override
    public void addTaskToQueue(Task task) {
        if (queueFull){
            Task taskToRemove = Collections.max(this.tasks, Comparator.comparing(Task::getCreationTime));
            this.tasks.remove(taskToRemove);
        }
        this.tasks.add(task);
    }
}
