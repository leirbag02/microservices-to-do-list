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

import java.time.LocalDateTime;
import java.util.Arrays;
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


    public Task createTaskFromText(String inputPrompt, Long userId, Long stateId) throws JsonProcessingException {
        System.out.println("Texto recebido para criar tarefa: " + inputPrompt);

        // Novo prompt que solicita uma resposta em JSON
        String fullPrompt = String.format(
                "Retorne somente o textContent com base no seguinte pedido, crie um objeto JSON representando uma tarefa com os seguintes campos:\n" +
                        "{\n" +
                        "  \"id\": null,\n" +
                        "  \"userId\": <ID do usuário>,\n" +
                        "  \"title\": \"<Título da Tarefa>\",\n" +
                        "  \"description\": \"<Descrição da Tarefa>\",\n" +
                        "  \"priority\": <Número entre 1 e 5>,\n" +
                        "  \"categoryId\": <ID da categoria>,\n" +
                        "  \"stateId\": <ID do estado da tarefa>,\n" +
                        "  \"donedate\": \"<Data de conclusão da tarefa>\",\n" +
                        "  \"createddate\": \"<Data de criação da tarefa>\",\n" +
                        "  \"updateddate\": \"<Data de atualização da tarefa>\"\n" +
                        "}\n" +
                        "Pedido: %s", inputPrompt);


        String generatedText = null;

        // Gerar o texto com a OpenAI

        generatedText = String.valueOf(azureOpenAIService.generateTaskDetails(fullPrompt));
        System.out.println("Texto gerado pela OpenAI (completo): " + generatedText);
        // Processar o texto gerado para converter em uma Task
        Task newTask = parseJsonToTask(generatedText, userId, stateId);
        Task savedTask = taskRepository.save(newTask);
        System.out.println("Tarefa salva com sucesso: " + savedTask);


        return savedTask;
    }

    private Task parseJsonToTask(String json, Long userId, Long stateId) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        // Converte o JSON recebido em um nó para facilitar a manipulação
        JsonNode jsonNode = objectMapper.readTree(json);

        // Busca entidades relacionadas
        TaskState taskState = taskStateRepository.findByID(stateId);
        Long categoryId = jsonNode.get("categoryId").asLong();
        TaskCategory taskCategory = taskCategoryService.repository.findByID(categoryId);

        // Converte a data de conclusão, se presente
        Date doneDate = jsonNode.has("donedate") && !jsonNode.get("donedate").isNull() ?
                new Date(jsonNode.get("donedate").asLong()) : null;

        // Converte a data de criação e atualização
        Date createdDate = jsonNode.has("createddate") && !jsonNode.get("createddate").isNull() ?
                new Date(jsonNode.get("createddate").asLong()) : new Date();
        Date updatedDate = jsonNode.has("updateddate") && !jsonNode.get("updateddate").isNull() ?
                new Date(jsonNode.get("updateddate").asLong()) : new Date();

        // Construção do objeto Task
        Task task = new Task(
                null, // ID será gerado automaticamente ao salvar
                userRepository.findByID(userId), // Busca o usuário pelo ID
                jsonNode.get("title").asText(), // Título da tarefa
                jsonNode.get("description").asText(), // Descrição da tarefa
                jsonNode.get("priority").asInt(), // Prioridade (1 a 5)
                taskState, // Estado da tarefa
                taskCategory, // Categoria da tarefa
                doneDate, // Data de conclusão, se disponível
                createdDate, // Data de criação
                updatedDate  // Data de atualização
        );

        return task;
    }


}








