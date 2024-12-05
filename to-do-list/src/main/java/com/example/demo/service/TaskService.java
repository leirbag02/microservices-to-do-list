package com.example.demo.service;
import com.example.demo.Repository.TaskRepository;
import com.example.demo.Repository.TaskStateRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.dto.TaskDTO;

import com.example.demo.model.Task;
import com.example.demo.model.TaskCategory;
import com.example.demo.model.TaskState;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class
TaskService {

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    TaskCategoryService taskCategoryService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TaskStateRepository taskState;

    @Autowired
    private AzureOpenAIService azureOpenAIService;


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

        return taskRepository.findByStateOpenOrLate(OpenState, LateState, userId, pageable);
    }

    public Page<Task> findAllOpenOrLateOrderByDonedate(Long userId, int page, int size) {
        taskStateService.updateTaskState();

        Pageable pageable = PageRequest.of(page, size);

        Long OpenState = 1L;

        Long LateState = 3L;

        return taskRepository.findByStateOpenOrLate(OpenState, LateState, userId, pageable);
    }


    public Page<Task> findAllClosed(Long user, int page, int size) {

        taskStateService.updateTaskState();

        Pageable pageable = PageRequest.of(page, size);

        Long closedStateId = 2L;

        return taskRepository.findByStateAndClientId(closedStateId, user, pageable);

    }

    public TaskState CompletTask(Long id) {
        TaskState taskState = new TaskState();
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
        return taskRepository.save(updateData(oldTask, task1));
    }


    public Task findTaskById(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
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


    public Task createTaskFromText(String inputPrompt, Long userId) throws JsonProcessingException {
        System.out.println(STR."Texto recebido para criar tarefa: \{inputPrompt}");

        // Monta o prompt para o modelo AI
        String fullPrompt = String.format(STR."""
        Analise o pedido abaixo e retorne somente o textContent como um objeto JSON estruturado. Identifique o tipo de ação (POST, UPDATE ou DELETE) e forneça os campos necessários para realizar a operação em uma tarefa. O JSON deve ter a seguinte estrutura:
        {
          "action": "<POST | UPDATE | DELETE>",
          "task": {
            "id": <ID da tarefa ou null para POST>,
            "userId": <ID do usuário>,
            "title": "<Título da Tarefa>",
            "description": "<Descrição da Tarefa>",
            "priority": <Número entre 1 e 5 : quanto menor mais urgente>,
            "categoryId": <Número entre 1 e 8>,
            "stateId": <Número entre 1 e 3> 1 : OPEN, 2 : LATE, 3 : CLOSED,
            "donedate": "<Formato: yyyy-MM-dd'T'HH:mm:ss'Z'> Horário atual:"\{new Date()},
            "createddate": "<Formato: yyyy-MM-dd'T'HH:mm:ss'Z'> ",
            "updateddate": "<Formato: yyyy-MM-dd'T'HH:mm:ss'Z'>"
          }
        }
        OBS : Forneça somente o json e nenhuma outra resposta adicional.
        Pedido: %s
        """, inputPrompt);

        String generatedText = azureOpenAIService.generateTaskDetails(fullPrompt);
        System.out.println(STR."Texto gerado pela OpenAI: \{generatedText}");
        return processTaskAction(generatedText, userId);
    }

    private Task processTaskAction(String inputJson, Long userId) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(inputJson);

        if (!jsonNode.has("action") || !jsonNode.has("task")) {
            throw new IllegalArgumentException("JSON inválido: 'action' ou 'task' ausente.");
        }

        String action = jsonNode.get("action").asText().toUpperCase();
        System.out.println(action);
        JsonNode taskNode = jsonNode.get("task");

        return switch (action) {
            case "POST" -> handleCreateTask(taskNode, userId);
            case "UPDATE" -> handleUpdateTask(taskNode, userId);
            case "DELETE" -> handleDeleteTask(taskNode, userId);
            default -> throw new IllegalArgumentException(STR."Ação inválida: \{action}");
        };
    }

    private Task handleCreateTask(JsonNode taskNode, Long userId) throws JsonProcessingException {
        Long stateId = taskNode.get("stateId").asLong();
        Task newTask = parseJsonToTask(taskNode.toString(), userId, stateId);
        System.out.println(STR."Tarefa criada com sucesso: \{newTask}");
        return taskRepository.save(newTask);
    }

    private Task handleUpdateTask(JsonNode taskNode, Long userId) {
        String name = taskNode.get("title").asText();
        Optional<Task> optionalTask = taskRepository.findTasksByNameLike(userId, name).stream().findFirst();
        if (optionalTask.isEmpty()) {
            throw new IllegalArgumentException("Tarefa com o título fornecido não encontrada.");
        }
        Task existingTask = optionalTask.get();

        System.out.println(existingTask);


        if (taskNode.has("title") && !Objects.equals(taskNode.get("title").asText(), "null")) {
            existingTask.setTitle(taskNode.get("title").asText());
        }
        if (taskNode.has("description") && !Objects.equals(taskNode.get("description").asText(), "null")) {
            existingTask.setDescription(taskNode.get("description").asText());
        }
        if (taskNode.has("priority") && !Objects.equals(taskNode.get("priority").asText(), "null")) {
            existingTask.setPriority(taskNode.get("priority").asInt());
        }
        if (taskNode.has("donedate") && !Objects.equals(taskNode.get("donedate").asText(), "null")) {
            existingTask.setDonedate(Date.from(Instant.parse(taskNode.get("donedate").asText())));
        }
        if (taskNode.has("stateId") && !Objects.equals(taskNode.get("stateId").asText(), "null")) {
            TaskState newState = taskStateRepository.findById(taskNode.get("stateId").asLong())
                    .orElseThrow(() -> new IllegalArgumentException("Estado inválido."));
            existingTask.setState(newState);
        }


        System.out.println(STR."Tarefa atualizada com sucesso: \{existingTask}");
        return taskRepository.save(existingTask);

    }

    private Task handleDeleteTask(JsonNode taskNode, Long userId) {
        String name = taskNode.get("title").asText();
        Task existingTask =  taskRepository.findTasksByNameLike(userId, name).getFirst();
        taskRepository.delete(existingTask);
        System.out.println(STR."Tarefa excluída com sucesso: \{existingTask.getTitle()}");
        return existingTask;
    }

    private Task parseJsonToTask(String json, Long userId, Long stateId) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(json);

        TaskState taskState = taskStateRepository.findByID(stateId);
        TaskCategory taskCategory = taskCategoryService.repository.findByID(jsonNode.get("categoryId").asLong());

        return new Task(
                null,
                userRepository.findByID(userId),
                jsonNode.get("title").asText(),
                jsonNode.get("description").asText(),
                jsonNode.get("priority").asInt(),
                taskState,
                taskCategory,
                jsonNode.has("donedate") ? Date.from(Instant.parse(jsonNode.get("donedate").asText())) : null,
                new Date(),
                new Date()
        );
    }


}



