package pt.upskill.groceryroutepro.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
public class Route {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime creationDate;
    @ManyToOne
    @JsonIgnore
    private User user;
    @OneToOne(mappedBy = "currentRoute")
    @JoinColumn(name = "current_route_for_user_id")
    @JsonIgnore
    private User currentRouteForUser;
    @OneToMany(mappedBy = "route")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<CheapestMarker> cheapestMarkers;

    @OneToMany(mappedBy = "route")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<FastestMarker> fastestMarkers;
    @Column(length = 5000)
    private String cheapestPolyline;
    @Column(length = 5000)
    private String fastestPolyline;
    private int totalCheapestTime;
    private int totalFastestTime;

    private double totalCheapestCost;
    private double totalFastestCost;

    public Route() {
        this.creationDate=LocalDateTime.now();
        List<CheapestMarker> listC =new ArrayList<>();
        this.cheapestMarkers= listC;
        List<FastestMarker> listF =new ArrayList<>();
        this.fastestMarkers= listF;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
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

    public List<CheapestMarker> getCheapestMarkers() {
        return cheapestMarkers;
    }

    public void setCheapestMarkers(List<CheapestMarker> cheapestMarkers) {
        this.cheapestMarkers = cheapestMarkers;
    }

    public List<FastestMarker> getFastestMarkers() {
        return fastestMarkers;
    }

    public void setFastestMarkers(List<FastestMarker> fastestMarkers) {
        this.fastestMarkers = fastestMarkers;
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

    public double getTotalCheapestCost() {
        return totalCheapestCost;
    }

    public void setTotalCheapestCost(double totalCheapestCost) {
        this.totalCheapestCost = totalCheapestCost;
    }

    public double getTotalFastestCost() {
        return totalFastestCost;
    }

    public void setTotalFastestCost(double totalFastestCost) {
        this.totalFastestCost = totalFastestCost;
    }
}
