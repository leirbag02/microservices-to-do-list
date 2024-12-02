package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "client_id")
    private User client;
    private String title;
    private String description;
    private int priority;
    @ManyToOne
    @JoinColumn(name = "state_id")
    private TaskState state;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private TaskCategory category;
    private Date donedate;
    private Date createdAt;
    private Date updatedAt;
}
