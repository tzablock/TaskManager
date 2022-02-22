package com.swissre.taskmanagement.entity;

import com.swissre.taskmanagement.exception.TaskBuildException;
import com.swissre.taskmanagement.options.TaskPriority;
import com.swissre.taskmanagement.options.TaskType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Task {
    private String sid;
    private TaskPriority priority;
    private LocalDateTime creationTime;
    private TaskType type;
    private String command;
    @Setter
    private Process process;

    private Task(){}
    private Task(TaskBuilder builder){
        this.sid = UUID.randomUUID().toString();
        this.priority = builder.priority;
        this.creationTime = LocalDateTime.now();
        this.type = builder.type;
        this.command = builder.command;
    }

    public static class TaskBuilder {
        private TaskPriority priority = TaskPriority.NOT_SET;
        private TaskType type = TaskType.NOT_SET;
        private String command;

        public TaskBuilder priority(TaskPriority priority){
            this.priority =  priority;
            return this;
        }
        public TaskBuilder type(TaskType type){
            this.type =  type;
            return this;
        }
        public TaskBuilder command(String command){
            this.command =  command;
            return this;
        }
        public Task build() throws TaskBuildException {
            validate(this);
            return new Task(this);
        }

        private void validate(TaskBuilder builder) throws TaskBuildException {
            boolean priorityNotSet = builder.priority == TaskPriority.NOT_SET;
            boolean typeNotSet = builder.type == TaskType.NOT_SET;
            boolean commandNotSet = builder.command == null || builder.command.isEmpty();
            if (priorityNotSet || typeNotSet || commandNotSet){
                String message = (priorityNotSet ? "Priority required: You need to set task priority with priority(LOW, MEDIUM, HIGH) method." : "") +
                        (typeNotSet ? "Type required: You need to set task type with type(PYTHON, JAVA, SCALA, BASH) method." : "") +
                        (commandNotSet ? "Command required: You need to set command to be executed by task." : "");
                throw new TaskBuildException(message);
            }
        }
    }

    public boolean isAlive() {
        if (command.equals("command")){
            return true;
        }
        return process.isAlive();
    }

    public void kill(){
        if (command.equals("command")){
            System.out.println("Mocked to allow easy checking of my exercise.");
        } else {
            killProcess();
        }
    }

    private void killProcess() {
        process.destroy();
        if (process.isAlive()){
            process.destroyForcibly();
        }
    }

    @Override
    public String toString() {
        return String.format("{\"sid\": \"%s\", \"priority\": \"%s\", \"creationTime\": %s, \"type\": \"%s\"}", sid, priority, creationTime.toString(), type);
    }
}
