package com.swissre.taskmanagement.verification;

import com.swissre.taskmanagement.entity.InitTask;
import com.swissre.taskmanagement.entity.Task;
import com.swissre.taskmanagement.exception.MaxProcessesException;
import com.swissre.taskmanagement.exception.TaskBuildException;
import com.swissre.taskmanagement.options.TaskPriority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;


class TaskQueueManagerLessThanMaxStrategyTest {
    TaskQueueManagerLessThanMaxStrategy out;

    @BeforeEach
    void createTestObject(){
        this.out = new TaskQueueManagerLessThanMaxStrategy();
    }

    @Test
    void checkIfVerifyPlaceInQueueFailOnToManyTasksWhenMaxProcessesSetTo6() throws TaskBuildException, IOException, MaxProcessesException {
        List<Task> tasks = TaskCreatorTool.createNTasks(6, TaskPriority.LOW);
        InitTask additionalTask = TaskCreatorTool.createNInitTasks(1, "high").get(0);
        tasks.forEach(out::addTaskToQueue);

        try {
            out.maxProcesses = 6;
            out.verifyPlaceInQueue(additionalTask);
        } catch (MaxProcessesException e){
            assertThat(e.getMessage()).isEqualTo("Your Task can't be process as queue is full. Max amount of tasks: 6 for strategy base on maximum amount of tasks to be processed.");
        }
    }
}