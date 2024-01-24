package pt.upskill.groceryroutepro.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;


import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private String name;
    @JsonIgnore
    private String email;
    @JsonIgnore
    private String password;
    private boolean includeWhiteLabel = true;
    private double maxStoreRadiusKm = 5.5;
    private String vehicleFuelType;
    private double vehicleConsumption;

    private boolean verifiedEmail;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Confirmation confirmation;

    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "user_stores",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "store_id")
    )
    private Set<Store> stores = new HashSet<>();

    @ManyToOne
    private Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private PasswordLink passwordLink;

    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "current_shopping_list_id")
    private ShoppingList currentShoppingList;

    @OneToMany(mappedBy = "user", cascade = {CascadeType.ALL},fetch = FetchType.EAGER)
    @JsonIgnore
    private List<ShoppingList> shoppingLists;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Route> routes = new ArrayList<>();

    public User() {
    }

    public ShoppingList getCurrentShoppingList() {
        return currentShoppingList;
    }

    public void setCurrentShoppingList(ShoppingList currentShoppingList) {
        this.currentShoppingList = currentShoppingList;
    }

    public List<ShoppingList> getShoppingLists() {
        return shoppingLists;
    }

    public void setShoppingLists(List<ShoppingList> shoppingLists) {
        this.shoppingLists = shoppingLists;
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


    public Confirmation getConfirmation() {
        return confirmation;
    }

    public void setConfirmation(Confirmation confirmation) {
        this.confirmation = confirmation;
    }

    public boolean isVerifiedEmail() {
        return verifiedEmail;
    }

    public void setVerifiedEmail(boolean verifiedEmail) {
        this.verifiedEmail = verifiedEmail;
    }

    public PasswordLink getPasswordLink() {
        return passwordLink;
    }

    public void setPasswordLink(PasswordLink passwordLink) {
        this.passwordLink = passwordLink;
    }

    @Override
    public String toString() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(this);
        } catch (Exception e) {
            return super.toString();
        }
    }
}
