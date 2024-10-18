package com.example.demo.service;
import com.example.demo.Excpetion.ObjectNotFound;
import com.example.demo.Repository.UserRepository;
import com.example.demo.dto.TaskDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.UserLoged;
import com.example.demo.model.Task;
import com.example.demo.model.User;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Date;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void update(User obj) {
        User user = userRepository.findByID(obj.getId());
        updateData(obj, user);
        userRepository.save(user);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ObjectNotFound("User Not Found"));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public UserDTO login(UserLoged userLoged) {
        User client = findUser(userLoged.getEmail(), userLoged.getPassword());
        return createLoggedDTO(client);
    }

    public User findUser(String email, String password) {
        User user = userRepository.findByEmailAndPassword(email, password);
        if (user == null) {
            throw new ObjectNotFound("Usuário não encontrado");
        }
        return user;
    }

    private UserDTO createLoggedDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setPassword(user.getPassword());
        userDTO.setEnabled(user.getActive());
        return userDTO;
    }

    public User fromDTO(UserDTO obj) {
        return new User(
                obj.getId(),
                obj.getName(),
                obj.getEmail(),
                obj.getPassword(),
                new Date(),
                new Date(),
                Boolean.TRUE
        );
    }

    public Boolean findByEmail(String email) {
        User user = userRepository.findByEmail(email);
        return (Boolean) (user != null);
    }

    public TaskDTO toDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setPriority(task.getPriority());
        dto.setState(task.getState().getId());
        dto.setCategory(task.getCategory().getId());
        dto.setDonedate(task.getDonedate());
        dto.setCreateddate(task.getCreatedAt());
        dto.setUpdateddate(task.getUpdatedAt());
        return dto;
    }


    private void updateData(User newObj, User obj) {
        newObj.setName((obj.getName() == null) ? newObj.getName() : obj.getName());
        newObj.setEmail((obj.getEmail() == null) ? newObj.getEmail() : obj.getEmail());
        newObj.setPassword((obj.getPassword() == null) ? newObj.getPassword() : obj.getPassword());
        newObj.setActive((obj.getActive() == null) ? newObj.getActive() : obj.getActive());
    }

}
