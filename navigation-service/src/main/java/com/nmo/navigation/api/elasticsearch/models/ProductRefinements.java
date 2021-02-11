package com.sixthday.navigation.api.elasticsearch.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductRefinements implements Serializable {
    private static final long serialVersionUID = 5198455284465198316L;

    private boolean adornOnly;
    private boolean adornAndSaleOnly;
    private boolean regOnly;
    private boolean saleOnly;

    private PriceRangeAtg priceRange;
}
