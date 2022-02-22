package com.swissre.taskmanagement.options;

public enum TaskPriority {
    NOT_SET(-1), LOW(0), MEDIUM(1), HIGH(2);
    public final int value;
    private TaskPriority(int value){
        this.value = value;
    }
}
