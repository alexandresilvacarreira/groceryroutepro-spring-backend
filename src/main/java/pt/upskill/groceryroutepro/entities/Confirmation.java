package pt.upskill.groceryroutepro.entities;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Confirmation {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private String code;
    private LocalDateTime createdDate;
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
