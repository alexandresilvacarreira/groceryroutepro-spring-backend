package pt.upskill.groceryroutepro.services;

import pt.upskill.groceryroutepro.entities.User;
import pt.upskill.groceryroutepro.models.LatLng;

import java.util.List;

public interface GoogleApiService {




    String createRoute(LatLng partida, LatLng Destino, User user);
}
