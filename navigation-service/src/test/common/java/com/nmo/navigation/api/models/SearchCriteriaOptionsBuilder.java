package com.sixthday.navigation.api.models;

import com.sixthday.navigation.api.elasticsearch.models.SearchCriteriaOptions;

import java.util.*;

public class SearchCriteriaOptionsBuilder {

    private List<String> promotions = new ArrayList<>();
    private List<Map<String, String>> hierarchy = new ArrayList<>();
    private List<Map<String, List<String>>> attributes = new ArrayList<>();

    public SearchCriteriaOptions build() {
        return new SearchCriteriaOptions(promotions, hierarchy, attributes);
    }

    public SearchCriteriaOptionsBuilder withCategoryInHierarchy(Map<String, String> category) {
        this.hierarchy.add(category);
        return this;
    }

    public SearchCriteriaOptionsBuilder withHierarchy(List<Map<String, String>> hierarchy) {
        this.hierarchy = hierarchy;
        return this;
    }

    public SearchCriteriaOptionsBuilder withPromotions(List<String> promotions) {
        this.promotions = promotions;
        return this;
    }

    public SearchCriteriaOptionsBuilder withAttributes(List<Map<String, List<String>>> attributes) {
        this.attributes = attributes;
        return this;
    }
}
