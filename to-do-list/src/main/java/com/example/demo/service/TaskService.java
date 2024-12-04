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
import java.util.Date;
import java.util.List;

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


    public void createTaskFromText(String inputPrompt, Long userId) throws JsonProcessingException {
        System.out.println("Texto recebido para criar tarefa: " + inputPrompt);

        // Novo prompt que solicita uma resposta em JSON
        String fullPrompt = String.format(
                "Analise o pedido abaixo e retorne somente o textContent como um objeto JSON estruturado. Identifique o tipo de ação (POST, UPDATE ou DELETE) e forneça os campos necessários para realizar a operação em uma tarefa. O JSON deve ter a seguinte estrutura:\n" +
                        "{\n" +
                        "  \"action\": \"<POST | UPDATE | DELETE>\",\n" +
                        "  \"task\": {\n" +
                        "    \"id\": <ID da tarefa ou null para POST>,\n" +
                        "    \"userId\": <ID do usuário>,\n" +
                        "    \"title\": \"<Título da Tarefa>\",\n" +
                        "    \"description\": \"<Descrição da Tarefa>\",\n" +
                        "    \"priority\": <Número entre 1 e 5 : quanto menor mais urgente>,\n" +
                        "    \"categoryId\": <Número entre 1 e 8>,\n" +
                        "    \"stateId\": <Numero entre 1 e 3, 1 : OPEN, 2 : LATE,  3 : CLOSED>,\n" +
                        "    \"donedate\": \"<Formato  : 2024-10-15T14:30:00Z>\",\n" +
                        "    \"createddate\": \"<Data de criação da tarefa ou null>\",\n" +
                        "    \"updateddate\": \"<Data de atualização da tarefa ou null>\"\n" +
                        "  }\n" +
                        "}\n" +
                        "Se o pedido não incluir dados suficientes tente interpretar o pedido e preencher da forma mais coveniente. Caso não seja possivel de maneira alguma   uma tarefa ou realizar a operação, informe claramente quais campos estão ausentes no JSON. Certifique-se de interpretar corretamente o pedido em linguagem natural.\n" +
                        "Pedido: %s", inputPrompt);
        String generatedText = null;
        // Gerar o texto com a OpenAI
        generatedText = String.valueOf(azureOpenAIService.generateTaskDetails(fullPrompt));
        System.out.println(STR."Texto gerado pela OpenAI (completo): \{generatedText}");
        processTaskAction(generatedText, userId);
    }



    public void processTaskAction(String inputPrompt, Long userId) throws JsonProcessingException {
        // Gera o JSON a partir do pedido
        String fullPrompt = String.format(
                "Analise o pedido abaixo e retorne somente o textContent como um objeto JSON estruturado. Identifique o tipo de ação ...",
                inputPrompt
        );

        String responseJson = azureOpenAIService.generateTaskDetails(fullPrompt);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseJson);

        String action = jsonNode.get("action").asText();

        switch (action.toUpperCase()) {
            case "POST":
                handleCreateTask(jsonNode.get("task"), userId);
                break;

            case "UPDATE":
                handleUpdateTask(jsonNode.get("task"), userId);
                break;

            case "DELETE":
                handleDeleteTask(jsonNode.get("task"), userId);
                break;

            default:
                throw new IllegalArgumentException("Ação inválida: " + action);
        }
    }

    private void handleCreateTask(JsonNode taskNode, Long userId) throws JsonProcessingException {
        // Converte os dados recebidos do JSON para uma entidade Task
        Long stateId = taskNode.get("stateId").asLong();
        Task newTask = parseJsonToTask(taskNode.toString(), userId, stateId);

        // Salva a nova tarefa no banco
        taskRepository.save(newTask);
        System.out.println("Tarefa criada com sucesso: " + newTask);
    }

    private void handleUpdateTask(JsonNode taskNode, Long userId) {
        String taskName = taskNode.get("title").asText();
        List<Task> tasks = taskRepository.findTasksByNameLike(userId, taskName);

        if (tasks.isEmpty()) {
            throw new IllegalArgumentException("Nenhuma tarefa encontrada com o nome: " + taskName);
        }

        Task taskToUpdate = tasks.get(0); // Usando a primeira tarefa encontrada para atualizar

        // Atualiza os campos da tarefa
        if (taskNode.has("description")) {
            taskToUpdate.setDescription(taskNode.get("description").asText());
        }
        if (taskNode.has("priority")) {
            taskToUpdate.setPriority(taskNode.get("priority").asInt());
        }
        if (taskNode.has("donedate")) {
            taskToUpdate.setDonedate(new Date(taskNode.get("donedate").asLong()));
        }
        if (taskNode.has("stateId")) {
            Long stateId = taskNode.get("stateId").asLong();
            TaskState newState = taskStateRepository.findById(stateId)
                    .orElseThrow(() -> new IllegalArgumentException("Estado inválido: " + stateId));
            taskToUpdate.setState(newState);
        }

        // Salva a tarefa atualizada no banco
        taskRepository.save(taskToUpdate);
        System.out.println(STR."Tarefa atualizada com sucesso: \{taskToUpdate}");
    }

    private void handleDeleteTask(JsonNode taskNode, Long userId) {
        // Busca a tarefa pelo nome ou ID
        String taskName = taskNode.get("title").asText();
        List<Task> tasks = taskRepository.findTasksByNameLike(userId, taskName);

        if (tasks.isEmpty()) {
            throw new IllegalArgumentException(STR."Nenhuma tarefa encontrada com o nome: \{taskName}");
        }

        Task taskToDelete = tasks.getFirst(); // Usando a primeira tarefa encontrada para exclusão

        taskRepository.delete(taskToDelete);
        System.out.println(STR."Tarefa excluída com sucesso: \{taskToDelete.getTitle()}");
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
        // ID será gerado automaticamente ao salvar
        // Busca o usuário pelo ID
        // Título da tarefa
        // Descrição da tarefa
        // Prioridade (1 a 5)
        // Estado da tarefa
        // Categoria da tarefa
        // Data de conclusão, se disponível
        // Data de criação
        // Data de atualização
        return new Task(
                null, // ID será gerado automaticamente ao salvar
                userRepository.findByID(userId), // Busca o usuário pelo ID
                jsonNode.get("title").asText(), // Título da tarefa
                jsonNode.get("description").asText(), // Descrição da tarefa
                jsonNode.get("priority").asInt(), // Prioridade (1 a 5)
                taskState, // Estado da tarefa
                taskCategory, // Categoria da tarefa
                doneDate, // Data de conclusão, se disponível
                new Date(), // Data de criação
                new Date()  // Data de atualização
                );
}

}



