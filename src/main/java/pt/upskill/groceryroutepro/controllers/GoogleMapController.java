package pt.upskill.groceryroutepro.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.upskill.groceryroutepro.entities.User;
import pt.upskill.groceryroutepro.exceptions.ValidationException;
import pt.upskill.groceryroutepro.exceptions.types.BadRequestException;
import pt.upskill.groceryroutepro.models.LatLng;
import pt.upskill.groceryroutepro.services.GoogleApiService;
import pt.upskill.groceryroutepro.services.GoogleApiServiceImpl;
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

   /* @PostMapping("/get-polyline")
    public ResponseEntity getCoordinates(@RequestBody Map<String, String> polylineObject) {
        String polyline = polylineObject.get("polyline");

        Map<String, Object> response = new HashMap<>();

        //List<LatLng> coordinates = googleApiService.decodePolyline(polyline);
        response.put("points", coordinates);
        return ResponseEntity.ok(response);
    }*/


    @PostMapping("/create-route")
    public ResponseEntity createRoute(@RequestBody Map<String, LatLng> coordinates) {
        Map<String, Object> response = new HashMap<>();

        User user = userService.getAuthenticatedUser();

        //if (user == null) throw new BadRequestException("Utilizador não autenticado"); //TODO;


        LatLng partida = coordinates.get("partida");
        LatLng destino = coordinates.get("destino");

        String routes = googleApiService.createRoute(partida, destino, user);

        response.put("rotas",routes);

        return ResponseEntity.ok(response);
    }
}
