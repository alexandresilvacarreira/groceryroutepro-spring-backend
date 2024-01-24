package pt.upskill.groceryroutepro.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import pt.upskill.groceryroutepro.models.CreateRouteModel;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;


@Entity
public class Route {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime creationgDate;

    @ManyToOne
    @JsonIgnore
    private User user;
    @OneToOne(mappedBy = "currentRoute")
    @JoinColumn(name = "current_route_for_user_id")
    @JsonIgnore
    private User currentRouteForUser;


    @OneToMany(mappedBy = "route")
    private List<Marker> markers;

    private String cheapestPolyline;
    private String fastestPolyline;

    private int totalCheapestTime;
    private int totalFastestTime;




    public Route() {
    }


}
