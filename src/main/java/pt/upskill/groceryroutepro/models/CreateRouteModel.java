package pt.upskill.groceryroutepro.models;

import java.util.ArrayList;
import java.util.List;

public class CreateRouteModel {

    private String polyline;

    private ArrayList<LatLngName> coordenadasMarcadores;

    private Integer totalTime;

    private Double shoppingListCost;

    private List<LatLng> vertices;

    private List<Long> ChainIdList;
    private List<String> ChainNameList;






    public CreateRouteModel(String polyline, ArrayList<LatLngName> coordenadasMarcadores, Integer totalTime, Double shoppingListCost) {
        this.polyline = polyline;
        this.coordenadasMarcadores = coordenadasMarcadores;
        this.totalTime = totalTime;
        this.shoppingListCost = shoppingListCost;
        this.vertices=new ArrayList<>();
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

    public List<LatLng> getVertices() {
        return vertices;
    }

    public void setVertices(List<LatLng> vertices) {
        this.vertices = vertices;
    }

    public List<Long> getChainIdList() {
        return ChainIdList;
    }

    public void setChainIdList(List<Long> chainIdList) {
        ChainIdList = chainIdList;
    }

    public List<String> getChainNameList() {
        return ChainNameList;
    }

    public void setChainNameList(List<String> chainNameList) {
        ChainNameList = chainNameList;
    }


}
