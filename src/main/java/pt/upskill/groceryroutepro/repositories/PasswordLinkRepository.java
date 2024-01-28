package pt.upskill.groceryroutepro.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.upskill.groceryroutepro.entities.PasswordLink;

@Repository
public interface PasswordLinkRepository extends JpaRepository<PasswordLink,Long> {
    PasswordLink findByToken(String token);

    PasswordLink findByUser_Id(Long id);


}
