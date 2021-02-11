package com.sixthday.navigation.api.mappers;

import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.elasticsearch.models.PriceRangeAtg;
import com.sixthday.navigation.api.elasticsearch.models.SearchCriteria;
import com.sixthday.navigation.api.models.response.PriceRange;
import com.sixthday.navigation.api.models.response.SearchCriteriaResponse;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

import static com.sixthday.navigation.api.models.response.PriceRange.PriceRangeType.OFF;

public class SearchCriteriaResponseMapper {
    public SearchCriteriaResponse map(@NotNull CategoryDocument categoryDocument) {
        SearchCriteriaResponse searchCriteriaResponse = new SearchCriteriaResponse();
        SearchCriteria searchCriteria = categoryDocument.getSearchCriteria();
        if (searchCriteria != null) {
            searchCriteriaResponse.setExclude(searchCriteria.getExclude());
            searchCriteriaResponse.setInclude(searchCriteria.getInclude());
        }
        searchCriteriaResponse.setIncludeUnsellable(categoryDocument.isShowAllProducts());
        searchCriteriaResponse.setCatalogIds(categoryDocument.getCmosCatalogCodes());
        searchCriteriaResponse.setProductClassType(getProductClassType(categoryDocument));
        searchCriteriaResponse.setPriceRange(getPriceRange(categoryDocument));
        return searchCriteriaResponse;
    }

    private PriceRange getPriceRange(CategoryDocument categoryDocument) {
        PriceRangeAtg promoPrice = categoryDocument.getProductRefinements().getPriceRange();
        if (promoPrice == null) {
            return new PriceRange(BigDecimal.ZERO, BigDecimal.ZERO, OFF);
        }
        PriceRange priceRange = new PriceRange();
        priceRange.setOption(PriceRange.PriceRangeType.valueOf(promoPrice.getOption()));
        priceRange.setMax(promoPrice.getMax());
        priceRange.setMin(promoPrice.getMin());
        return priceRange;
    }

    private String getProductClassType(CategoryDocument categoryDocument) {
        if (categoryDocument.getProductRefinements().isSaleOnly())
            return "INCLUDE_SALE_ONLY";
        if (categoryDocument.getProductRefinements().isAdornOnly())
            return "INCLUDE_ADORNED_ONLY";
        if (categoryDocument.getProductRefinements().isAdornAndSaleOnly())
            return "SALE_ONLY_AND_ADORNED_ONLY";
        if (categoryDocument.getProductRefinements().isRegOnly())
            return "REGULAR_PRICE_ONLY";
        return "INCLUDE_ALL";
    }
}