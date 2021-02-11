package com.sixthday.category.api.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sixthday.navigation.api.elasticsearch.models.SearchCriteriaOptions;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchCriteriaResponse {
    private boolean includeUnsellable;

    private String productClassType;

    private List<String> catalogIds;

    private SearchCriteriaOptions include;
    private SearchCriteriaOptions exclude;

    private PriceRange priceRange;
}
