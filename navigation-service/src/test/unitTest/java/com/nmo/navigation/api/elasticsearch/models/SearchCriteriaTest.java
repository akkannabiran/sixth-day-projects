package com.sixthday.navigation.api.elasticsearch.models;

import com.sixthday.navigation.api.models.SearchCriteriaBuilder;
import com.sixthday.navigation.api.models.SearchCriteriaOptionsBuilder;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SearchCriteriaTest {

    @Test
    public void shouldIsEmptyReturnTrueIfBothIncludeAndExcludeAreEmpty() {
        SearchCriteria emptySearchCriteria = new SearchCriteria();
        emptySearchCriteria.setInclude(new SearchCriteriaOptions());
        emptySearchCriteria.setExclude(new SearchCriteriaOptions());
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
        SearchCriteria validSearchCriteria = new SearchCriteriaBuilder().build();
        assertFalse(validSearchCriteria.hasEmptyIncludeAndExcludeOptions());
    }

    @Test
    public void shouldIsEmptyReturnFalseIfIncludeIsEmptyAndExcludeIsNotEmpty() {

        Map<String, String> levelOneHierarchy = new HashMap<>();
        levelOneHierarchy.put("level1", "Dresses");
        ArrayList<Map<String, String>> hierarchy = new ArrayList<>();
        hierarchy.add(levelOneHierarchy);

        SearchCriteriaOptions validExcludeOptions = new SearchCriteriaOptionsBuilder().withHierarchy(hierarchy).build();
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

        SearchCriteriaOptions validIncludeOptions = new SearchCriteriaOptionsBuilder().withHierarchy(hierarchy).build();
        SearchCriteria validSearchCriteria = new SearchCriteria();
        validSearchCriteria.setInclude(validIncludeOptions);

        assertFalse(validSearchCriteria.hasEmptyIncludeAndExcludeOptions());
    }
}