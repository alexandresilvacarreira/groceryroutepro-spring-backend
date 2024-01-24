package pt.upskill.groceryroutepro.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.upskill.groceryroutepro.entities.CheapestMarker;

@Repository
public interface CheapestMarkerRepository extends JpaRepository<CheapestMarker,Long> {


}
