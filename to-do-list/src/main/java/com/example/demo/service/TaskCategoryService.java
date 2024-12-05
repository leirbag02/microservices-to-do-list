package com.example.demo.service;

import com.example.demo.Excpetion.ObjectNotFound;
import com.example.demo.repository.TaskCategoryRepository;
import com.example.demo.model.TaskCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskCategoryService  {

    @Autowired
    TaskCategoryRepository repository;

    public List<TaskCategory> getRepository() {
        return repository.findAll();
    }

    public TaskCategory save(TaskCategory taskCategory) {
        return repository.save(taskCategory);
    }

    public Optional<TaskCategory> findById(Long id) {
        return Optional.ofNullable(repository.findById(id).orElseThrow(() -> new ObjectNotFound("Book not found")));
    }

    public TaskCategory findById(TaskCategory taskCategory) {
        return repository.findById(taskCategory.getId()).orElseThrow(() -> new ObjectNotFound("Book not found"));
    }

}
