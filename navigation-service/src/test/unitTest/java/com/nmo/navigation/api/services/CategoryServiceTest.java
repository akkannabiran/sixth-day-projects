package com.sixthday.navigation.api.services;

import com.sixthday.navigation.api.controllers.elasticsearch.documents.CategoryDocumentBuilder;
import com.sixthday.navigation.api.elasticsearch.repository.CategoryRepository;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class CategoryServiceTest {
    public static final String CATEGORY_ID = "cat0001";
    public static final String BAD_CATEGORY_ID = "badCatId";
    private CategoryRepository mockCategoryRepository;
    private CategoryService categoryService;

    @Before
    public void setup() {
        mockCategoryRepository = mock(CategoryRepository.class);
        categoryService = new CategoryService(mockCategoryRepository);
    }

    @Test
    public void shouldFetchCategoryFromRepository() {
        when(mockCategoryRepository.getCategoryDocument(CATEGORY_ID)).thenReturn(new CategoryDocumentBuilder().withId(CATEGORY_ID).build());
        categoryService.getCategoryDocument(CATEGORY_ID);
        verify(mockCategoryRepository).getCategoryDocument(CATEGORY_ID);
    }
}