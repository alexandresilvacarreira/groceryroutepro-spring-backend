package pt.upskill.groceryroutepro.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import pt.upskill.groceryroutepro.exceptions.ValidationException;
import pt.upskill.groceryroutepro.entities.PasswordLink;
import pt.upskill.groceryroutepro.entities.User;
import pt.upskill.groceryroutepro.models.ChangePasswordRequestModel;
import pt.upskill.groceryroutepro.models.EmailVerificationToken;
import pt.upskill.groceryroutepro.models.Emailmodel;
import pt.upskill.groceryroutepro.models.SignUp;
import pt.upskill.groceryroutepro.services.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@Component
public class UserController {

    @Autowired
    UserService userService;


    @GetMapping("/users/get-authenticated-user")
    public ResponseEntity getAuthenticatedUser() {

        User authenticatedUser = this.userService.getAuthenticatedUser();
        if (authenticatedUser != null)
            return ResponseEntity.ok(authenticatedUser);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
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
        Map<String, Object> response = new HashMap<>();
        try {
            userService.createAccount(signUp);
            response.put("success", true);
            response.put("message", "Conta criada com sucesso");
            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(response);
        }
    }

    @PostMapping("/verify-account/")
    public ResponseEntity<Map<String, Object>> verifyAccount(@RequestBody EmailVerificationToken emailVerificationToken){
        Map<String, Object> response = new HashMap<>();

        try {
            userService.verifyEmail(emailVerificationToken.getToken());
                response.put("success", true);
                response.put("message", "Conta verificada com sucesso");
                return ResponseEntity.ok(response);
        } catch (ValidationException e){
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(response);
        }
    }

    @PostMapping("/users/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Emailmodel emailmodel){
        Map<String, Object> response = new HashMap<>();

        try {
            //TODO isto não está bem é suposto devolver logo erro
            if (userService.getPasswordLinkFromEmail(emailmodel.getEmail())) {
                // devolver o user?
                response.put("success", true);
                response.put("message", "Pedido de mudança de password efetuado com sucesso");
                return ResponseEntity.ok(response);

            } else {
                response.put("success", false);
                response.put("message", "Email não existe em base de dados");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PostMapping("/change-password/")
    public ResponseEntity<String> changePassword(
            @RequestBody ChangePasswordRequestModel changePasswordRequest) {

        PasswordLink passwordLink = userService.getPasswordLinkFromToken(changePasswordRequest.getToken());
        if (passwordLink == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido");
        }

        userService.changePassword(passwordLink, changePasswordRequest.getPassword());

        return ResponseEntity.ok("Password alterada com sucesso");
    }


}
