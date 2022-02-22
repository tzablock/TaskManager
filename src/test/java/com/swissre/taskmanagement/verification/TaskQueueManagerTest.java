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

class TaskQueueManagerTest {
    private TaskQueueManager out;

    @BeforeEach
    void createTestObject(){
        this.out = new TaskQueueManager() {
            @Override
            public void verifyPlaceInQueue(InitTask initTask) throws MaxProcessesException {}

            @Override
            public void addTaskToQueue(Task task) {
                this.tasks.add(task);
            }
        };
    }

    @Test
    void checkIfRemoveAllTasksRemoveAllTasksFromQueue() throws TaskBuildException, IOException {
        List<Task> tasks = TaskCreatorTool.createNTasks(3, TaskPriority.LOW);
        tasks.forEach(out::addTaskToQueue);

        List<String> results = out.removeAllTasks();

        assertThat(results).containsAll(tasks.stream().map(Task::getSid).collect(toList()));
    }

    @Test
    void removeSelectedTasks() throws IOException, TaskBuildException {
        List<Task> tasks = TaskCreatorTool.createNTasks(3, TaskPriority.LOW);
        tasks.forEach(out::addTaskToQueue);
        List<String> removedSids = tasks.subList(0,2).stream().map(Task::getSid).collect(toList());

        List<String> results = out.removeSelectedTasks(removedSids);

        assertThat(results).containsAll(removedSids);
    }
}