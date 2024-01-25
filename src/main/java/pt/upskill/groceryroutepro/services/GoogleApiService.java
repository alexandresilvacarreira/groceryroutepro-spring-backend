package pt.upskill.groceryroutepro.services;

import pt.upskill.groceryroutepro.entities.User;
import pt.upskill.groceryroutepro.models.CreateRouteModel;
import pt.upskill.groceryroutepro.models.LatLng;
import pt.upskill.groceryroutepro.models.LatLngName;

import java.util.List;

public interface GoogleApiService {

    List<CreateRouteModel> generateRoutes(LatLngName partida, LatLngName Destino);

    List<CreateRouteModel> getRoutes();
}
