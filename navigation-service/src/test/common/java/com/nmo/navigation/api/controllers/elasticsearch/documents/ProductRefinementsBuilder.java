package com.sixthday.navigation.api.controllers.elasticsearch.documents;

import com.sixthday.navigation.api.elasticsearch.models.PriceRangeAtg;
import com.sixthday.navigation.api.elasticsearch.models.ProductRefinements;

import java.math.BigDecimal;

public class ProductRefinementsBuilder {
    private boolean adornOnly;
    private boolean adornAndSaleOnly;
    private boolean regOnly;
    private boolean saleOnly;
    private PriceRangeAtg priceRange = new PriceRangeAtg("OFF", BigDecimal.ZERO, BigDecimal.ZERO);

    public ProductRefinements build() {
        ProductRefinements productRefinements = new ProductRefinements();
        productRefinements.setAdornAndSaleOnly(this.adornAndSaleOnly);
        productRefinements.setAdornOnly(this.adornOnly);
        productRefinements.setRegOnly(this.regOnly);
        productRefinements.setSaleOnly(this.saleOnly);
        productRefinements.setPriceRange(this.priceRange);
        return productRefinements;
    }

    public ProductRefinementsBuilder withRegOnly(boolean regOnly) {
        this.regOnly = regOnly;
        return this;
    }
}
