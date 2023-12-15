package pt.upskill.groceryroutepro.controllers;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import pt.upskill.groceryroutepro.config.UserAuthenticationProvider;
import pt.upskill.groceryroutepro.entities.User;
import pt.upskill.groceryroutepro.models.Login;
import pt.upskill.groceryroutepro.models.SignUp;
import pt.upskill.groceryroutepro.services.AuthService;
import pt.upskill.groceryroutepro.services.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@Component
public class AuthController {

    @Autowired
    AuthService authService;

    @Autowired
    UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signUp(@RequestBody SignUp signUp) {
        Map<String, Object> serverMessage = new HashMap<>();
        try {
            if (authService.createAccount(signUp) != null) {
                serverMessage.put("success", true);
                serverMessage.put("message", "Conta registada com sucesso");
                return ResponseEntity.ok(serverMessage);
            } else {
                serverMessage.put("success", false);
                serverMessage.put("message", "Erro ao registar conta");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(serverMessage);
            }
        } catch (IllegalArgumentException e) {
            serverMessage.put("success", false);
            serverMessage.put("message", "Erro ao registar conta");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(serverMessage);
        } catch (Exception e) {
            serverMessage.put("success", false);
            serverMessage.put("message", "Erro ao registar conta");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(serverMessage);
        }
    }

    @GetMapping("/users/authenticated-user")
    public ResponseEntity<User> getAuthenticatedUser() {
        try {
            User authenticatedUser = this.userService.getAuthenticatedUser();
            return ResponseEntity.ok(authenticatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
