package com.swissre.controller;

import com.swissre.taskmanagement.TaskManager;
import com.swissre.taskmanagement.entity.InitTask;
import com.swissre.taskmanagement.entity.Task;
import com.swissre.taskmanagement.exception.MaxProcessesException;
import com.swissre.taskmanagement.exception.TaskBuildException;
import com.swissre.taskmanagement.exception.TaskKillException;
import com.swissre.taskmanagement.exception.TaskSortException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.stream.Collectors.joining;

@RestController
public class TasksController {
    private TaskManager taskManager;

    @Autowired
    public TasksController(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @PostMapping("/addTask")
    public ResponseEntity addTask(InitTask initTask) {
        try {
            taskManager.removeFinishedTasks();
            Task task = taskManager.createTaskAndAssignedProcess(initTask);
            return ResponseEntity.ok(task.toString());
        } catch (TaskBuildException | MaxProcessesException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(e.getMessage());
        }
    }

    @GetMapping("/listRunningTasks")
    public ResponseEntity listRunningTasks(@RequestParam(value = "sort", defaultValue = "NOT_SET") String sort) throws TaskBuildException {
        try {
            taskManager.removeFinishedTasks();
            List<Task> tasks = taskManager.listAllActiveTasks(sort);
            return ResponseEntity.ok(String.format("[%s]", tasks.stream().map(Task::toString).collect(joining(","))));
        } catch (TaskSortException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(e.getMessage());
        }
    }


    @PostMapping("/killTasks")
    public ResponseEntity killTasks(@RequestParam(value = "sids", defaultValue = "") String sids) throws TaskBuildException {
        try {
            List<String> finishedSids = taskManager.removeFinishedTasks();
            List<String> removedSids = taskManager.killTasks(sids, finishedSids);
            return ResponseEntity.ok(String.format("[%s]", removedSids.stream().map(s -> "\"" + s + "\"").collect(joining(","))));
        } catch (TaskKillException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}