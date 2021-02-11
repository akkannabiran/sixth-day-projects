package com.sixthday.category.elasticsearch.documents.models;

import com.sixthday.navigation.api.elasticsearch.models.SearchCriteriaOptions;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SearchCriteriaOptionsTest {

    @Test
    public void shouldIsEmptyReturnTrueIfBothHierarchyAndAttributesAreEmpty() {
        SearchCriteriaOptions emptySearchCriteriaOptions = SearchCriteriaOptions.builder()
                .hierarchy(Collections.emptyList()).attributes(Collections.emptyList()).promotions(Collections.emptyList()).build();
        assertTrue(emptySearchCriteriaOptions.hasEmptyHierarchyAndAttributes());
    }

    @Test
    public void shouldIsEmptyReturnFalseIfBothHierarchyAndAttributesAreNotEmpty() {
        Map<String, String> levelOneHierarchy = new HashMap<>();
        levelOneHierarchy.put("level1", "Dresses");
        ArrayList<Map<String, String>> hierarchy = new ArrayList<>();
        hierarchy.add(levelOneHierarchy);

        List<Map<String, List<String>>> attributes = new ArrayList<>();
        Map<String, List<String>> designerAttribute = new HashMap<>();
        designerAttribute.put("Designer", Arrays.asList("Designer One"));
        attributes.add(designerAttribute);

        SearchCriteriaOptions validSearchCriteriaOptions = SearchCriteriaOptions.builder()
                .hierarchy(hierarchy)
                .attributes(attributes)
                .build();
        assertFalse(validSearchCriteriaOptions.hasEmptyHierarchyAndAttributes());
    }

    @Test
    public void shouldIsEmptyReturnFalseIfAttributesAreNotEmptyAndHierarchyIsEmpty() {
        List<Map<String, List<String>>> attributes = new ArrayList<>();
        Map<String, List<String>> designerAttribute = new HashMap<>();
        designerAttribute.put("Designer", Arrays.asList("Designer One"));
        attributes.add(designerAttribute);

        SearchCriteriaOptions validSearchCriteriaOptions = SearchCriteriaOptions.builder()
                .attributes(attributes)
                .build();
        assertFalse(validSearchCriteriaOptions.hasEmptyHierarchyAndAttributes());
    }

    @Test
    public void shouldIsEmptyReturnFalseIfHierarchyIsNotEmptyAndAttributesAreEmpty() {
        Map<String, String> levelOneHierarchy = new HashMap<>();
        levelOneHierarchy.put("level1", "Dresses");
        ArrayList<Map<String, String>> hierarchy = new ArrayList<>();
        hierarchy.add(levelOneHierarchy);

        SearchCriteriaOptions validSearchCriteriaOptions = SearchCriteriaOptions.builder()
                .hierarchy(hierarchy)
                .build();
        assertFalse(validSearchCriteriaOptions.hasEmptyHierarchyAndAttributes());
    }
}