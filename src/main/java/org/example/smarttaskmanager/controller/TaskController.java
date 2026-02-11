package com.example.smarttaskmanager.controller;

import com.example.smarttaskmanager.model.Task;
import com.example.smarttaskmanager.model.User;
import com.example.smarttaskmanager.repository.UserRepository;
import com.example.smarttaskmanager.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final UserRepository userRepository;

    @PostMapping("/create")
    public Task createTask(@RequestBody Task task, Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).get();
        task.setAssignedTo(user);
        return taskService.createTask(task);
    }

    @GetMapping("/my-tasks")
    public List<Task> getMyTasks(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).get();
        return taskService.getTasksByUser(user);
    }

    @PutMapping("/update")
    public Task updateTask(@RequestBody Task task) {
        return taskService.updateTask(task);
    }

    @DeleteMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return "Task deleted";
    }

    @GetMapping("/all")
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }
}
