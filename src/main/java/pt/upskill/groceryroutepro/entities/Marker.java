package pt.upskill.groceryroutepro.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import pt.upskill.groceryroutepro.models.LatLngName;

import javax.persistence.*;
import java.time.LocalDateTime;


@Entity
public class Marker {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JsonIgnore
    private Route route;
    private double lat;
    private double lng;
    private String label;

    public Marker() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
