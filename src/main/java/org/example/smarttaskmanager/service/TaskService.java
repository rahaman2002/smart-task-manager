package org.example.smarttaskmanager.service;

import lombok.RequiredArgsConstructor;
import org.example.smarttaskmanager.model.Task;
import org.example.smarttaskmanager.model.User;
import org.example.smarttaskmanager.repository.TaskRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    public Page<Task> getTasksByUser(User user, int page, int size, String search) {
        if (search != null && !search.isEmpty()) {
            return taskRepository.findByAssignedToAndTitleContainingIgnoreCase(user, search, PageRequest.of(page, size));
        }
        return taskRepository.findByAssignedTo(user, PageRequest.of(page, size));
    }

    @Cacheable("tasks")
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
}
