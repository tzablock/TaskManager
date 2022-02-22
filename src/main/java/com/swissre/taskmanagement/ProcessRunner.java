package com.swissre.taskmanagement;

import com.swissre.taskmanagement.entity.Task;
import com.swissre.taskmanagement.exception.CommandRunException;
import com.swissre.taskmanagement.options.TaskType;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ProcessRunner {
    public Task run(Task task) throws CommandRunException {
        try {
            ProcessBuilder builder = new ProcessBuilder()
                    .command(chooseRunningAppPerSystem(task.getType()), task.getCommand());
            Process process = task.getType() != TaskType.TEST ? builder.start() : null;
            task.setProcess(process);
            return task;
        } catch (IOException e){
            throw new CommandRunException(String.format("Command %s can't be run, check if command is a correct %s command.", task.getCommand(), task.getType()));
        }
    }

    private String chooseRunningAppPerSystem(TaskType type){
        String runningApp = "";
        if (type == TaskType.BASH){
            runningApp = "bash";
        }
        if (type == TaskType.MACOS){
            runningApp = "osascript";
        }
        if (type == TaskType.WINDOWS){
            runningApp = "cmd.exe";
        }
        return runningApp;
    }
}
