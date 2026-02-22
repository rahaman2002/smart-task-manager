package org.example.smarttaskmanager.service;

import lombok.RequiredArgsConstructor;
import org.example.smarttaskmanager.exception.ResourceNotFoundException;
import org.example.smarttaskmanager.model.Task;
import org.example.smarttaskmanager.model.User;
import org.example.smarttaskmanager.repository.TaskRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final RabbitTemplate rabbitTemplate;

    public Task createTask(Task task) {
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);
        rabbitTemplate.convertAndSend("task_notifications", "New Task: " + task.getTitle());
        return saved;
    }

    public Task updateTask(Task task) {
        task.setUpdatedAt(LocalDateTime.now());
        Task updated = taskRepository.save(task);
        rabbitTemplate.convertAndSend("task_notifications", "Task Updated: " + task.getTitle());
        return updated;
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public Page<Task> getTasksByUser(User user, int page, int size, String search, String status) {
        Pageable pageable = PageRequest.of(page, size);
        if (status != null && !status.equalsIgnoreCase("ALL")) {
            return taskRepository.findByAssignedToAndStatusAndTitleContainingIgnoreCase(
                    user, Task.Status.valueOf(status), search == null ? "" : search, pageable
            );
        } else {
            return taskRepository.findByAssignedToAndTitleContainingIgnoreCase(
                    user, search == null ? "" : search, pageable
            );
        }
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }

    @Cacheable("tasks")
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
}
