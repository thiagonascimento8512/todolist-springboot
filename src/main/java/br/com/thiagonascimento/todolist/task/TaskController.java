package br.com.thiagonascimento.todolist.task;

import br.com.thiagonascimento.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel task, HttpServletRequest request) {
        var userId = request.getAttribute("idUser");

        var currentDate = LocalDateTime.now();

        if (currentDate.isAfter(task.getStartAt()) ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    "The start date must be greater than the current date"
            );
        } else if (currentDate.isAfter(task.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    "The end date must be greater than the current date"
            );
        } else if (task.getStartAt().isAfter(task.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    "The end date must be greater than the start date"
            );
        }

        task.setIdUser((UUID) userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(taskRepository.save(task));
    }

    @GetMapping("/")
    public ResponseEntity list(HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");
        var tasks = this.taskRepository.findByIdUser((UUID) idUser);

        return ResponseEntity.status(HttpStatus.OK).body(tasks);
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel task, HttpServletRequest request, @PathVariable UUID id) {
        var idUser = request.getAttribute("idUser");
        var t = this.taskRepository.findById(id).orElse(null);

        if (t == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
        }

        if (!t.getIdUser().equals(idUser)) {
           return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to update this task");
        }

        Utils.copyNonNullProperties(task, t);
        return ResponseEntity.status(HttpStatus.OK).body(this.taskRepository.save(t));
    }
}
