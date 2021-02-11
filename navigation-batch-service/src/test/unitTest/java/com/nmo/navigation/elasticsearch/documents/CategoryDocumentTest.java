package com.sixthday.navigation.elasticsearch.documents;

import com.sixthday.navigation.CategoryDocumentBuilder;
import com.sixthday.navigation.batch.processor.LeftNavTreeProcessor;
import com.sixthday.navigation.domain.ContextualProperty;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CategoryDocumentTest {
    private List<String> emptyChildCategoryOrder = new ArrayList<>();
    private String emptyDriveToSubcategoryId = "";

    @Mock
    private LeftNavTreeProcessor leftNavTreeProcessor;

    @Test
    public void shouldReturnEmptyOptionalWhenCategoryHasNoApplicableContextualProperties() {
        ContextualProperty firstContextualProperty = new ContextualProperty(false, false, "10", "Alternate Name", "", emptyDriveToSubcategoryId, null, null, null, emptyChildCategoryOrder);

        CategoryDocument categoryDocument = new CategoryDocumentBuilder().withId("101")
                .withName("Original Name")
                .withContextualProperties(firstContextualProperty)
                .build();
        ContextualProperty contextualProperty = categoryDocument.getApplicablePropertiesForCategory("101");
        assertEquals("Original Name", categoryDocument.getCategoryName(contextualProperty));
    }

    @Test
    public void shouldGetAlternateCategoryNameIfExists() {
        ContextualProperty firstContextualProperty = new ContextualProperty(false, false, "10", "Alternate Name", "", emptyDriveToSubcategoryId, null, null, null, emptyChildCategoryOrder);

        CategoryDocument categoryDocument = new CategoryDocumentBuilder().withId("101")
                .withContextualProperties(firstContextualProperty)
                .build();
        ContextualProperty contextualProperty = categoryDocument.getApplicablePropertiesForCategory("10");
        assertEquals("Alternate Name", categoryDocument.getCategoryName(contextualProperty));
    }

    @Test
    public void shouldGetDriveToSubcategoryIdForGrandChildIfExists() {
        when(leftNavTreeProcessor.getCategoryDocument(anyString())).thenReturn(Optional.of(CategoryDocument.builder().build()));
        ContextualProperty firstContextualProperty = new ContextualProperty(false, false, "10", "", "", "cat123:cat456", null, null, null, emptyChildCategoryOrder);

        CategoryDocument categoryDocument101 = new CategoryDocumentBuilder().withId("101")
                .withContextualProperties(firstContextualProperty)
                .withChildren("cat123")
                .build();

        CategoryDocument categoryDocument123 = new CategoryDocumentBuilder().withId("cat123")
                .withChildren("cat456")
                .build();

        CategoryDocument categoryDocument456 = new CategoryDocumentBuilder().withId("cat123")
                .build();

        when(leftNavTreeProcessor.getCategoryDocument(eq("cat123"))).thenReturn(Optional.of(categoryDocument123));

        when(leftNavTreeProcessor.getCategoryDocument(eq("cat456"))).thenReturn(Optional.of(categoryDocument456));

        ContextualProperty contextualProperty = categoryDocument101.getApplicablePropertiesForCategory("10");
        assertEquals("cat123_cat456", categoryDocument101.getDriveToSubCategoryId(contextualProperty, leftNavTreeProcessor));
    }

    @Test
    public void shouldNotGetDriveToSubcategoryIdForGrandChildIfDoesNotExists() {
        when(leftNavTreeProcessor.getCategoryDocument(anyString())).thenReturn(Optional.empty());
        ContextualProperty firstContextualProperty = new ContextualProperty(false, false, "10", "", "", "cat123:cat456", null, null, null, emptyChildCategoryOrder);

        CategoryDocument categoryDocument = new CategoryDocumentBuilder().withId("101")
                .withContextualProperties(firstContextualProperty)
                .build();
        ContextualProperty contextualProperty = categoryDocument.getApplicablePropertiesForCategory("10");
        assertEquals("", categoryDocument.getDriveToSubCategoryId(contextualProperty, leftNavTreeProcessor));
    }
}