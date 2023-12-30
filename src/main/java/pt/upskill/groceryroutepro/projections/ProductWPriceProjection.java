package pt.upskill.groceryroutepro.projections;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public interface ProductWPriceProjection {

        Long getProductId();
        String getBrand();
        String getImageUrl();
        String getName();
        String getQuantity();
        String getChain();
        Long getPriceId();
        Timestamp getPriceCollectionDate();
        int getPriceDiscountPercentage();
        String getPriceWoDiscount();
        String getPricePrimaryUnit();
        double getPricePrimaryValue();
        String getPriceSecondaryUnit();
        double getPriceSecondaryValue();

}
