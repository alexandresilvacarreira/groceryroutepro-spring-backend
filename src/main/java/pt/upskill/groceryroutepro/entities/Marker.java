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



}
