package com.swissre.taskmanagement.verification;

import com.swissre.taskmanagement.entity.InitTask;
import com.swissre.taskmanagement.entity.Task;
import com.swissre.taskmanagement.exception.MaxProcessesException;
import com.swissre.taskmanagement.options.TaskPriority;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static java.util.Comparator.comparing;

@Component
@ConditionalOnProperty(
        value="processes.management.strategy",
        havingValue = "remove_lowest_priority",
        matchIfMissing = false)
public class TaskQueueManagerRemoveLowestPriorityStrategy extends TaskQueueManager {
    @Override
    public void verifyPlaceInQueue(InitTask initTask) throws MaxProcessesException {
        TaskPriority priority = TaskPriority.valueOf(initTask.getPriority().toUpperCase());
        this.queueFull = verifyIfQueueIsFull();
        boolean creationNotPossible = queueFull && noneTaskWithLowerPriority(priority);
        if (creationNotPossible){
            throw new MaxProcessesException(String.format("Your Task can't be process as queue is full and there is no task with lower priority. Max amount of tasks: %d for strategy base on removing lower priority task.", maxProcesses));
        }
    }

    @Override
    public void addTaskToQueue(Task task) {
        if (queueFull){
            Optional<Task> oldestLowerPriorityTask = this.tasks.stream()
                    .filter(t -> t.getPriority().value < task.getPriority().value)
                    .sorted(comparing(t -> t.getPriority().value))
                    .max(comparing(Task::getCreationTime));
            this.tasks.remove(oldestLowerPriorityTask.get());
        }
        this.tasks.add(task);
    }

    private boolean noneTaskWithLowerPriority(TaskPriority priority){
        return this.tasks.stream().noneMatch(t -> t.getPriority().value < priority.value);
    }
}
