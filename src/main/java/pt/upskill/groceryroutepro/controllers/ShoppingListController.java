package pt.upskill.groceryroutepro.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import pt.upskill.groceryroutepro.entities.ShoppingList;
import pt.upskill.groceryroutepro.exceptions.ValidationException;
import pt.upskill.groceryroutepro.services.ShoppingListService;
import pt.upskill.groceryroutepro.services.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@Component
@RequestMapping("/shopping-list")
public class ShoppingListController {

    @Autowired
    UserService userService;

    @Autowired
    ShoppingListService shoppingListService;

    @PostMapping("/add-product")
    public ResponseEntity addProduct(@RequestBody Map<String, Long> genericProductId) {
        Map<String, Object> response = new HashMap<>();
        try {
            ShoppingList shoppingList = shoppingListService.addProduct(genericProductId.get("genericProductId"));
            Map<String, Object> data = new HashMap<>();
            data.put("shoppingList", shoppingList);
            response.put("data", data);
            response.put("message", "Produto adicionado!");
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(e.getStatusCode()).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/remove-product")
    public ResponseEntity removeProduct(@RequestBody Map<String, Long> genericProductId) {
        Map<String, Object> response = new HashMap<>();
        try {
            ShoppingList shoppingList = shoppingListService.removeProduct(genericProductId.get("genericProductId"));
            Map<String, Object> data = new HashMap<>();
            data.put("shoppingList", shoppingList);
            response.put("data", data);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(e.getStatusCode()).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/remove-all")
    public ResponseEntity removeAll(@RequestBody Map<String, Long> genericProductId) {
        Map<String, Object> response = new HashMap<>();
        try {
            ShoppingList shoppingList = shoppingListService.removeAll(genericProductId.get("genericProductId"));
            Map<String, Object> data = new HashMap<>();
            data.put("shoppingList", shoppingList);
            response.put("data", data);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(e.getStatusCode()).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/get")
    public ResponseEntity getShoppingList() {
        Map<String, Object> response = new HashMap<>();
        try {
            ShoppingList shoppingList = shoppingListService.getCurrentShoppingList();
            Map<String, Object> data = new HashMap<>();
            data.put("shoppingList", shoppingList);
            response.put("data", data);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(response);
        } catch (Exception e1) {
            return ResponseEntity.badRequest().body(e1.getMessage());
        }
    }

}
