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

    private List<GenericProduct> genericProductsTemp = new ArrayList<>();


    @Override
    public void mergeProducts() {

        int pageSize = 1000;
        int pageNumber = 0;
        Slice<Product> productSlice;

        do {
            productSlice = productRepository.findAll(PageRequest.of(pageNumber++, pageSize));

            if (!productSlice.isEmpty()) {
                mergeProductChunk(productSlice.getContent());
            }
        } while (productSlice.hasNext());
    }

    @Transactional
    public void mergeProductChunk(List<Product> products) {

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

            String productBrand = product.getBrand();
            if (!productBrand.equals("")) {
                processedBrand = productBrand.replaceAll("\\s", "").toLowerCase();
            }

            // Nome

            String productName = product.getName();
            String processedName = productName.replaceAll("\\s", "").toLowerCase();
            String genericProductName = productName;

            switch (product.getChain().getName()) { // Alguns precisam de processamento adicional
                case "auchan":
                    processedName = productName.replace(productQuantity, "").replaceAll("\\s", "").toLowerCase();
                    genericProductName = productName.replace(productQuantity, "");
                    break;
                case "minipreço":
                    processedName = productName.replace(productBrand, "").replace(processedQuantity, "").replaceAll("\\s", "").toLowerCase();
                    genericProductName = processedName.toUpperCase();
                    break;
            }

            List<GenericProduct> genericProducts = this.genericProductRepository.findAll();

            if (!genericProducts.isEmpty()) {

                for (GenericProduct genericProduct : genericProducts) {

                    int nameDistance = 999;
                    int brandDistance = 999;
                    int quantityDistance = 999;
                    String genericProcessedName = genericProduct.getProcessedName();
                    String genericProcessedBrand = genericProduct.getProcessedBrand();
                    String genericProcessedQuantity = genericProduct.getProcessedQuantity();

                    if (genericProcessedName != null && processedName != null) {
                        nameDistance = LevenshteinDistance.getDefaultInstance().apply(genericProcessedName, processedName);
                    }

                    if (genericProcessedBrand != null && processedBrand != null) {
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

                    if (nameDistance == 0) {
                        this.saveProductInfo(genericProduct, product, false);
                        break;
                    } else if (nameDistance <= 10 && brandDistance <= 10 && quantityDistance <= 5 && hasSameCategory) {
                        this.saveProductInfo(genericProduct, product, false);
                        break;
                    } else {
                        GenericProduct newGenericProduct = new GenericProduct();
                        newGenericProduct.setName(genericProductName);
                        newGenericProduct.setBrand(productBrand);
                        newGenericProduct.setQuantity(productQuantity);
                        newGenericProduct.setProcessedName(processedName);
                        newGenericProduct.setProcessedBrand(processedBrand);
                        newGenericProduct.setProcessedQuantity(processedQuantity);
                        this.saveProductInfo(newGenericProduct, product, true);
                        break;
                    }
                }
            } else { // Caso seja o primeiro produto

                GenericProduct newGenericProduct = new GenericProduct();
                newGenericProduct.setName(genericProductName);
                newGenericProduct.setBrand(productBrand);
                newGenericProduct.setQuantity(productQuantity);
                newGenericProduct.setProcessedName(processedName);
                newGenericProduct.setProcessedBrand(processedBrand);
                newGenericProduct.setProcessedQuantity(processedQuantity);
                this.saveProductInfo(newGenericProduct, product, true);
                break;
            }
        }

    }

    @Override
    public GenericProduct getProductById(Long genericProductId) {
        return genericProductRepository.findById(genericProductId).get();
    }

    @Override
    public Slice<GenericProduct> getProductsByParams(String search, List<Long> categoryIds, List<Long> chainIds, Pageable pageable) {
        return genericProductRepository.findGenericProductByParams(search, categoryIds, chainIds, pageable);
    }

    private void saveProductInfo(GenericProduct genericProductToSave, Product productToSave, boolean isNewGenericProduct) {

        GenericProduct genericProduct = genericProductToSave;
        Product product = productRepository.findById(productToSave.getId()).get();

        if (!isNewGenericProduct) {
            genericProduct = genericProductRepository.findById(genericProductToSave.getId()).get();
        }

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

        if (isNewGenericProduct) {
            this.genericProductsTemp.add(genericProduct);
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

            this.saveProductInfo(genericProduct, product, true);

        }

    }

    @Override
    public void mergeToGenericTable(String chainName) {

        List<Product> products = productRepository.findByChain(chainRepository.findByName(chainName));

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
            String processedName = productName.replaceAll("\\s", "");
            String genericProductName = StringUtils.capitalize(productName);

            this.genericProductsTemp = genericProductRepository.findAll();

            for (int i = 0; i < this.genericProductsTemp.size(); i++) {

                GenericProduct genericProduct = this.genericProductsTemp.get(i);

                int nameDistance = 999;
                int brandDistance = 999;
                int quantityDistance = 999;
                String genericProcessedName = genericProduct.getProcessedName();
                String genericProcessedBrand = genericProduct.getProcessedBrand();
                String genericProcessedQuantity = genericProduct.getProcessedQuantity();

                if (genericProcessedName != null && processedName != null) {
                    nameDistance = LevenshteinDistance.getDefaultInstance().apply(genericProcessedName, processedName);
                }

                if (genericProcessedBrand != null && processedBrand != null) {
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
                boolean isNewGenericProduct = true;

                // Os else-if são só conjuntos de critérios com diferente prioridade
                // para fazer o merge
                if (nameDistance == 0 && brandDistance == 0) {
                    canBeSaved = true;
                } else if (nameDistance <= 3 && brandDistance == 0 && hasSameCategory) {
                    canBeSaved = true;
                } else if (nameDistance <= 5 && brandDistance == 0 && quantityDistance == 0 && hasSameCategory) {
                    canBeSaved = true;
                } else if (i == this.genericProductsTemp.size() - 1) {
                    // Não foi apanhado por nenhum dos critérios, por isso é um produto novo
                    genericProductToSave = new GenericProduct();
                    genericProductToSave.setName(genericProductName);
                    genericProductToSave.setBrand(productBrand);
                    genericProductToSave.setQuantity(productQuantity);
                    genericProductToSave.setProcessedName(processedName);
                    genericProductToSave.setProcessedBrand(processedBrand);
                    genericProductToSave.setProcessedQuantity(processedQuantity);
                    canBeSaved = true;
                    isNewGenericProduct = true;
                    genericProductToSave.setId(null);
                }

                if (canBeSaved) {

                    if (genericProductToSave.getProducts().isEmpty() || !genericProductToSave.getProducts().contains(product)) {
                        genericProductToSave.getProducts().add(product);
                        genericProductToSave.getChains().add(product.getChain());
                        product.setGenericProduct(genericProductToSave);
                    }


                    for (Category category : productCategories) {
                        if (!genericProductCategories.contains(category)) {
                            genericProductCategories.add(category);
                        }
                    }

                    genericProductRepository.save(genericProductToSave);

                    break;
                }
            }
        }

    }


}