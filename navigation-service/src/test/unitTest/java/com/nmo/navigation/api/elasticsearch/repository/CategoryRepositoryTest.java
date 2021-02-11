package com.sixthday.navigation.api.elasticsearch.repository;

import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CategoryRepositoryTest {

    @InjectMocks
    private CategoryRepository categoryRepository;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private NavigationServiceConfig navigationServiceConfig;
    @Mock
    private ESCategoryDocumentRepository esCategoryDocumentRepository;

    @Before
    public void init() {
        when(navigationServiceConfig.getCategoryConfig().getIdConfig().getSeoFooter()).thenReturn("seoCategory");
    }

    @Test
    public void testGetBrandLinksReturnsEmptyMapWhenSeoCategoryIsNotAvailable() {
        Map<CategoryDocument, List<CategoryDocument>> brandLinks = categoryRepository.getBrandLinks();
        assertThat(brandLinks.size(), is(0));
    }

    @Test
    public void testGetBrandLinksReturnsEmptyMapWhenSeoCategoryChildrenIsEmpty() {
        when(esCategoryDocumentRepository.getCategoryDocument(anyString())).thenReturn(CategoryDocument.builder().build());
        Map<CategoryDocument, List<CategoryDocument>> brandLinks = categoryRepository.getBrandLinks();
        assertThat(brandLinks.size(), is(0));
    }

    @Test
    public void testGetBrandLinksReturnsNoneEmptyMap() {
        when(esCategoryDocumentRepository.getCategoryDocument(eq("seoCategory"))).thenReturn(CategoryDocument.builder().children(Arrays.asList("subCat1", "subCat2")).build());
        when(esCategoryDocumentRepository.getCategoryDocument(eq("subCat1"))).thenReturn(CategoryDocument.builder().children(Arrays.asList("subCat3")).build());
        when(esCategoryDocumentRepository.getCategoryDocument(eq("subCat2"))).thenReturn(null);
        when(esCategoryDocumentRepository.getCategoryDocuments(anyListOf(String.class))).thenReturn(new ArrayList<>());
        Map<CategoryDocument, List<CategoryDocument>> brandLinks = categoryRepository.getBrandLinks();
        assertThat(brandLinks.size(), is(1));
    }
}