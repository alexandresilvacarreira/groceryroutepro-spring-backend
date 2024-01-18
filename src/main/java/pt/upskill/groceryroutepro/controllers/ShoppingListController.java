package pt.upskill.groceryroutepro.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import pt.upskill.groceryroutepro.entities.User;
import pt.upskill.groceryroutepro.exceptions.ValidationException;
import pt.upskill.groceryroutepro.models.ChangePasswordRequestModel;
import pt.upskill.groceryroutepro.models.EmailVerificationToken;
import pt.upskill.groceryroutepro.models.Emailmodel;
import pt.upskill.groceryroutepro.models.SignUp;
import pt.upskill.groceryroutepro.services.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@Component
@RequestMapping("/shopping-list")
public class ShoppingListController {

    @Autowired
    UserService userService;




}
