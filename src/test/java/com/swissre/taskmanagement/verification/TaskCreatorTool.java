package com.swissre.taskmanagement.verification;

import com.swissre.taskmanagement.entity.InitTask;
import com.swissre.taskmanagement.entity.Task;
import com.swissre.taskmanagement.exception.TaskBuildException;
import com.swissre.taskmanagement.options.TaskPriority;
import com.swissre.taskmanagement.options.TaskType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class TaskCreatorTool {
    static List<InitTask> createNInitTasks(int n, String priority){
        List<InitTask> tasks = new ArrayList<>();

        while (n > 0){
            InitTask task = new InitTask();
            task.setPriority(priority);
            task.setCommand("ls .");
            task.setType("bash");
            tasks.add(task);
            n--;
        }
        return tasks;
    }

    static List<Task> createNTasks(int n, TaskPriority priority) throws IOException, TaskBuildException {
        List<Task> tasks = new ArrayList<>();

        while (n > 0){
            Task task = new Task.TaskBuilder().priority(priority).type(TaskType.BASH).command("ls .").build();
            task.setProcess(createDeathProcess(task));
            tasks.add(task);
            n--;
        }
        return tasks;
    }

    static Process createDeathProcess(Task task) throws IOException {
        return new ProcessBuilder()
                .command("test", task.getCommand()).start();
    }
}
