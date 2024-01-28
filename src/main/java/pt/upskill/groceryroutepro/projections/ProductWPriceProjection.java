package pt.upskill.groceryroutepro.projections;

import java.sql.Timestamp;

public interface ProductWPriceProjection {

        Long getProductId();
        String getBrand();
        String getImageUrl();
        String getName();
        String getQuantity();
        String getChain();
        Long getChainId();
        Long getPriceId();
        Long getGenericProductId();
        Timestamp getPriceCollectionDate();
        int getPriceDiscountPercentage();
        String getPriceWoDiscount();
        String getPricePrimaryUnit();
        double getPricePrimaryValue();
        String getPriceSecondaryUnit();
        double getPriceSecondaryValue();

}
