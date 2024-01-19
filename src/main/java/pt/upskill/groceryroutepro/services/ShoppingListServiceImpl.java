package pt.upskill.groceryroutepro.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pt.upskill.groceryroutepro.entities.*;
import pt.upskill.groceryroutepro.exceptions.types.BadRequestException;
import pt.upskill.groceryroutepro.exceptions.types.UnauthorizedException;
import pt.upskill.groceryroutepro.models.EmailVerificationToken;
import pt.upskill.groceryroutepro.models.SignUp;
import pt.upskill.groceryroutepro.repositories.*;
import pt.upskill.groceryroutepro.utils.Enum.EmailType;

import java.util.*;
import java.util.stream.Collectors;

import static pt.upskill.groceryroutepro.utils.Validator.isExpired;
import static pt.upskill.groceryroutepro.utils.Validator.verifyToken;

@Service
public class ShoppingListServiceImpl implements ShoppingListService {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GenericProductRepository genericProductRepository;

    @Autowired
    ShoppingListRepository shoppingListRepository;

    @Autowired
    ProductQuantityGenericRepository productQuantityGenericRepository;

    @Autowired
    ProductQuantityFastestRepository productQuantityFastestRepository;

    @Autowired
    ProductQuantityCheapestRepository productQuantityCheapestRepository;

    @Autowired
    ChainRepository chainRepository;

    private List<Chain> chains = new ArrayList<>(chainRepository.findAll());


    @Override
    public void addProduct(Long genericProductId) {

        User user = userService.getAuthenticatedUser();

        if (user == null) {
            throw new BadRequestException("Utilizador não encontrado");
        }

        ShoppingList shoppingList = shoppingListRepository.findFirstByCurrentShoppingListForUser(user);

        if (shoppingList == null) {
            shoppingList = new ShoppingList();
            shoppingList.setCurrentShoppingListForUser(user);
            user.getShoppingLists().add(shoppingList);
        }

        // Adicionar o produto à lista genérica
        GenericProduct genericProductToAdd = genericProductRepository.findById(genericProductId).get();
        List<ProductQuantityGeneric> genericProductQuantities = shoppingList.getGenericProductQuantities();
        int genericProductQuantitiesSize = genericProductQuantities.size();

        for (int i = 0; i < genericProductQuantitiesSize; i++) {
            ProductQuantityGeneric genericProductQuantity = genericProductQuantities.get(i);
            if (genericProductQuantity.getGenericProduct().getId().equals(genericProductId)){
                // Se o produto já existe na lista, é só preciso atualizar a quantidade
                genericProductQuantity.setQuantity(genericProductQuantity.getQuantity() + 1);
                productQuantityGenericRepository.save(genericProductQuantity);
                break;
            } else if(i == genericProductQuantitiesSize -1) {
                // Se não existe na lista, adicionar
                ProductQuantityGeneric productQuantityGeneric = new ProductQuantityGeneric();
                productQuantityGeneric.setGenericProduct(genericProductToAdd);
                productQuantityGeneric.setShoppingList(shoppingList);
                productQuantityGeneric.setQuantity(1);
                genericProductQuantities.add(productQuantityGeneric);
                shoppingList.getGenericProductQuantities().add(productQuantityGeneric);
                productQuantityGenericRepository.save(productQuantityGeneric);
            }
        }
        
        /////////////////////// Gerar listas mais rápida e mais barata /////////////////////////////////
        
        // Se só houver um produto na lista, a lista mais rápida e mais barata são iguais
        if (genericProductQuantities.size() == 1){
            
            ProductQuantityGeneric genericProductQuantity = genericProductQuantities.get(0);
            GenericProduct genericProduct = genericProductQuantity.getGenericProduct();
            Product currentCheapestProduct = genericProduct.getCurrentCheapestProduct();
            double listCost = genericProductQuantity.getQuantity() * genericProduct.getCurrentLowestPricePrimaryValue();
            
            ProductQuantityFastest productQuantityFastest = new ProductQuantityFastest();
            productQuantityFastest.setProduct(currentCheapestProduct);
            productQuantityFastest.setShoppingList(shoppingList);
            productQuantityFastest.setQuantity(genericProductQuantity.getQuantity());
            shoppingList.getFastestProductQuantities().add(productQuantityFastest);
            shoppingList.setFastestListCost(listCost);
            productQuantityFastestRepository.save(productQuantityFastest);

            ProductQuantityCheapest productQuantityCheapest = new ProductQuantityCheapest();
            productQuantityCheapest.setProduct(currentCheapestProduct);
            productQuantityCheapest.setShoppingList(shoppingList);
            productQuantityCheapest.setQuantity(genericProductQuantity.getQuantity());
            shoppingList.getCheapestProductQuantities().add(productQuantityCheapest);
            shoppingList.setCheapestListCost(listCost);
            productQuantityCheapestRepository.save(productQuantityCheapest);

        } else {

            ////////////////////// Criar lista de produtos "mais rápida" //////////////////////////////////

            // Inicializar lista para contagem de superfícies
            int[] countProductChains = new int[this.chains.size()];

            // Obter lista com todos os produtos
            List<Product> productsInList = genericProductQuantities.stream()
                    .flatMap(productQuantityGeneric -> productQuantityGeneric.getGenericProduct().getProducts().stream())
                    .collect(Collectors.toList());

            // Contar superfícies
            for(Product product : productsInList) {
                Chain productChain = product.getChain();
                int index = this.chains.indexOf(productChain);
                countProductChains[index] += 1;
            }

            // Criar lista com superfícies e contagem
            List<Map<String,Object>> countProductChainsList = new ArrayList<>();


            for (int i = 0; i < countProductChains.length; i++) {
                Map<String, Object> chainCounter = new HashMap<>();
                chainCounter.put("chain", this.chains.get(i));
                chainCounter.put("count", countProductChains[i]);
                countProductChainsList.add(chainCounter);
            }

            // Ordenar lista
            Collections.sort(countProductChainsList, Comparator.comparingInt(map -> (Integer)  map.get("count")));

            // Criar lista "mais rápida"
            List<ProductQuantityFastest> productQuantityFastestList = new ArrayList<>();
            double fastestListCost = 0;

            while (productQuantityFastestList.size() < genericProductQuantities.size()) {
                for (ProductQuantityGeneric genericProductQuantity : genericProductQuantities) {
                    List<Product> products = genericProductQuantity.getGenericProduct().getProducts();
                    for (Product product : products) {
                        for (int i = 0; i < countProductChainsList.size(); i++) {
                            if (product.getChain().equals(countProductChainsList.get(i).get("chain"))){
                                int quantity = genericProductQuantity.getQuantity();
                                Price currentPrice = product.getPrices().get(product.getPrices().size()-1);
                                ProductQuantityFastest productQuantityFastest = new ProductQuantityFastest();
                                productQuantityFastest.setProduct(product);
                                productQuantityFastest.setShoppingList(shoppingList);
                                productQuantityFastest.setQuantity(quantity);
                                productQuantityFastestList.add(productQuantityFastest);
                                fastestListCost += quantity * currentPrice.getPrimaryValue();
                                break;
                            }
                        }
                    }
                }
            }

            shoppingList.setFastestListCost(fastestListCost);
            productQuantityFastestRepository.saveAll(productQuantityFastestList);
            double cheapestListCost = 0;

            ////////////////////// Criar lista de produtos "mais barata" //////////////////////////////////

            List<ProductQuantityCheapest> productQuantityCheapestList = new ArrayList<>();

            for (ProductQuantityGeneric genericProductQuantity : genericProductQuantities){
                int quantity = genericProductQuantity.getQuantity();
                GenericProduct genericProduct = genericProductQuantity.getGenericProduct();
                Product currentCheapestProduct = genericProduct.getCurrentCheapestProduct();
                ProductQuantityCheapest productQuantityCheapest = new ProductQuantityCheapest();
                productQuantityCheapest.setProduct(currentCheapestProduct);
                productQuantityCheapest.setShoppingList(shoppingList);
                productQuantityCheapest.setQuantity(genericProductQuantity.getQuantity());
                cheapestListCost += quantity * genericProduct.getCurrentLowestPricePrimaryValue();
            }

            shoppingList.setCheapestListCost(cheapestListCost);
            productQuantityCheapestRepository.saveAll(productQuantityCheapestList);
        }


        shoppingListRepository.save(shoppingList);
        userRepository.save(user);
    }

    @Override
    public ShoppingList getCurrentShoppingList() {
        User user = userService.getAuthenticatedUser();
        if (user == null) {
            throw new BadRequestException("Utilizador não encontrado");
        }
        ShoppingList shoppingList = shoppingListRepository.findFirstByCurrentShoppingListForUser(user);
        return shoppingList;
    }
}