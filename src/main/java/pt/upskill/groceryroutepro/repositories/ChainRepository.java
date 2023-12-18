package pt.upskill.groceryroutepro.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.upskill.groceryroutepro.entities.Category;
import pt.upskill.groceryroutepro.entities.Chain;

@Repository
public interface ChainRepository extends JpaRepository<Chain,Long> {
    Chain findByName(String chainName);

}
