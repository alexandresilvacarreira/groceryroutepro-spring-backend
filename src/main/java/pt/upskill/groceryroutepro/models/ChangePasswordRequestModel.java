package pt.upskill.groceryroutepro.models;

public class ChangePasswordRequestModel {
    private String password;

    private String token;

    public ChangePasswordRequestModel() {
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
