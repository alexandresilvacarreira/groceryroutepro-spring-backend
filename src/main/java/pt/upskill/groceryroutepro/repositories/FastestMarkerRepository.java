package pt.upskill.groceryroutepro.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.upskill.groceryroutepro.entities.FastestMarker;

@Repository
public interface FastestMarkerRepository extends JpaRepository<FastestMarker,Long> {


}
