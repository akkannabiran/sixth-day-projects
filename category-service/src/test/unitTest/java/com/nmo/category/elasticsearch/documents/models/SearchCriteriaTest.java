package com.sixthday.category.elasticsearch.documents.models;

import com.sixthday.navigation.api.elasticsearch.models.SearchCriteria;
import com.sixthday.navigation.api.elasticsearch.models.SearchCriteriaOptions;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SearchCriteriaTest {

    @Test
    public void shouldIsEmptyReturnTrueIfBothIncludeAndExcludeAreEmpty() {
        SearchCriteria emptySearchCriteria = SearchCriteria.builder()
                .include(SearchCriteriaOptions.builder()
                        .hierarchy(Collections.emptyList()).attributes(Collections.emptyList()).promotions(Collections.emptyList()).build())
                .exclude(SearchCriteriaOptions.builder()
                        .hierarchy(Collections.emptyList()).attributes(Collections.emptyList()).promotions(Collections.emptyList()).build())
                .build();

        assertTrue(emptySearchCriteria.hasEmptyIncludeAndExcludeOptions());
    }

    @Test
    public void shouldIsEmptyReturnTrueIfBothIncludeAndExcludeAreNull() {
        SearchCriteria invalidSearchCriteria = new SearchCriteria();
        assertTrue(invalidSearchCriteria.hasEmptyIncludeAndExcludeOptions());
    }

    @Test
    public void shouldIsEmptyReturnTrueIfBothIncludeAndExcludeAreNullOrEmpty() {
        SearchCriteria invalidSearchCriteria = new SearchCriteria();
        invalidSearchCriteria.setExclude(null);
        assertTrue(invalidSearchCriteria.hasEmptyIncludeAndExcludeOptions());
    }

    @Test
    public void shouldIsEmptyReturnFalseIfBothIncludeAndExcludeAreNotEmpty() {
        Map<String, String> hierarchyMap = new HashMap<>();
        hierarchyMap.put("level1","Accessories");
        SearchCriteria validSearchCriteria = SearchCriteria.builder()
                .include(SearchCriteriaOptions.builder()
                        .hierarchy(Arrays.asList(hierarchyMap)).attributes(Collections.emptyList()).promotions(Collections.emptyList()).build())
                .exclude(SearchCriteriaOptions.builder()
                        .hierarchy(Collections.emptyList()).attributes(Collections.emptyList()).promotions(Collections.emptyList()).build())
                .build();
        assertFalse(validSearchCriteria.hasEmptyIncludeAndExcludeOptions());
    }

    @Test
    public void shouldIsEmptyReturnFalseIfIncludeIsEmptyAndExcludeIsNotEmpty() {

        Map<String, String> levelOneHierarchy = new HashMap<>();
        levelOneHierarchy.put("level1", "Dresses");
        ArrayList<Map<String, String>> hierarchy = new ArrayList<>();
        hierarchy.add(levelOneHierarchy);

        SearchCriteriaOptions validExcludeOptions = SearchCriteriaOptions.builder().hierarchy(hierarchy).build();
        SearchCriteria validSearchCriteria = new SearchCriteria();
        validSearchCriteria.setExclude(validExcludeOptions);

        assertFalse(validSearchCriteria.hasEmptyIncludeAndExcludeOptions());
    }

    @Test
    public void shouldIsEmptyReturnFalseIfExcludeIsEmptyAndIncludeIsNotEmpty() {

        Map<String, String> levelOneHierarchy = new HashMap<>();
        levelOneHierarchy.put("level1", "Dresses");
        ArrayList<Map<String, String>> hierarchy = new ArrayList<>();
        hierarchy.add(levelOneHierarchy);

        SearchCriteriaOptions validIncludeOptions = SearchCriteriaOptions.builder().hierarchy(hierarchy).build();
        SearchCriteria validSearchCriteria = new SearchCriteria();
        validSearchCriteria.setInclude(validIncludeOptions);

        assertFalse(validSearchCriteria.hasEmptyIncludeAndExcludeOptions());
    }
}