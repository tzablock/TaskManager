package com.swissre.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
public class TasksControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    public void cleanState() throws Exception {
        cleanTasks();
    }

    @Disabled
    @Test
    public void checkIfAddTaskAddOneCorrectTask() throws Exception {
        this.mockMvc.perform(post("/addTask")
                             .param("priority", "high")
                             .param("type", "bash")
                             .param("command", "command"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(stringContainsInOrder("{\"sid\": \"", "\", \"priority\": \"HIGH\", \"creationTime\": ", ", \"type\": \"BASH\"}")));
    }

    @Disabled
    @Test
    public void checkIfListRunningTasksWillReturnThreeFreshlyAddedTasks() throws Exception {
        String returnedJsonTask1 = createTask("high", "test", "command");
        String returnedJsonTask2 = createTask("low", "test", "command");
        String returnedJsonTask3 = createTask("low", "test", "command");

        this.mockMvc.perform(get("/listRunningTasks"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(String.format("[%s,%s,%s]", returnedJsonTask1, returnedJsonTask2, returnedJsonTask3))));
    }

    @Disabled
    @Test
    public void checkIfListRunningTasksWillReturnThreeFreshlyAddedTasksSortedByCreationTime() throws Exception {
        String returnedJsonTask1 = createTask("high", "test", "command");
        String returnedJsonTask2 = createTask("low", "test", "command");
        String returnedJsonTask3 = createTask("low", "test", "command");

        this.mockMvc.perform(get("/listRunningTasks?sort=CreationTime"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(String.format("[%s,%s,%s]", returnedJsonTask1, returnedJsonTask2, returnedJsonTask3))));
    }

    @Disabled
    @Test
    public void checkIfListRunningTasksWillReturnThreeFreshlyAddedTasksSortedByPriority() throws Exception {
        String returnedJsonTask1 = createTask("medium", "test", "command");
        String returnedJsonTask2 = createTask("high", "test", "command");
        String returnedJsonTask3 = createTask("low", "test", "command");

        this.mockMvc.perform(get("/listRunningTasks?sort=Priority"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(String.format("[%s,%s,%s]", returnedJsonTask2, returnedJsonTask1, returnedJsonTask3))));
    }

    @Disabled
    @Test
    public void checkIfKillTasksWillKillAllThreeTasksWhenSIDsNotProvided() throws Exception {
        String sid1 = getSIDFromJson(createTask("medium", "test", "command"));
        String sid2 = getSIDFromJson(createTask("high", "test", "command"));
        String sid3 = getSIDFromJson(createTask("low", "test", "command"));

        this.mockMvc.perform(post("/killTasks"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(String.format("[\"%s\",\"%s\",\"%s\"]", sid1, sid2, sid3))));
    }

    @Disabled
    @Test
    public void checkIfKillTasksWillKillOnlyOneTaskWhenSIDProvided() throws Exception {
        String sid1 = getSIDFromJson(createTask("medium", "test", "command"));
        getSIDFromJson(createTask("high", "test", "command"));
        getSIDFromJson(createTask("low", "test", "command"));

        this.mockMvc.perform(post(String.format("/killTasks?sids=%s", sid1)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(String.format("[\"%s\"]", sid1))));
    }

    @Disabled
    @Test
    public void checkIfKillTasksWillKillOnlyTwoTasksWhenSIDProvided() throws Exception {
        String sid1 = getSIDFromJson(createTask("medium", "test", "command"));
        String sid2 = getSIDFromJson(createTask("high", "test", "command"));
        getSIDFromJson(createTask("low", "test", "command"));

        this.mockMvc.perform(post(String.format("/killTasks?sids=%s,%s", sid1, sid2)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(String.format("[\"%s\",\"%s\"]", sid1, sid2))));
    }

    private String createTask(String priority, String type, String command) throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(post("/addTask")
                .param("priority", priority)
                .param("type", type)
                .param("command", command)).andReturn();
        return mvcResult.getResponse().getContentAsString();
    }

    private void cleanTasks() throws Exception {
        this.mockMvc.perform(post("/killTasks"));
    }

    private String getSIDFromJson(String taskJson){
        return taskJson.split("\"sid\": \"")[1].split("\", \"priority\"")[0];
        //"sid": "ecdb2d0f-12d0-4b0b-93c0-2cb7b19779fe", "priority"
    }
}