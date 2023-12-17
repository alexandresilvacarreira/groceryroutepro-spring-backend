package pt.upskill.groceryroutepro.controllers;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pt.upskill.groceryroutepro.entities.User;
import pt.upskill.groceryroutepro.models.SignUp;
import pt.upskill.groceryroutepro.services.AuthService;
import pt.upskill.groceryroutepro.services.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@Component
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("/users/get-authenticated-user")
    public ResponseEntity<Map<String, Object>> getAuthenticatedUser() {
        Map<String, Object> response = new HashMap<>();
        try {
            User authenticatedUser = this.userService.getAuthenticatedUser();
            if (authenticatedUser == null) {
                response.put("success", false);
                response.put("message", "Nenhum utilizador encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            response.put("success", true);
            response.put("user", authenticatedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erro ao obter utilizador");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
