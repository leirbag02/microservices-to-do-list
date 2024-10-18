package com.example.demo.controller;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.UserLoged;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> findAll() {
        List<User> users = userService.findAll();
        List<UserDTO> userDTOS  = users.stream().map(UserDTO::new).collect(Collectors.toList());
        return ResponseEntity.ok().body(userDTOS);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoged loginDTO) {
        UserDTO loggedDTO = userService.login(loginDTO);
        if (loggedDTO != null) {
            URI loggedUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/").build().toUri();
            return ResponseEntity.created(loggedUri).body(loggedDTO);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody UserDTO userDTO) {
        if (userService.findByEmail(userDTO.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new UserDTO());
        }
        User user = userService.fromDTO(userDTO);
        userService.save(user);
        return ResponseEntity.ok(new UserDTO(user));
    }

    @PutMapping("{id}/update")
    public ResponseEntity<UserDTO> update(@RequestBody UserDTO userDTO, @PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        userDTO.setId(id);
        userService.update(userService.fromDTO(userDTO));
        return ResponseEntity.status(HttpStatus.OK).body(userDTO);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        userService.deleteUser(user.getId());
        return ResponseEntity.ok().build();
    }

}
