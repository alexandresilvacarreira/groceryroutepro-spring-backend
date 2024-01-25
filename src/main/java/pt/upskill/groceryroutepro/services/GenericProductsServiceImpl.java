package pt.upskill.groceryroutepro.services;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import pt.upskill.groceryroutepro.entities.*;
import pt.upskill.groceryroutepro.exceptions.types.BadRequestException;
import pt.upskill.groceryroutepro.projections.ProductWPriceProjection;
import pt.upskill.groceryroutepro.repositories.ChainRepository;
import pt.upskill.groceryroutepro.repositories.GenericProductRepository;
import pt.upskill.groceryroutepro.repositories.PriceRepository;
import pt.upskill.groceryroutepro.repositories.ProductRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class GenericProductsServiceImpl implements GenericProductsService {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    GenericProductRepository genericProductRepository;

    @Autowired
    PriceRepository priceRepository;

    @Autowired
    ChainRepository chainRepository;


    @Override
    public GenericProduct getGenericProductById(Long genericProductId) {
        GenericProduct genericProduct = genericProductRepository.findById(genericProductId).get();
        if (genericProduct == null) throw new BadRequestException("Produto não encontrado.");
        return genericProduct;
    }

    @Override
    public Slice<GenericProduct> getGenericProductsByParams(String search, String processedSearch, List<Long> categoryIds, List<Long> chainIds, Pageable pageable) {
        Slice<GenericProduct> genericProducts = genericProductRepository.findGenericProductByParams(search, processedSearch, categoryIds, chainIds, pageable);
        if (genericProducts == null) throw new BadRequestException("Erro ao obter produtos.");
        return genericProducts;
    }


    private void saveProductInfo(GenericProduct genericProduct, Product product) {

        if (genericProduct.getProducts().isEmpty() || !genericProduct.getProducts().contains(product)) {
            genericProduct.getProducts().add(product);
            genericProduct.getChains().add(product.getChain());
            product.setGenericProduct(genericProduct);
        }

        Set<Category> productCategories = product.getCategories();
        Set<Category> genericProductCategories = genericProduct.getCategories();

        for (Category category : productCategories) {
            if (!genericProductCategories.contains(category)) {
                genericProductCategories.add(category);
            }
        }

        List<Price> productPrices = product.getPrices();
        Price currentProductPriceToSave = productPrices.get(productPrices.size() - 1);
        Price currentProductPrice = priceRepository.findById(currentProductPriceToSave.getId()).get();

        Price currentGenericProductPrice = genericProduct.getCurrentLowestPrice();

        if (currentGenericProductPrice == null || currentProductPrice.getPrimaryValue() <= currentGenericProductPrice.getPrimaryValue()) {
            genericProduct.setCurrentLowestPrice(currentProductPrice);
            genericProduct.setCurrentLowestPricePrimaryValue(currentProductPrice.getPrimaryValue());
            genericProduct.setCurrentCheapestProduct(product);
            currentProductPrice.setGenericProduct(genericProduct);
            product.setCheapestForGenericProduct(genericProduct);
            priceRepository.save(currentProductPrice);
        }

    }

    @Override
    public void createMergedTable() {

        // Usamos o Pingo Doce para popular a tabela com os primeiros GenericProducts uma vez que é a loja com os dados de melhor qualidade

        List<Product> products = productRepository.findByChain(chainRepository.findByName("pingo doce"));

        for (Product product : products) {


            String processedBrand = null;
            String processedQuantity = null;

            // Quantidade

            String productQuantity = product.getQuantity();

            if (!productQuantity.equals("")) {
                Pattern pattern = Pattern.compile("\\d+");
                Matcher matcher = pattern.matcher(product.getQuantity());
                if (matcher.find()) {
                    String firstNumber = matcher.group();
                    processedQuantity = firstNumber;
                }
            }

            // Marca

            String productBrand = product.getBrand().toLowerCase();
            if (!productBrand.equals("")) {
                processedBrand = productBrand.replaceAll("\\s", "").toLowerCase();
            }

            // Nome

            String productName = product.getName().toLowerCase();
            String processedName = productName.replace(productBrand, "").replaceAll("\\s", "");

            String genericProductName = StringUtils.capitalize(productName.replace(productBrand, ""));
            String genericProductBrand = productBrand;
            String genericProductQuantity = productQuantity;

            GenericProduct genericProduct = new GenericProduct();
            genericProduct.setName(genericProductName);
            genericProduct.setBrand(productBrand);
            genericProduct.setQuantity(productQuantity);
            genericProduct.setProcessedName(processedName);
            genericProduct.setProcessedBrand(processedBrand);
            genericProduct.setProcessedQuantity(processedQuantity);

            this.saveProductInfo(genericProduct, product);

        }

    }

    @Override
    public void mergeToGenericTable(String chainName) {

//        List<Product> products = productRepository.findByChain(chainRepository.findByName(chainName));
        List<Product> products = productRepository.findByChainAndNotNullGenericProduct(chainRepository.findByName(chainName));
//        List<Product> products = productRepository.findByChainAndNullGenericProduct(chainRepository.findByName(chainName));
        List<GenericProduct> genericProductsList = new ArrayList<>(genericProductRepository.findAll());

        int batchSize = 10000;

        for (int i = 0; i < products.size(); i += batchSize) {

            int endIndex = Math.min(i + batchSize, products.size());
            List<Product> batch = products.subList(i, endIndex);

            List<GenericProduct> genericProductsToSave = new ArrayList<>();

            for (Product product : batch) {

                String processedBrand = null;
                String processedQuantity = null;

                // Quantidade

                String productQuantity = product.getQuantity();

                if (!productQuantity.equals("")) {
                    Pattern pattern = Pattern.compile("\\d+");
                    Matcher matcher = pattern.matcher(product.getQuantity());
                    if (matcher.find()) {
                        String firstNumber = matcher.group();
                        processedQuantity = firstNumber;
                    }
                }

                // Marca

                String productBrand = "";
                if (!chainName.equals("auchan")) { // Não temos info da marca da Auchan
                    productBrand = product.getBrand().toLowerCase();
                    if (!productBrand.equals("")) {
                        processedBrand = productBrand.replaceAll("\\s", "");
                    }
                }

                // Nome

                String processedName = "";
                String genericProductName = "";

                String productName = product.getName().toLowerCase();

                switch (chainName) {
                    case "auchan":
                        if (!productQuantity.equals("")) {
                            productName = productName.replace(productQuantity, "");
                        }
                        break;
                    case "minipreço":
                        if (!productBrand.equals("")) {
                            productName = productName.replace(productBrand, "");
                        }
                        if (processedQuantity != null && !processedQuantity.equals(""))
                            productName = productName.replace(processedQuantity, "");
                        break;
                }

                processedName = productName.replaceAll("\\s", "");
                genericProductName = StringUtils.capitalize(productName);

//            List<GenericProduct> genericProductsList = new ArrayList<>(genericProductRepository.findAll());

                for (int j = 0; j < genericProductsList.size(); j++) {

                    GenericProduct genericProduct = genericProductsList.get(j);

                    int nameDistance = 999;
                    int brandDistance = 999;
                    int quantityDistance = 999;
                    String genericProcessedName = genericProduct.getProcessedName();
                    String genericProcessedBrand = genericProduct.getProcessedBrand();
                    String genericProcessedQuantity = genericProduct.getProcessedQuantity();

                    if (genericProcessedName != null && processedName != null) {
                        nameDistance = LevenshteinDistance.getDefaultInstance().apply(genericProcessedName, processedName);
                    }

                    if (!chainName.equals("auchan") && genericProcessedBrand != null && processedBrand != null) {
                        brandDistance = LevenshteinDistance.getDefaultInstance().apply(genericProcessedBrand, processedBrand);
                    }

                    if (genericProcessedQuantity != null && processedQuantity != null) {
                        quantityDistance = LevenshteinDistance.getDefaultInstance().apply(genericProcessedQuantity, processedQuantity);
                    }

                    boolean hasSameCategory = false;
                    Set<Category> productCategories = product.getCategories();
                    Set<Category> genericProductCategories = genericProduct.getCategories();

                    for (Category category : productCategories) {
                        if (genericProductCategories.contains(category)) {
                            hasSameCategory = true;
                            break;
                        }
                    }

                    GenericProduct genericProductToSave = genericProduct;
                    boolean canBeSaved = false;

                    if (chainName.equals("auchan")) { // Auchan não tem informação da marca discriminada, a comparação é ligeiramente diferente
                        // Os else-if são só conjuntos de critérios com diferente prioridade
                        // para fazer o merge
                        boolean containsBrandInName = false;
                        if (genericProcessedBrand != null && !genericProcessedBrand.equals("")) {
                            containsBrandInName = processedName.lastIndexOf(genericProcessedBrand) != -1;
                        }
                        if (nameDistance == 0 && containsBrandInName && quantityDistance == 0 && hasSameCategory) {
                            canBeSaved = true;
                        } else if (nameDistance <= 2 && containsBrandInName && quantityDistance == 0 && hasSameCategory) {
                            canBeSaved = true;
//                    } else if (nameDistance <= 5 && containsBrandInName && quantityDistance == 0 && hasSameCategory) {
//                        canBeSaved = true;
//                    } else if (nameDistance <= 7 && containsBrandInName && quantityDistance <= 1 && hasSameCategory) {
//                        canBeSaved = true;
                        } else if (j == genericProductsList.size() - 1) {
                            // Não foi apanhado por nenhum dos critérios, por isso é um produto novo
                            genericProductToSave = new GenericProduct();
                            genericProductToSave.setName(genericProductName);
                            genericProductToSave.setBrand(productBrand);
                            genericProductToSave.setQuantity(productQuantity);
                            genericProductToSave.setProcessedName(processedName);
                            genericProductToSave.setProcessedBrand(processedBrand);
                            genericProductToSave.setProcessedQuantity(processedQuantity);
                            canBeSaved = true;
                        }
                    } else {
                        // Os else-if são só conjuntos de critérios com diferente prioridade
                        // para fazer o merge
                        if (nameDistance == 0 && brandDistance == 0 && quantityDistance == 0 && hasSameCategory) {
                            canBeSaved = true;
                        } else if (nameDistance <= 2 && brandDistance <= 2 && quantityDistance == 0 && hasSameCategory) {
                            canBeSaved = true;
//                    } else if (nameDistance <= 5 && brandDistance == 0 && quantityDistance == 0 && hasSameCategory) {
//                        canBeSaved = true;
                        } else if (j == genericProductsList.size() - 1) {
                            // Não foi apanhado por nenhum dos critérios, por isso é um produto novo
                            genericProductToSave = new GenericProduct();
                            genericProductToSave.setName(genericProductName);
                            genericProductToSave.setBrand(productBrand);
                            genericProductToSave.setQuantity(productQuantity);
                            genericProductToSave.setProcessedName(processedName);
                            genericProductToSave.setProcessedBrand(processedBrand);
                            genericProductToSave.setProcessedQuantity(processedQuantity);
                            canBeSaved = true;
                        }
                    }

                    if (canBeSaved) {

                        if (genericProductToSave.getProducts().isEmpty() || !genericProductToSave.getProducts().contains(product)) {
                            genericProductToSave.getProducts().add(product);
                            Set<Chain> genericProductToSaveChains = genericProductToSave.getChains();
                            Chain productChain = product.getChain();
                            if (!genericProductToSaveChains.isEmpty() && !genericProductToSaveChains.contains(productChain)) {
                                genericProductToSave.getChains().add(productChain);
                            }
                            product.setGenericProduct(genericProductToSave);
                        }

                        for (Category category : productCategories) {
                            if (!genericProductCategories.contains(category)) {
                                genericProductCategories.add(category);
                            }
                        }

                        // Verificar e atualizar preços
//                        List<Product> productsInGenericProduct = new ArrayList<>(genericProductToSave.getProducts());
//                        for (Product productInGenericProduct : productsInGenericProduct) {
//
//                            List<Price> productPrices = productInGenericProduct.getPrices();
//                            Price currentProductPrice = productPrices.get(productPrices.size() - 1); // O último da lista é sempre o mais recente
//
//                            Price currentGenericProductPrice = genericProductToSave.getCurrentLowestPrice();
//                            Product currentCheapestProduct = genericProductToSave.getCurrentCheapestProduct();
//
//                            if (currentGenericProductPrice == null || currentProductPrice.getPrimaryValue() < currentGenericProductPrice.getPrimaryValue()) {
//
//                                // Elminar o preço e produtos antigos, caso contrário será enviada uma exceção porque a relação é one-to-one
//                                if (currentGenericProductPrice != null && currentCheapestProduct != null) {
//                                    currentGenericProductPrice.setGenericProduct(null);
//                                    currentCheapestProduct.setCheapestForGenericProduct(null);
////                                productRepository.save(currentCheapestProduct);
////                                priceRepository.save(currentGenericProductPrice);
//                                }
//
//                                // Atualizar GenericProduct
//                                genericProductToSave.setCurrentLowestPrice(currentProductPrice);
//                                genericProductToSave.setCurrentLowestPricePrimaryValue(currentProductPrice.getPrimaryValue());
//                                genericProductToSave.setCurrentCheapestProduct(productInGenericProduct);
//                                currentProductPrice.setGenericProduct(genericProductToSave);
//                                productInGenericProduct.setCheapestForGenericProduct(genericProductToSave);
//                            }
//                        }
                        genericProductsList.add(genericProductToSave);
                        genericProductsToSave.add(genericProductToSave);
                        break;
                    }
                }
            }
            genericProductRepository.saveAll(genericProductsToSave);
        }
    }

    @Override
    public void updateGenericProductPrices() {

        int batchSize = 10000;

        List<GenericProduct> genericProducts = genericProductRepository.findAllByCurrentCheapestProductIsNull();

//        List<GenericProduct> genericProducts = genericProductRepository.findAll();

        for (int i = 0; i < genericProducts.size(); i += batchSize) {

            int endIndex = Math.min(i + batchSize, genericProducts.size());
            List<GenericProduct> batch = genericProducts.subList(i, endIndex);

//            List<Product> products = new ArrayList<>(productRepository.findByGenericProductIn(batch));

            for (GenericProduct genericProduct : batch) {

                List<Product> products = new ArrayList<>(genericProduct.getProducts());

                for (Product product : products) {

                    List<Price> productPrices = product.getPrices();
                    Price currentProductPrice = productPrices.get(productPrices.size() - 1); // O último da lista é sempre o mais recente

                    Price currentGenericProductPrice = genericProduct.getCurrentLowestPrice();
                    Product currentCheapestProduct = genericProduct.getCurrentCheapestProduct();

                    if (currentGenericProductPrice == null || currentProductPrice.getPrimaryValue() < currentGenericProductPrice.getPrimaryValue()) {

                        // Elminar o preço e produtos antigos, caso contrário será enviada uma exceção porque a relação é one-to-one
                        if (currentGenericProductPrice != null && currentCheapestProduct != null) {
                            currentGenericProductPrice.setGenericProduct(null);
                            currentCheapestProduct.setCheapestForGenericProduct(null);
//                            productRepository.save(currentCheapestProduct);
//                            priceRepository.save(currentGenericProductPrice);
                        }

                        // Atualizar GenericProduct
                        genericProduct.setCurrentLowestPrice(currentProductPrice);
                        genericProduct.setCurrentLowestPricePrimaryValue(currentProductPrice.getPrimaryValue());
                        genericProduct.setCurrentCheapestProduct(product);
                        currentProductPrice.setGenericProduct(genericProduct);
                        product.setCheapestForGenericProduct(genericProduct);
                    }
                }
//                productRepository.saveAll(products); //TODO experimentar sem isto
            }
            genericProductRepository.saveAll(batch);
        }
    }

    @Override
    public void updateGenericProducts(Product product) {
        GenericProduct genericProduct = product.getGenericProduct();
        if (genericProduct != null) {
            // Se o produto já tem GenericProduct associado, temos apenas de verificar o preço
            this.updateGenericProductPrice(genericProductRepository.findById(genericProduct.getId()).get());
        } else {
            // Se não existe, tentamos categorizar/criar novo produto
            this.matchToGenericProduct(product);
        }
    }

    private void updateGenericProductPrice(GenericProduct genericProduct) {

        List<Product> products = new ArrayList<>(genericProduct.getProducts());

        for (Product product : products) {

            List<Price> productPrices = product.getPrices();
            Price currentProductPrice = productPrices.get(productPrices.size() - 1); // O último da lista é sempre o mais recente

            Price currentGenericProductPrice = genericProduct.getCurrentLowestPrice();
            Product currentCheapestProduct = genericProduct.getCurrentCheapestProduct();

            if (currentGenericProductPrice == null || currentProductPrice.getPrimaryValue() < currentGenericProductPrice.getPrimaryValue()) {

                // Elminar o preço e produtos antigos, caso contrário será enviada uma exceção porque a relação é one-to-one
                if (currentGenericProductPrice != null && currentCheapestProduct != null) {
                    currentGenericProductPrice.setGenericProduct(null);
                    currentCheapestProduct.setCheapestForGenericProduct(null);
                }

                // Atualizar GenericProduct
                genericProduct.setCurrentLowestPrice(currentProductPrice);
                genericProduct.setCurrentLowestPricePrimaryValue(currentProductPrice.getPrimaryValue());
                genericProduct.setCurrentCheapestProduct(product);
                currentProductPrice.setGenericProduct(genericProduct);
                product.setCheapestForGenericProduct(genericProduct);
            }
        }
        genericProductRepository.save(genericProduct);
    }

    private void matchToGenericProduct(Product product) {

        String processedBrand = null;
        String processedQuantity = null;

        // Quantidade

        String productQuantity = product.getQuantity();

        if (!productQuantity.equals("")) {
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(product.getQuantity());
            if (matcher.find()) {
                String firstNumber = matcher.group();
                processedQuantity = firstNumber;
            }
        }

        // Marca

        String chainName = product.getChain().getName();

        String productBrand = "";
        if (!chainName.equals("auchan")) { // Não temos info da marca da Auchan
            productBrand = product.getBrand().toLowerCase();
            if (!productBrand.equals("")) {
                processedBrand = productBrand.replaceAll("\\s", "");
            }
        }

        // Nome

        String processedName = "";
        String genericProductName = "";

        String productName = product.getName().toLowerCase();

        switch (chainName) {
            case "auchan":
                if (!productQuantity.equals("")) {
                    productName = productName.replace(productQuantity, "");
                }
                break;
            case "minipreço":
                if (!productBrand.equals("")) {
                    productName = productName.replace(productBrand, "");
                }
                if (processedQuantity != null && !processedQuantity.equals(""))
                    productName = productName.replace(processedQuantity, "");
                break;
        }

        processedName = productName.replaceAll("\\s", "");
        genericProductName = StringUtils.capitalize(productName);

        List<GenericProduct> genericProductsList = new ArrayList<>(genericProductRepository.findAll());

        for (int i = 0; i < genericProductsList.size(); i++) {

            GenericProduct genericProduct = genericProductsList.get(i);

            int nameDistance = 999;
            int brandDistance = 999;
            int quantityDistance = 999;
            String genericProcessedName = genericProduct.getProcessedName();
            String genericProcessedBrand = genericProduct.getProcessedBrand();
            String genericProcessedQuantity = genericProduct.getProcessedQuantity();

            if (genericProcessedName != null && processedName != null) {
                nameDistance = LevenshteinDistance.getDefaultInstance().apply(genericProcessedName, processedName);
            }

            if (!chainName.equals("auchan") && genericProcessedBrand != null && processedBrand != null) {
                brandDistance = LevenshteinDistance.getDefaultInstance().apply(genericProcessedBrand, processedBrand);
            }

            if (genericProcessedQuantity != null && processedQuantity != null) {
                quantityDistance = LevenshteinDistance.getDefaultInstance().apply(genericProcessedQuantity, processedQuantity);
            }

            boolean hasSameCategory = false;
            Set<Category> productCategories = product.getCategories();
            Set<Category> genericProductCategories = genericProduct.getCategories();

            for (Category category : productCategories) {
                if (genericProductCategories.contains(category)) {
                    hasSameCategory = true;
                    break;
                }
            }

            GenericProduct genericProductToSave = genericProduct;
            boolean canBeSaved = false;

            if (chainName.equals("auchan")) { // Auchan não tem informação da marca discriminada, a comparação é ligeiramente diferente
                // Os else-if são só conjuntos de critérios com diferente prioridade
                // para fazer o merge
                boolean containsBrandInName = false;
                if (genericProcessedBrand != null && !genericProcessedBrand.equals("")) {
                    containsBrandInName = processedName.lastIndexOf(genericProcessedBrand) != -1;
                }
                if (nameDistance == 0 && containsBrandInName && quantityDistance == 0 && hasSameCategory) {
                    canBeSaved = true;
                } else if (nameDistance <= 2 && containsBrandInName && quantityDistance == 0 && hasSameCategory) {
                    canBeSaved = true;
//                    } else if (nameDistance <= 5 && containsBrandInName && quantityDistance == 0 && hasSameCategory) {
//                        canBeSaved = true;
//                    } else if (nameDistance <= 7 && containsBrandInName && quantityDistance <= 1 && hasSameCategory) {
//                        canBeSaved = true;
                } else if (i == genericProductsList.size() - 1) {
                    // Não foi apanhado por nenhum dos critérios, por isso é um produto novo
                    genericProductToSave = new GenericProduct();
                    genericProductToSave.setName(genericProductName);
                    genericProductToSave.setBrand(productBrand);
                    genericProductToSave.setQuantity(productQuantity);
                    genericProductToSave.setProcessedName(processedName);
                    genericProductToSave.setProcessedBrand(processedBrand);
                    genericProductToSave.setProcessedQuantity(processedQuantity);
                    canBeSaved = true;
                }
            } else {
                // Os else-if são só conjuntos de critérios com diferente prioridade
                // para fazer o merge
                if (nameDistance == 0 && brandDistance == 0 && quantityDistance == 0 && hasSameCategory) {
                    canBeSaved = true;
                } else if (nameDistance <= 2 && brandDistance <= 2 && quantityDistance == 0 && hasSameCategory) {
                    canBeSaved = true;
//                    } else if (nameDistance <= 5 && brandDistance == 0 && quantityDistance == 0 && hasSameCategory) {
//                        canBeSaved = true;
                } else if (i == genericProductsList.size() - 1) {
                    // Não foi apanhado por nenhum dos critérios, por isso é um produto novo
                    genericProductToSave = new GenericProduct();
                    genericProductToSave.setName(genericProductName);
                    genericProductToSave.setBrand(productBrand);
                    genericProductToSave.setQuantity(productQuantity);
                    genericProductToSave.setProcessedName(processedName);
                    genericProductToSave.setProcessedBrand(processedBrand);
                    genericProductToSave.setProcessedQuantity(processedQuantity);
                    canBeSaved = true;
                }
            }

            if (canBeSaved) {

                if (genericProductToSave.getProducts().isEmpty() || !genericProductToSave.getProducts().contains(product)) {
                    genericProductToSave.getProducts().add(product);
                    Set<Chain> genericProductToSaveChains = genericProductToSave.getChains();
                    Chain productChain = product.getChain();
                    if (!genericProductToSaveChains.isEmpty() && !genericProductToSaveChains.contains(productChain)) {
                        genericProductToSave.getChains().add(productChain);
                    }
                    product.setGenericProduct(genericProductToSave);
                }

                for (Category category : productCategories) {
                    if (!genericProductCategories.contains(category)) {
                        genericProductCategories.add(category);
                    }
                }
                genericProductRepository.save(genericProductToSave);
                // Atualizar preço
                this.updateGenericProductPrice(genericProductToSave);
                break;
            }
        }
    }

}