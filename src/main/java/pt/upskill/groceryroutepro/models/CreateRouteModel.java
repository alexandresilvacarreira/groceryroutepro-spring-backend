package pt.upskill.groceryroutepro.models;

import java.util.ArrayList;

public class CreateRouteModel {

    private String polyline;

    private ArrayList<LatLngName> coordenadasMarcadores;

    private Integer totalTime;

    private Double shoppingListCost;

    public CreateRouteModel(String polyline, ArrayList<LatLngName> coordenadasMarcadores, Integer totalTime, Double shoppingListCost) {
        this.polyline = polyline;
        this.coordenadasMarcadores = coordenadasMarcadores;
        this.totalTime = totalTime;
        this.shoppingListCost = shoppingListCost;
    }

    public String getPolyline() {
        return polyline;
    }

    public void setPolyline(String polyline) {
        this.polyline = polyline;
    }

    public ArrayList<LatLngName> getCoordenadasMarcadores() {
        return coordenadasMarcadores;
    }

    public void setCoordenadasMarcadores(ArrayList<LatLngName> coordenadasMarcadores) {
        this.coordenadasMarcadores = coordenadasMarcadores;
    }

    public Integer getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Integer totalTime) {
        this.totalTime = totalTime;
    }

    public Double getShoppingListCost() {
        return shoppingListCost;
    }

    public void setShoppingListCost(Double shoppingListCost) {
        this.shoppingListCost = shoppingListCost;
    }
}
