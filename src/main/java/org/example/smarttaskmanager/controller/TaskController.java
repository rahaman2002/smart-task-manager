package org.example.smarttaskmanager.controller;

import lombok.RequiredArgsConstructor;
import org.example.smarttaskmanager.model.Task;
import org.example.smarttaskmanager.model.User;
import org.example.smarttaskmanager.service.TaskService;
import org.example.smarttaskmanager.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final UserService userService; // fetch logged-in user

    @PostMapping
    public Task addTask(@RequestBody Task task, @RequestHeader("Authorization") String token) {
        User user = userService.getUserFromToken(token);
        task.setAssignedTo(user);
        return taskService.createTask(task);
    }

    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id, @RequestBody Task task, @RequestHeader("Authorization") String token) {
        task.setId(id);
        return taskService.updateTask(task);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }

    @GetMapping
    public Page<Task> getTasks(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        User user = userService.getUserFromToken(token);
        return taskService.getTasksByUser(user, page, size, search);
    }

    @GetMapping("/session")
    public User getSession(@RequestHeader("Authorization") String token) {
        return userService.getUserFromToken(token); // returns username, lastLogin etc
    }
}
