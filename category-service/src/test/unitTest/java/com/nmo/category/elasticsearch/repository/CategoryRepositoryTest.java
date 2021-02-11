package com.sixthday.category.elasticsearch.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CategoryRepositoryTest {
    @InjectMocks
    private CategoryRepository categoryRepository;
    @Mock
    private ESCategoryDocumentRepository esCategoryDocumentRepository;

    @Test
    public void testGetCategoryDocumentCallsES() {
        categoryRepository.getCategoryDocument("catId");
        verify(esCategoryDocumentRepository).getCategoryDocument(anyString());
    }

    @Test
    public void testGetCategoryDocumentsCallsES() {
        categoryRepository.getCategoryDocuments(Collections.emptySet());
        verify(esCategoryDocumentRepository).getCategoryDocuments((anySetOf(String.class)));
    }
}