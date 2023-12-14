package pt.upskill.groceryroutepro.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import pt.upskill.groceryroutepro.models.SignUp;
import pt.upskill.groceryroutepro.services.AuthService;

@RestController
@Component
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/users/signup")
    public ResponseEntity<String> signUp(@RequestBody SignUp signUp){
        try {
            authService.createAccount(signUp);
            return ResponseEntity.ok("Conta registada com sucesso");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao registar conta: " + e.getMessage());
        }
    }

    @GetMapping("/users/login")
    public ResponseEntity<String> login(){
        return ResponseEntity.ok("fixe");
    }


}
