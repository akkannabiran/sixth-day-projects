package com.sixthday.navigation.api.elasticsearch.models;

import com.sixthday.navigation.api.models.SearchCriteriaOptionsBuilder;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SearchCriteriaOptionsTest {

    @Test
    public void shouldIsEmptyReturnTrueIfBothHierarchyAndAttributesAreEmpty() {
        SearchCriteriaOptions emptySearchCriteriaOptions = new SearchCriteriaOptions();
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

        SearchCriteriaOptions validSearchCriteriaOptions = new SearchCriteriaOptionsBuilder()
                .withHierarchy(hierarchy)
                .withAttributes(attributes)
                .build();
        assertFalse(validSearchCriteriaOptions.hasEmptyHierarchyAndAttributes());
    }

    @Test
    public void shouldIsEmptyReturnFalseIfAttributesAreNotEmptyAndHierarchyIsEmpty() {
        List<Map<String, List<String>>> attributes = new ArrayList<>();
        Map<String, List<String>> designerAttribute = new HashMap<>();
        designerAttribute.put("Designer", Arrays.asList("Designer One"));
        attributes.add(designerAttribute);

        SearchCriteriaOptions validSearchCriteriaOptions = new SearchCriteriaOptionsBuilder()
                .withAttributes(attributes)
                .build();
        assertFalse(validSearchCriteriaOptions.hasEmptyHierarchyAndAttributes());
    }

    @Test
    public void shouldIsEmptyReturnFalseIfHierarchyIsNotEmptyAndAttributesAreEmpty() {
        Map<String, String> levelOneHierarchy = new HashMap<>();
        levelOneHierarchy.put("level1", "Dresses");
        ArrayList<Map<String, String>> hierarchy = new ArrayList<>();
        hierarchy.add(levelOneHierarchy);

        SearchCriteriaOptions validSearchCriteriaOptions = new SearchCriteriaOptionsBuilder()
                .withHierarchy(hierarchy)
                .build();
        assertFalse(validSearchCriteriaOptions.hasEmptyHierarchyAndAttributes());
    }
}