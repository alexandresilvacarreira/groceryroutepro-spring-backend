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
import pt.upskill.groceryroutepro.models.EmailVerificationToken;
import pt.upskill.groceryroutepro.models.Emailmodel;
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
    public User getAuthenticatedUser() {

        User authenticatedUser = this.userService.getAuthenticatedUser();
        return authenticatedUser;
//
//        Map<String, Object> response = new HashMap<>();
//        try {
//            User authenticatedUser = this.userService.getAuthenticatedUser();
//            if (authenticatedUser == null) {
//                response.put("success", false);
//                response.put("message", "Nenhum utilizador encontrado");
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//            }
//            response.put("success", true);
//            response.put("user", authenticatedUser);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            response.put("success", false);
//            response.put("message", "Erro ao obter utilizador");
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signUp(@RequestBody SignUp signUp) {
        // fazer verificação dos dados de sign up iguais ao frontend


        Map<String, Object> response = new HashMap<>();
        try {
            if (userService.createAccount(signUp) != null) {
                response.put("success", true);
                response.put("message", "Conta registada com sucesso");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Erro ao registar conta");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/verify-account")
    public ResponseEntity<Map<String, Object>> verifyAccount(@RequestBody EmailVerificationToken emailVerificationToken){
        Map<String, Object> response = new HashMap<>();
        System.out.println(emailVerificationToken);

        try {
            //TODO isto não está bem é suposto devolver logo erro
            if (userService.verifyEmail(emailVerificationToken.getToken())) {
                // devolver o user?
                response.put("success", true);
                response.put("message", "Conta verificada com sucesso");
                return ResponseEntity.ok(response);

            } else {
                response.put("success", false);
                response.put("message", "Erro ao verificar conta");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/users/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Emailmodel emailmodel){
        Map<String, Object> response = new HashMap<>();

        try {
            //TODO isto não está bem é suposto devolver logo erro
            if (userService.getPasswordLink(emailmodel.getEmail())) {
                // devolver o user?
                response.put("success", true);
                response.put("message", "Pedido de mudança de password efetuado com sucesso");
                return ResponseEntity.ok(response);

            } else {
                response.put("success", false);
                response.put("message", "Erro ao realizar pedido de mudança de password");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


}
