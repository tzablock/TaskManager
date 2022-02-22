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

class TaskQueueManagerRemoveOldestStrategyTest {
    TaskQueueManagerRemoveOldestStrategy out;

    @BeforeEach
    void createTestObject(){
        this.out = new TaskQueueManagerRemoveOldestStrategy();
    }

    @Test
    void checkIfAddTaskToQueueRemoveTheOldestTaskAndAddNewForFullQueueWhenMaxProcessesSetTo6() throws TaskBuildException, IOException, MaxProcessesException {
        List<Task> tasks = TaskCreatorTool.createNTasks(6, TaskPriority.LOW);
        tasks.forEach(out::addTaskToQueue);
        List<InitTask> additionalInitTask = TaskCreatorTool.createNInitTasks(1, "high");
        Task additionalTask = TaskCreatorTool.createNTasks(1, TaskPriority.LOW).get(0);

        out.maxProcesses = 6;
        out.verifyPlaceInQueue(additionalInitTask.get(0));
        out.addTaskToQueue(additionalTask);

        assertThat(out.getTasks()).doesNotContain(tasks.get(5));
        assertThat(out.getTasks()).contains(additionalTask);
    }
}