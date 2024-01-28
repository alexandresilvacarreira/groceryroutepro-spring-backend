package pt.upskill.groceryroutepro.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.upskill.groceryroutepro.entities.Price;

@Repository
public interface PriceRepository extends JpaRepository<Price,Long> {

}
