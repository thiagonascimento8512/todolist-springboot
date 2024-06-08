package br.com.thiagonascimento.todolist.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private IUserRepository userRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody UserModel user) {
        var u = this.userRepository.findByUsername(user.getUsername());

        if (u != null) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        var userExistsWithEmail = this.userRepository.findByEmail(user.getEmail());

        if (userExistsWithEmail != null) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        var passwordHashed = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(passwordHashed);

        return ResponseEntity.ok(this.userRepository.save(user));
    }
}

