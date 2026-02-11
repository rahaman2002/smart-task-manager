package com.example.smarttaskmanager.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String status; // TODO, IN_PROGRESS, DONE

    @ManyToOne
    private User assignedTo;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
