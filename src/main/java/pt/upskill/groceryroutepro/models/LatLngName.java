package pt.upskill.groceryroutepro.models;

public class LatLngName extends LatLng{


    private String nameLocation;

    public LatLngName(double lat, double lng) {
        super(lat, lng);
    }


    public String getNameLocation() {
        return nameLocation;
    }

    public void setNameLocation(String nameLocation) {
        this.nameLocation = nameLocation;
    }


}
