package pt.upskill.groceryroutepro.models;

public class ClosestChainModel {
    private String chain;
    private double dist;
    private LatLng coordinates;

    public ClosestChainModel(String chain) {
        this.chain = chain;
    }

    public String getChain() {
        return chain;
    }

    public void setChain(String chain) {
        this.chain = chain;
    }

    public double getDist() {
        return dist;
    }

    public void setDist(double dist) {
        this.dist = dist;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }
}
