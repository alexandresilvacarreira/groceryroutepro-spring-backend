package pt.upskill.groceryroutepro.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import pt.upskill.groceryroutepro.exceptions.ValidationException;
import pt.upskill.groceryroutepro.models.CreateRouteModel;
import pt.upskill.groceryroutepro.models.LatLngName;
import pt.upskill.groceryroutepro.services.GoogleApiService;
import pt.upskill.groceryroutepro.services.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Component
@RequestMapping("/google-maps-api")
public class GoogleMapController {
    @Autowired
    GoogleApiService googleApiService;


    @Autowired
    UserService userService;

    @PostMapping("/create-route")
    public ResponseEntity createRoute(@RequestBody Map<String, LatLngName> coordinates) {
        Map<String, Object> response = new HashMap<>();
        try{
            LatLngName partida = coordinates.get("partida");
            partida.setNameLocation("Partida");
            LatLngName destino = coordinates.get("destino");
            destino.setNameLocation("Destino");
            googleApiService.generateRoutes(partida,destino);
            response.put("success", true);
            response.put("message", "Rotas criadas com sucesso");
            //response.put("rotas",rotas);
            return ResponseEntity.ok(response);
        } catch (ValidationException e){
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(response);
        }
    }

    @GetMapping("/get-route")
    public ResponseEntity getRoute(){
        Map<String, Object> response = new HashMap<>();
        try{
            List<CreateRouteModel> routes = googleApiService.getRoutes();
            Map<String, Object> data = new HashMap<>();
            data.put("routes", routes);
            if (googleApiService.checkShoppingList()){
                response.put("success", true);
            } else {
                response.put("sucess", false);
                response.put("message", "Existem alterações na lista de compras, poderá ser necessário gerar nova rota");
            }
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (ValidationException e){
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(response);

        }
    }
}
