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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TaskQueueManagerRemoveLowestPriorityStrategyTest {
    TaskQueueManagerRemoveLowestPriorityStrategy out;

    @BeforeEach
    void createTestObject(){
        this.out = new TaskQueueManagerRemoveLowestPriorityStrategy();
    }

    @Test
    void checkIfVerifyPlaceInQueueFailWhenThereIsNoTaskWithLowerPriorityWhenMaxProcessesSetTo6() throws TaskBuildException, IOException, MaxProcessesException {
        List<Task> tasks = TaskCreatorTool.createNTasks(6, TaskPriority.HIGH);
        tasks.forEach(out::addTaskToQueue);
        InitTask additionalInitTask = TaskCreatorTool.createNInitTasks(1, "low").get(0);

        try {
            out.maxProcesses = 6;
            out.verifyPlaceInQueue(additionalInitTask);
        } catch (MaxProcessesException e){
            assertThat(e.getMessage()).isEqualTo("Your Task can't be process as queue is full and there is no task with lower priority. Max amount of tasks: 6 for strategy base on removing lower priority task.");
        }
    }

    @Test
    void checkIfAddTaskToQueueRemoveTheOldestTaskWithTheLowestPriorityAndAddNewOneWithHigherPriorityWhenMaxProcessesSetTo6() throws TaskBuildException, IOException, MaxProcessesException {
        List<Task> tasks = TaskCreatorTool.createNTasks(6, TaskPriority.LOW);
        tasks.forEach(out::addTaskToQueue);
        InitTask additionalInitTask = TaskCreatorTool.createNInitTasks(1, "high").get(0);
        Task additionalTask = TaskCreatorTool.createNTasks(1, TaskPriority.HIGH).get(0);

        out.maxProcesses = 6;
        out.verifyPlaceInQueue(additionalInitTask);
        out.addTaskToQueue(additionalTask);

        assertThat(out.getTasks()).doesNotContain(tasks.get(5));
        assertThat(out.getTasks()).contains(additionalTask);
    }
}