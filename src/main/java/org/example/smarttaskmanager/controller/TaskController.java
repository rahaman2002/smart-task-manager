package org.example.smarttaskmanager.controller;

import lombok.RequiredArgsConstructor;
import org.example.smarttaskmanager.model.Task;
import org.example.smarttaskmanager.model.User;
import org.example.smarttaskmanager.service.TaskService;
import org.example.smarttaskmanager.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;

    // ================= GET CURRENT USER =================
    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user");
        }

        // Since JWT subject = email
        String email = authentication.getName();

        return userService.getUserByEmail(email);
    }

    // ================= CREATE TASK =================
    @PostMapping
    public Task addTask(@RequestBody Task task) {

        User user = getCurrentUser();

        task.setAssignedTo(user);

        if (task.getStatus() == null) {
            task.setStatus(Task.Status.OPEN);
        }

        return taskService.createTask(task);
    }

    // ================= UPDATE TASK =================
    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id, @RequestBody Task task) {

        Task existingTask = taskService.getTaskById(id);
        User currentUser = getCurrentUser();

        // 🔒 Ensure task belongs to logged-in user
        if (!existingTask.getAssignedTo().getId()
                .equals(currentUser.getId())) {

            throw new RuntimeException("You are not allowed to update this task");
        }

        // Safe field updates
        if (task.getTitle() != null)
            existingTask.setTitle(task.getTitle());

        if (task.getDescription() != null)
            existingTask.setDescription(task.getDescription());

        if (task.getPriority() != null)
            existingTask.setPriority(task.getPriority());

        if (task.getDueDate() != null)
            existingTask.setDueDate(task.getDueDate());

        if (task.getStatus() != null)
            existingTask.setStatus(task.getStatus());

        return taskService.updateTask(existingTask);
    }

    // ================= DELETE TASK =================
    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {

        Task existingTask = taskService.getTaskById(id);
        User currentUser = getCurrentUser();

        // 🔒 Ensure task belongs to logged-in user
        if (!existingTask.getAssignedTo().getId()
                .equals(currentUser.getId())) {

            throw new RuntimeException("You are not allowed to delete this task");
        }

        taskService.deleteTask(id);
    }

    // ================= GET TASKS =================
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