package pt.upskill.groceryroutepro.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.upskill.groceryroutepro.entities.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {
    Role findByName(String roleName);

}
