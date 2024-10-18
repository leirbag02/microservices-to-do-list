package com.example.demo.dto;

import com.example.demo.model.Task;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TaskDTO {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private int priority;
    private Long category;
    private Long state;
    private Date donedate;
    private Date createddate;
    private Date updateddate;

    public TaskDTO(Task task ) {
        this.id = task.getId();
        this.userId = task.getClient().getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.priority = task.getPriority();
        this.category = task.getCategory().getId();
        this.state = task.getState().getId();
        this.donedate = task.getDonedate();
        this.createddate = task.getCreatedAt();
        this.updateddate = task.getUpdatedAt();
    }
}
