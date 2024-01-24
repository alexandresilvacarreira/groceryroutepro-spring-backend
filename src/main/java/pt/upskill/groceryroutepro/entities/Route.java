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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreationgDate() {
        return creationgDate;
    }

    public void setCreationgDate(LocalDateTime creationgDate) {
        this.creationgDate = creationgDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getCurrentRouteForUser() {
        return currentRouteForUser;
    }

    public void setCurrentRouteForUser(User currentRouteForUser) {
        this.currentRouteForUser = currentRouteForUser;
    }

    public List<Marker> getMarkers() {
        return markers;
    }

    public void setMarkers(List<Marker> markers) {
        this.markers = markers;
    }

    public String getCheapestPolyline() {
        return cheapestPolyline;
    }

    public void setCheapestPolyline(String cheapestPolyline) {
        this.cheapestPolyline = cheapestPolyline;
    }

    public String getFastestPolyline() {
        return fastestPolyline;
    }

    public void setFastestPolyline(String fastestPolyline) {
        this.fastestPolyline = fastestPolyline;
    }

    public int getTotalCheapestTime() {
        return totalCheapestTime;
    }

    public void setTotalCheapestTime(int totalCheapestTime) {
        this.totalCheapestTime = totalCheapestTime;
    }

    public int getTotalFastestTime() {
        return totalFastestTime;
    }

    public void setTotalFastestTime(int totalFastestTime) {
        this.totalFastestTime = totalFastestTime;
    }
}
