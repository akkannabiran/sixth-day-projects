package com.sixthday.navigation.api.elasticsearch.documents;

import com.sixthday.navigation.api.elasticsearch.models.SearchCriteria;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CategoryDocumentTest {

    @Test
    public void shouldHasEmptySearchCriteriaReturnTrueIfSearchCriteriaIsEmpty() {
        CategoryDocument categoryDocumentWithEmptySearchCriteria = new CategoryDocument();
        categoryDocumentWithEmptySearchCriteria.setSearchCriteria(new SearchCriteria());
        assertTrue(categoryDocumentWithEmptySearchCriteria.hasEmptySearchCriteria());
    }

    @Test
    public void shouldHasEmptySearchCriteriaReturnTrueIfSearchCriteriaIsNull() {
        CategoryDocument categoryDocumentWithEmptySearchCriteria = new CategoryDocument();
        assertTrue(categoryDocumentWithEmptySearchCriteria.hasEmptySearchCriteria());
    }
}