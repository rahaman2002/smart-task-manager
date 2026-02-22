package org.example.smarttaskmanager.controller;

import lombok.RequiredArgsConstructor;
import org.example.smarttaskmanager.model.Task;
import org.example.smarttaskmanager.model.User;
import org.example.smarttaskmanager.service.TaskService;
import org.example.smarttaskmanager.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;

    // Get logged-in user from SecurityContext
    private User getCurrentUser() {
        // Step 1: get Spring Security user
        org.springframework.security.core.userdetails.User principal =
                (org.springframework.security.core.userdetails.User)
                        SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Step 2: fetch your real entity from DB
        return userService.getUserByUsername(principal.getUsername());
    }

    @PostMapping
    public Task addTask(@RequestBody Task task) {
        User user = getCurrentUser();
        task.setAssignedTo(user);
        if (task.getStatus() == null) {
            task.setStatus(Task.Status.OPEN); // default status
        }
        return taskService.createTask(task);
    }

    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id, @RequestBody Task task) {
        Task existingTask = taskService.getTaskById(id); // fetch current task from DB
        User currentUser = getCurrentUser();

        // Only allow updates if task belongs to current user
        if (!existingTask.getAssignedTo().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not allowed to update this task");
        }

        // Merge fields safely
        if (task.getTitle() != null) existingTask.setTitle(task.getTitle());
        if (task.getDescription() != null) existingTask.setDescription(task.getDescription());
        if (task.getPriority() != null) existingTask.setPriority(task.getPriority());
        if (task.getDueDate() != null) existingTask.setDueDate(task.getDueDate());
        if (task.getStatus() != null) existingTask.setStatus(task.getStatus());
        // assignedTo is NOT updated here, it stays the same

        return taskService.updateTask(existingTask);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }

    @GetMapping
    public Page<Task> getTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status
    ) {
        User user = getCurrentUser();
        return taskService.getTasksByUser(user, page, size, search, status);
    }
}