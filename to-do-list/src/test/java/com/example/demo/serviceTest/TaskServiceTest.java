package com.example.demo.serviceTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;

import com.example.demo.Repository.TaskRepository;
import com.example.demo.Repository.TaskStateRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.model.Task;
import com.example.demo.service.TaskCategoryService;
import com.example.demo.service.TaskService;
import com.example.demo.service.TaskStateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TaskServiceTest {

    @InjectMocks
    private TaskService taskService;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskCategoryService taskCategoryService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskStateRepository taskStateRepository;

    @Mock
    private TaskStateService taskStateService;

    private Task task;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("This is a test task");
        task.setCreatedAt(new Date());
        task.setUpdatedAt(new Date());
    }

    @Test
    public void testGetTaskById() {
        when(taskRepository.findTask(1L, 1L)).thenReturn(task);
        Task result = taskService.getTaskById(1L, 1L);
        assertEquals(task, result);
        verify(taskRepository).findTask(1L, 1L);
    }

    @Test
    public void testGetTaskByIdNotFound() {
        when(taskRepository.findTask(1L, 1L)).thenReturn(null);
        Task result = taskService.getTaskById(1L, 1L);
        assertNull(result);
        verify(taskRepository).findTask(1L, 1L);
    }

    @Test
    public void testSaveTask() {
        taskService.saveTask(task);
        verify(taskRepository).save(task);
    }

    @Test
    public void testFindByCategory() {
        when(taskRepository.findByCategory(anyLong(), anyLong(), anyLong())).thenReturn(Collections.singletonList(task));
        List<Task> tasks = taskService.findByCategory(1L, 1L);
        assertEquals(1, tasks.size());
        assertEquals(task, tasks.getFirst());
    }

    @Test
    public void testCreateTask() {
        when(taskRepository.save(task)).thenReturn(task);
        Task createdTask = taskService.saveTask(task);
        assertEquals(task, createdTask);
        verify(taskRepository).save(task);
    }


    @Test
    public void testFindTaskById() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        Task foundTask = taskService.findTaskById(1L);
        assertEquals(task, foundTask);
    }




    @Test
    public void testDeleteTask() {
        taskService.deleteTask(task);
        verify(taskRepository).delete(task);
    }
}
