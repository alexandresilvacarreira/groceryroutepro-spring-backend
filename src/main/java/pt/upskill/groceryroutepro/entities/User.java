package pt.upskill.groceryroutepro.entities;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String password;
    private boolean includeWhiteLabel = true;
    private double maxStoreRadiusKm = 5.5;
    private String vehicleFuelType;
    private double vehicleConsumption;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "user_stores",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "store_id")
    )
    private Set<Store> stores = new HashSet<>();

    @ManyToOne
    private Role role;

    public User() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isIncludeWhiteLabel() {
        return includeWhiteLabel;
    }

    public void setIncludeWhiteLabel(boolean include_white_label) {
        this.includeWhiteLabel = include_white_label;
    }

    public double getMaxStoreRadiusKm() {
        return maxStoreRadiusKm;
    }

    public void setMaxStoreRadiusKm(double max_store_radius_km) {
        this.maxStoreRadiusKm = max_store_radius_km;
    }

    public String getVehicleFuelType() {
        return vehicleFuelType;
    }

    public void setVehicleFuelType(String vehicle_fuel_type) {
        this.vehicleFuelType = vehicle_fuel_type;
    }

    public double getVehicleConsumption() {
        return vehicleConsumption;
    }

    public void setVehicleConsumption(double vehicle_consumption) {
        this.vehicleConsumption = vehicle_consumption;
    }

    public Set<Store> getStores() {
        return stores;
    }

    public void setStores(Set<Store> stores) {
        this.stores = stores;
    }
}
