package com.example.demo.serviceTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.example.demo.Repository.TaskRepository;
import com.example.demo.Repository.TaskStateRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.dto.TaskDTO;
import com.example.demo.model.Task;
import com.example.demo.model.TaskCategory;
import com.example.demo.model.TaskState;
import com.example.demo.service.TaskCategoryService;
import com.example.demo.service.TaskService;
import com.example.demo.service.TaskStateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
    public void testFindAllTasksOpenByClient() {
        doNothing().when(taskStateService).updateTaskState();

        // Mocking the Pageable response
        Pageable pageable = PageRequest.of(0, 9);
        Page<Task> mockPage = new PageImpl<>(Arrays.asList(task), pageable, 1);

        when(taskRepository.findByStateAndClientId(1L, 1L, pageable)).thenReturn(mockPage);

        Page<Task> tasks = taskService.findAllOpenOrLate(1L, 0, 9);

        assertEquals(1, tasks.getSize());
        assertEquals(task, tasks.getContent().getFirst());

        verify(taskStateService).updateTaskState();
        verify(taskRepository).findByStateAndClientId(1L, 1L, pageable);
    }

    @Test
    public void testFindByCategory() {
        when(taskRepository.findByCategory(anyLong(), anyLong(), anyLong())).thenReturn(Arrays.asList(task));
        List<Task> tasks = taskService.findByCategory(1L, 1L);
        assertEquals(1, tasks.size());
        assertEquals(task, tasks.get(0));
    }

    @Test
    public void testCreateTask() {
        when(taskRepository.save(task)).thenReturn(task);
        Task createdTask = taskService.saveTask(task);
        assertEquals(task, createdTask);
        verify(taskRepository).save(task);
    }

    @Test
    public void testCreateTaskWithValidation() {
        task.setTitle("");
        Exception exception = assertThrows(Exception.class, () -> {
            taskService.createTask(task, 1L);
        });
        assertTrue(exception.getMessage().contains("Validation failed"));
        verify(taskRepository, never()).save(task);
    }

    @Test
    public void testFindTaskById() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        Task foundTask = taskService.findTaskById(1L);
        assertEquals(task, foundTask);
    }

    @Test
    public void testFindTaskByIdNotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        Task foundTask = taskService.findTaskById(1L);
        assertNull(foundTask);
    }

    @Test
    public void testUpdateTask() {
        // Configura o DTO da tarefa que será atualizado
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(1L);
        taskDTO.setUserId(1L); // Assumindo que a tarefa pertence ao usuário com ID 1
        taskDTO.setTitle("Updated Task");
        taskDTO.setCategory(2L); // Defina a categoria que deseja usar
        taskDTO.setState(1L); // Defina o estado que deseja usar
        taskDTO.setDescription("This is an updated description");
        taskDTO.setPriority(1); // Defina a prioridade se necessário

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        when(taskCategoryService.findById(2L)).thenReturn(Optional.of(new TaskCategory(2L, "Categoria 2"))); // Simule o retorno da categoria

        when(taskStateRepository.findByID(1L)).thenReturn(new TaskState(1L, "Estado 1")); // Simule o retorno do estado

        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task updatedTask = taskService.updateTask(taskDTO, 1L);

        assertEquals(task, updatedTask);
        assertEquals("Updated Task", updatedTask.getTitle());

        // Verifica se o repositório save foi chamado
        verify(taskRepository).save(any(Task.class));
    }


    @Test
    public void testDeleteTask() {
        taskService.deleteTask(task);
        verify(taskRepository).delete(task);
    }
}
