package com.sixthday.navigation.api.models;

import com.sixthday.navigation.api.elasticsearch.models.SearchCriteria;
import com.sixthday.navigation.api.elasticsearch.models.SearchCriteriaOptions;

import java.util.*;

public class SearchCriteriaBuilder {
    private SearchCriteriaOptions include = new SearchCriteriaOptionsBuilder().build();
    private SearchCriteriaOptions exclude = new SearchCriteriaOptionsBuilder().build();

    public SearchCriteriaBuilder() {
        Map<String, String> includeCriteria = new HashMap<>();
        includeCriteria.put("level1", "Woman's apparel");
        includeCriteria.put("level2", "Dresses");
        include = new SearchCriteriaOptionsBuilder()
                .withCategoryInHierarchy(includeCriteria)
                .withPromotions(Arrays.asList("promo1", "promo2"))
                .build();

        Map<String, String> excludeCriteria = new HashMap<>();
        excludeCriteria.put("level1", "Woman's apparel");
        excludeCriteria.put("level2", "Dresses");
        excludeCriteria.put("level3", "Tops");
        exclude = new SearchCriteriaOptionsBuilder()
                .withCategoryInHierarchy(excludeCriteria)
                .withPromotions(Arrays.asList("promo1", "promo2"))
                .build();
    }

    public SearchCriteria build() {
        return new SearchCriteria(include, exclude);
    }
}
