package pt.upskill.groceryroutepro.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pt.upskill.groceryroutepro.entities.Role;
import pt.upskill.groceryroutepro.entities.Store;
import pt.upskill.groceryroutepro.repositories.RoleRepository;
import pt.upskill.groceryroutepro.repositories.StoreRepository;

@Configuration
public class DatabaseConfig {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private StoreRepository storeRepository;

    public DatabaseConfig() {
    }

    @Bean
    public CommandLineRunner initRoles() {
        return args -> {
            initializeRoles();
            initializeStores();
        };
    }

    private void initializeRoles() {
        createRoleIfNotExists("USER_FREE");
        createRoleIfNotExists("USER_PREMIUM");
        createRoleIfNotExists("ADMIN");
        createRoleIfNotExists("STORE");
    }

    private void createRoleIfNotExists(String roleName) {
        if (roleRepository.findByName(roleName) == null) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
        }
    }

    private void initializeStores() {
        createStoreIfNotExists("aldi");
        createStoreIfNotExists("continente");
        createStoreIfNotExists("lidl");
        createStoreIfNotExists("minipreço");
        createStoreIfNotExists("intermarché");
        createStoreIfNotExists("pingo doce");
    }

    private void createStoreIfNotExists(String storeName) {
        if (storeRepository.findByName(storeName) == null) {
            Store store = new Store();
            store.setName(storeName);
            storeRepository.save(store);
        }
    }

}
