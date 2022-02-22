package com.swissre.taskmanagement.verification;

import com.swissre.taskmanagement.entity.InitTask;
import com.swissre.taskmanagement.entity.Task;
import com.swissre.taskmanagement.exception.MaxProcessesException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(
        value="processes.management.strategy",
        havingValue = "only_less_than_max",
        matchIfMissing = true)
public class TaskQueueManagerLessThanMaxStrategy extends TaskQueueManager {
    @Override
    public void verifyPlaceInQueue(InitTask initTask) throws MaxProcessesException {
        if (verifyIfQueueIsFull()){
            throw new MaxProcessesException(String.format("Your Task can't be process as queue is full. Max amount of tasks: %d for strategy base on maximum amount of tasks to be processed.", maxProcesses));
        }
    }

    @Override
    public void addTaskToQueue(Task task) {
        this.tasks.add(task);
    }
}
