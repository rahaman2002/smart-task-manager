package org.example.smarttaskmanager.repository;

import org.example.smarttaskmanager.model.Task;
import org.example.smarttaskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByAssignedTo(User user);
    Page<Task> findByAssignedTo(User user, Pageable pageable);
    Page<Task> findByAssignedToAndTitleContainingIgnoreCase(User user, String title, Pageable pageable);

    Page<Task> findByAssignedToAndStatusAndTitleContainingIgnoreCase(User user, Task.Status status, String s, Pageable pageable);
}
