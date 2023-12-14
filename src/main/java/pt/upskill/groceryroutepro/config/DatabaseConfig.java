package pt.upskill.groceryroutepro.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pt.upskill.groceryroutepro.entities.Role;
import pt.upskill.groceryroutepro.repositories.RoleRepository;

@Configuration
public class DatabaseConfig {

    @Autowired
    private RoleRepository roleRepository;

    public DatabaseConfig() {
    }

    @Bean
    public CommandLineRunner initRoles() {
        return args -> {
            initializeRoles();
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

}
