package com.swissre.taskmanagement.entity;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InitTask {
    private String priority;
    private String type;
    private String command;
}
