package pt.upskill.groceryroutepro.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pt.upskill.groceryroutepro.entities.Category;
import pt.upskill.groceryroutepro.entities.Chain;
import pt.upskill.groceryroutepro.entities.Role;
import pt.upskill.groceryroutepro.entities.Store;
import pt.upskill.groceryroutepro.repositories.CategoryRepository;
import pt.upskill.groceryroutepro.repositories.ChainRepository;
import pt.upskill.groceryroutepro.repositories.RoleRepository;
import pt.upskill.groceryroutepro.repositories.StoreRepository;

@Configuration
public class DatabaseConfig {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ChainRepository chainRepository;

    public DatabaseConfig() {
    }

    @Bean
    public CommandLineRunner initializeTables() {
        return args -> {
            initializeRoles();
            initializeStores();
            initializeCategories();
            initializeChains();
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

    private void initializeChains() {
        createChainIfNotExists("aldi");
        createChainIfNotExists("continente");
        createChainIfNotExists("lidl");
        createChainIfNotExists("minipreço");
        createChainIfNotExists("intermarché");
        createChainIfNotExists("pingo doce");
    }

    private void createChainIfNotExists(String chainName) {
        if (chainRepository.findByName(chainName) == null) {
            Chain chain = new Chain();
            chain.setName(chainName);
            chainRepository.save(chain);
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
            store.setChain(chainRepository.findByName(storeName));
            storeRepository.save(store);
        }
    }

    private void initializeCategories() {
        createCategoryIfNotExists("mercearia");
        createCategoryIfNotExists("frutas e legumes");
        createCategoryIfNotExists("congelados");
        createCategoryIfNotExists("mercearia");
        createCategoryIfNotExists("laticínios e ovos");
        createCategoryIfNotExists("peixaria");
        createCategoryIfNotExists("talho");
        createCategoryIfNotExists("charcutaria");
        createCategoryIfNotExists("alternativas alimentares, bio, saudável");
        createCategoryIfNotExists("bebidas");
        createCategoryIfNotExists("padaria e pastelaria");
    }

    private void createCategoryIfNotExists(String categoryName) {
        if (categoryRepository.findByName(categoryName) == null) {
            Category category = new Category();
            category.setName(categoryName);
            categoryRepository.save(category);
        }
    }

}
