package com.kafka.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.kafka.api.service.TaskService;
import com.kafka.model.http.TaskRequest;
import com.kafka.model.http.TaskResponse;

@RestController
public class TaskController {

  private TaskService taskService;

  @Autowired
  public TaskController(TaskService taskService) {
    this.taskService = taskService;
  }

  @PostMapping("/tasks")
  public ResponseEntity<?> addTask(@RequestBody TaskRequest taskRequest) throws Exception {
    TaskResponse response = taskService.saveTaskEntity(taskRequest.getCommand());
    if (response == null) {
      return new ResponseEntity<String>("Unable to save the task.", HttpStatusCode.valueOf(500));
    }
    return new ResponseEntity<TaskResponse>(response, HttpStatusCode.valueOf(200));
  }

  @GetMapping("/tasks")
  public ResponseEntity<?> getAllTasks() {
    List<TaskResponse> response = taskService.getAllTasks();
    if (response == null) {
      return new ResponseEntity<String>("Unexpected error happened.", HttpStatusCode.valueOf(500));
    }
    return new ResponseEntity<List<TaskResponse>>(response, HttpStatusCode.valueOf(200));
  }

  @GetMapping("/tasks/{taskId}")
  public ResponseEntity<?> getTasksById(@PathVariable UUID taskId) {
    TaskResponse response = taskService.getTaskById(taskId);
    if (response == null) {
      return new ResponseEntity<String>("Unexpected error happened.", HttpStatusCode.valueOf(500));
    }
    if (response.getId() == null) {
      return new ResponseEntity<String>(String.format("No task found with id: %s", taskId),
          HttpStatusCode.valueOf(400));
    }
    return new ResponseEntity<TaskResponse>(response, HttpStatusCode.valueOf(200));
  }

}
