package com.example.smarttaskmanager.service;

import com.example.smarttaskmanager.model.Task;
import com.example.smarttaskmanager.model.User;
import com.example.smarttaskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
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
        Task savedTask = taskRepository.save(task);

        // Send notification
        rabbitTemplate.convertAndSend("task_notifications", "New Task Assigned: " + task.getTitle());
        return savedTask;
    }

    public List<Task> getTasksByUser(User user) {
        return taskRepository.findByAssignedTo(user);
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

    @Cacheable(value = "tasks")
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
}
