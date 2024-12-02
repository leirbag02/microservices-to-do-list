package com.example.demo.controller;
import com.example.demo.dto.TaskDTO;
import com.example.demo.model.Task;
import com.example.demo.service.TaskService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("api/{userId}/task")
public class TaskController {
    @Autowired
    TaskService taskService;

    @Autowired
    UserService userService;

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getAllOpenOrLateTasks(
            @PathVariable long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
        Page<Task> tasksPage = taskService.findAllOpenOrLate(userId, page, size);
        return getMapResponseEntity(tasksPage);
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks(@PathVariable long userId){
        if (userService.findById(userId) == null) {
            return ResponseEntity.notFound().build();
        }
        List<Task> tasksPage = taskService.findAll(userId);
        return ResponseEntity.ok(tasksPage);
    }


    @GetMapping("/activebydate")
    public ResponseEntity<Map<String, Object>> getAllOpenOrLateTasksByDate(
            @PathVariable long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
        Page<Task> tasksPage = taskService.findAllOpenOrLateOrderByDonedate(userId, page, size);
        return getMapResponseEntity(tasksPage);
    }

    private ResponseEntity<Map<String, Object>> getMapResponseEntity(Page<Task> tasksPage) {
        Page<TaskDTO> dtoTasksPage = tasksPage.map(TaskDTO::new);
        Map<String, Object> response = new HashMap<>();
        response.put("tasks", dtoTasksPage.getContent());
        response.put("totalTasks", dtoTasksPage.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/closed")
    public ResponseEntity<Map<String, Object>> getAllTasksClosed(
            @PathVariable long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
        Page<Task> tasksPage = taskService.findAllClosed(userId, page, size);
        return getMapResponseEntity(tasksPage);
    }


    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id, @PathVariable Long userId) {
        if(taskService.findTaskById(id) == null) {
            return ResponseEntity.badRequest().build();
        }
        Task task = taskService.getTaskById(id, userId);
        TaskDTO taskDTO = new TaskDTO(task);
        return ResponseEntity.ok().body(taskDTO);
    }

    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@RequestBody TaskDTO task, @PathVariable Long userId) {
        task.setUserId(userId);
        Task newtask = taskService.fromDTO(task);
        System.out.println("data: "+ newtask.getDonedate());
        taskService.createTask(newtask, newtask.getState().getId());
        TaskDTO taskDTO = new TaskDTO(newtask);
        return ResponseEntity.ok().body(taskDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@RequestBody TaskDTO task, @PathVariable Long id, @PathVariable Long userId) {
        task.setUserId(userId);
        task.setId(id);
        Task updatedTask = taskService.updateTask(task, id);
        return ResponseEntity.ok().body(updatedTask);
    }


    @PutMapping("done/{id}")
    public ResponseEntity<TaskDTO> completedTask(@PathVariable Long id, @PathVariable Long userId) {
        if (taskService.findTaskById(id) == null) {
            return ResponseEntity.badRequest().build();
        }
        Task task = taskService.getTaskById(id, userId);
        task.setState(taskService.CompletTask(2L));
        TaskDTO taskDTO = new TaskDTO(task);
        Task newTask =  taskService.updateTask(taskDTO, userId);
        return ResponseEntity.ok().body(new TaskDTO(newTask));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, @PathVariable Long userId) {
        Task task = taskService.getTaskById(id, userId);
        if (userService.findById(userId) == null) {
            return ResponseEntity.notFound().build();
        }
        taskService.deleteTask(task);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Task>> getTasksByCategoryAndClient(@PathVariable Long categoryId, @PathVariable Long userId) {
        List<Task> tasks = taskService.findByCategory(categoryId, userId);
        return ResponseEntity.ok().body(tasks);
    }


}
