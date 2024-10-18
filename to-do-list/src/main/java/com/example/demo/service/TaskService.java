package com.example.demo.service;
import com.example.demo.Repository.TaskRepository;
import com.example.demo.Repository.TaskStateRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.dto.TaskDTO;
import com.example.demo.model.Task;
import com.example.demo.model.TaskCategory;
import com.example.demo.model.TaskState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    TaskCategoryService taskCategoryService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TaskStateRepository taskState;

    @Autowired
    TaskStateService taskStateService;
    @Autowired
    TaskStateRepository taskStateRepository;
    @Autowired
    private UserService userService;

    public List<Task> findAll(Long userId) {
        return taskRepository.findByClient(userId);
    }


    public Task getTaskById(Long id, Long userId) {
        return taskRepository.findTask(id, userId);
    }

    public Task saveTask(Task task) {
        taskRepository.save(task);
        return task;
    }

    public Page<Task> findAllOpenOrLate(Long userId, int page, int size) {
        taskStateService.updateTaskState();

        Pageable pageable = PageRequest.of(page, size);

        Long OpenState = 1L;

        Long LateState = 3L;

        return taskRepository.findByStateOpenOrLate(OpenState,LateState, userId, pageable);
    }

    public Page<Task> findAllOpenOrLateOrderByDonedate(Long userId, int page, int size) {
        taskStateService.updateTaskState();

        Pageable pageable = PageRequest.of(page, size);

        Long OpenState = 1L;

        Long LateState = 3L;

        return taskRepository.findByStateOpenOrLate(OpenState,LateState, userId, pageable);
    }


    public Page<Task> findAllClosed(Long user, int page, int size) {

        taskStateService.updateTaskState();

        Pageable pageable = PageRequest.of(page, size);

        Long closedStateId = 2L;

        return taskRepository.findByStateAndClientId(closedStateId, user, pageable);

    }

    public TaskState CompletTask(Long id) {
        TaskState taskState  = new TaskState();
        taskState.setId(id);
        return taskState;
    }


    public List<Task> findByCategory(Long id, Long clientID) {
        return taskRepository.findByCategory(id, (long) 1, clientID);
    }


    public void createTask(Task task, Long stateId) {
        TaskState state = taskStateRepository.findById(stateId)
                .orElseThrow(() -> new IllegalArgumentException("State not found"));
        task.setState(state);
        taskRepository.save(task); // Save the Task
    }


    public Task updateTask(TaskDTO task, Long id) {
        taskStateService.updateTaskState();
        Task task1 = fromDTOUpdate(task);
        Task oldTask = findTaskById(id);
        return taskRepository.save( updateData(oldTask, task1));
    }


    public Task findTaskById(Long id) {
        return taskRepository.findById(id).orElseThrow(()-> new RuntimeException("Task not found"));
    }

    public void deleteTask(Task task) {
        taskRepository.delete(task);
    }



    public Task fromDTO(TaskDTO taskDTO) {
        TaskState taskState = new TaskState();
        taskState.setId(1L);
        return new Task(
                taskDTO.getId(),
                userRepository.findByID(taskDTO.getUserId()),
                taskDTO.getTitle(),
                taskDTO.getDescription(),
                taskDTO.getPriority(),
                taskState,
                taskCategoryService.repository.findByID(taskDTO.getCategory()),
                taskDTO.getDonedate(),
                new Date(),
                new Date()
        );
    }



    public Task fromDTOUpdate(TaskDTO taskDTO) {
        TaskCategory taskCategory = taskCategoryService.repository.findByID(taskDTO.getCategory());
        return new Task(
                taskDTO.getId(),
                userRepository.findByID(taskDTO.getUserId()),
                taskDTO.getTitle(),
                taskDTO.getDescription(),
                taskDTO.getPriority(),
                taskStateRepository.findByID(taskDTO.getState()),
                taskCategory,
                taskDTO.getDonedate(),
                null,
                new Date()
        );
    }

    private Task updateData(Task obj, Task newObj) {
        if (newObj.getTitle() != null) {
            obj.setTitle(newObj.getTitle());
        }
        if (newObj.getDescription() != null) {
            obj.setDescription(newObj.getDescription());
        }
        if (newObj.getPriority() != 0) {
            obj.setPriority(newObj.getPriority());
        }
        if (newObj.getClient() != null) {
            obj.setClient(newObj.getClient());
        }
        if (newObj.getState() != null) {
            obj.setState(newObj.getState());
        }
        if (newObj.getCategory() != null) {
            obj.setCategory(newObj.getCategory());
        }
        if (newObj.getDonedate() != null) {
            obj.setDonedate(newObj.getDonedate());
        }
        obj.setUpdatedAt(new Date());
        return obj;
    }

}

