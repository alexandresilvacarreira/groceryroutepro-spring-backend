package pt.upskill.groceryroutepro.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.upskill.groceryroutepro.entities.Category;
import pt.upskill.groceryroutepro.entities.Route;

@Repository
public interface RouteRepository extends JpaRepository<Route,Long> {


}
