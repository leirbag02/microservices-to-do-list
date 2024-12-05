package com.example.demo.service;

import com.example.demo.repository.TaskRepository;
import com.example.demo.repository.TaskStateRepository;
import com.example.demo.model.Task;
import com.example.demo.model.TaskState;
import com.example.demo.model.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
public class TaskStateService {

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    TaskStateRepository taskStateRepository;

    public void updateTaskState() {
        List<Task> tasks = taskRepository.findAll();
        LocalDate today = LocalDate.now();
        TaskState lateStatus = taskStateRepository.findByStatus(TaskStatus.LATE.name());
        for (Task task : tasks) {
            if (task.getDonedate() != null) {
                LocalDate validatyDate = convertToLocalDate(task.getDonedate());
                if (validatyDate != null && validatyDate.isBefore(today)) {
                    if (task.getState() != null &&
                            !task.getState().getTaskStatus().equals(TaskStatus.LATE.name())) {
                        if (lateStatus != null) {
                            task.setState(lateStatus);
                            taskRepository.save(task);
                        }
                    }
                }
            }
        }
    }

    private LocalDate convertToLocalDate(Date dateToConvert) {
        if (dateToConvert == null) {
            return null;
        }
        return new java.sql.Date(dateToConvert.getTime()).toLocalDate();
    }
}
