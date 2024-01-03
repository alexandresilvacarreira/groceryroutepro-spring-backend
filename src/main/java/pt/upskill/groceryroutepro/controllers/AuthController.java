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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@Component
public class AuthController {

    @Autowired
    AuthService authService;



    // O loginForm requer este endpoint para redireccionar o utilizador no caso de pedidos efetuados sem autenticação.
    // Deverá redireccionar para a página de login do frontend
    @GetMapping("/login")
    public void login() {
        // TODO redirect para localhost:4200/login
    }

    @PostMapping("/logout")
    public void logout() {

    }


}
