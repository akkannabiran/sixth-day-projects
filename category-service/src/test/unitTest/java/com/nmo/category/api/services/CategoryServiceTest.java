package com.sixthday.category.api.services;

import com.sixthday.category.api.mappers.CategoryMapper;
import com.sixthday.category.api.models.Category;
import com.sixthday.category.api.models.CategoryDocumentInfo;
import com.sixthday.category.config.CategoryServiceConfig;
import com.sixthday.category.elasticsearch.repository.CategoryRepository;
import com.sixthday.category.exceptions.CategoryDocumentNotFoundException;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.MDC;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private CategoryServiceConfig categoryServiceConfig;

    @Before
    public void init() {
        when(categoryServiceConfig.getThresholdLimitValue()).thenReturn(120);
    }

    @Test
    public void shouldFetchAllCategoriesFromRepositoryWhenItIsAvailable() {
        CategoryDocument cat1CategoryDocument = CategoryDocument.builder().id("cat1").seoTags("SampleSEO Tags").seoTitleOverride("some Title").build();
        CategoryDocument cat2CategoryDocument = CategoryDocument.builder().id("cat2").seoTags("SampleSEO Tags").seoTitleOverride("some Title").build();
        CategoryDocument cat3CategoryDocument = CategoryDocument.builder().id("cat3").seoTags("SampleSEO Tags").seoTitleOverride("some Title").build();
        List<CategoryDocument> categoryDocuments = Arrays.asList(cat1CategoryDocument, cat2CategoryDocument, cat3CategoryDocument);
        CategoryDocumentInfo category1DocumentInfo = CategoryDocumentInfo.builder().parentCategoryId("parentCat1").categoryDocument(cat1CategoryDocument).build();
        CategoryDocumentInfo category2DocumentInfo = CategoryDocumentInfo.builder().parentCategoryId("parentCat1").categoryDocument(cat2CategoryDocument).build();
        CategoryDocumentInfo category3DocumentInfo = CategoryDocumentInfo.builder().parentCategoryId("parentCat1").categoryDocument(cat2CategoryDocument).build();

        Map<String, String> categoryIds = new HashMap<>();
        categoryIds.put("cat1", "parentCat1");
        categoryIds.put("cat2", "parentCat2");
        categoryIds.put("cat3", "parentCat3");

        when(categoryRepository.getCategoryDocuments(anySetOf(String.class))).thenReturn(categoryDocuments);
        when(categoryMapper.map(category1DocumentInfo,Optional.empty())).thenReturn(new Category());
        when(categoryMapper.map(category2DocumentInfo,Optional.empty())).thenReturn(new Category());
        when(categoryMapper.map(category3DocumentInfo,Optional.empty())).thenReturn(new Category());

        final List<Category> categories = categoryService.getCategories(categoryIds, Optional.empty(), Optional.empty());

        verify(categoryRepository, times(1)).getCategoryDocuments(anySetOf(String.class));
        verify(categoryMapper, times(3)).map(anyObject(),any());
        assertThat(categories.size(), equalTo(3));
    }

    @Test
    public void shouldFetchOnlyAvailableCategoriesFromRepository() {
        CategoryDocument cat1CategoryDocument = CategoryDocument.builder().id("cat1").seoTags("SampleSEO Tags").seoTitleOverride("some Title").build();
        CategoryDocument cat2CategoryDocument = CategoryDocument.builder().build();
        List<CategoryDocument> categoryDocuments = Collections.singletonList(cat1CategoryDocument);
        CategoryDocumentInfo category1DocumentInfo = CategoryDocumentInfo.builder().parentCategoryId("parentCat1").categoryDocument(cat1CategoryDocument).build();
        CategoryDocumentInfo category2DocumentInfo = CategoryDocumentInfo.builder().parentCategoryId("parentCat1").categoryDocument(cat2CategoryDocument).build();
        CategoryDocumentInfo category3DocumentInfo = CategoryDocumentInfo.builder().parentCategoryId("parentCat1").categoryDocument(cat2CategoryDocument).build();

        Map<String, String> categoryIds = new HashMap<>();
        categoryIds.put("cat1", "parentCat1");
        categoryIds.put("cat2", "parentCat2");
        categoryIds.put("cat3", "parentCat3");

        when(categoryRepository.getCategoryDocuments(anySetOf(String.class))).thenReturn(categoryDocuments);
        when(categoryMapper.map(category1DocumentInfo,Optional.empty())).thenReturn(new Category());
        when(categoryMapper.map(category2DocumentInfo,Optional.empty())).thenThrow(CategoryDocumentNotFoundException.class);
        when(categoryMapper.map(category3DocumentInfo,Optional.empty())).thenThrow(CategoryDocumentNotFoundException.class);

        final List<Category> categories = categoryService.getCategories(categoryIds, Optional.empty(), Optional.empty());
        verify(categoryRepository, times(1)).getCategoryDocuments(anySetOf(String.class));
        verify(categoryMapper, times(1)).map(anyObject(),any());
        assertThat(categories.size(), equalTo(1));
    }

    @Test(expected = CategoryDocumentNotFoundException.class)
    public void shouldThrowCategoryDocumentNotFoundExceptionWhenNoneOfTheCategoryDocumentsFound() {
        CategoryDocument cat1CategoryDocument = CategoryDocument.builder().id("cat123").seoTags("SampleSEO Tags").seoTitleOverride("some Title").build();
        CategoryDocumentInfo category1DocumentInfo = CategoryDocumentInfo.builder().parentCategoryId("parentCategoryId").categoryDocument(cat1CategoryDocument).build();

        Map<String, String> categoryIds = new HashMap<>();
        categoryIds.put("cat1", "parentCat1");

        when(categoryRepository.getCategoryDocuments(anySetOf(String.class))).thenThrow(CategoryDocumentNotFoundException.class);

        categoryService.getCategories(categoryIds, Optional.empty(), Optional.empty());
        verify(categoryRepository, times(0)).getCategoryDocuments(categoryIds.keySet());
        verify(categoryMapper, times(0)).map(category1DocumentInfo, Optional.empty());
    }

    @Test
    public void shouldReturnEmptyCategoryWhenCategoryIdsAreNull() {
        final List<Category> nullCategories = categoryService.getCategories(null, Optional.empty(), Optional.empty());
        verify(categoryRepository, times(0)).getCategoryDocuments(anySetOf(String.class));
        verify(categoryMapper, times(0)).map(anyObject(), any());
        assertThat(nullCategories.size(), equalTo(0));
    }

    @Test
    public void shouldReturnEmptyCategoryWhenCategoryIdsAreEmpty() {
        final List<Category> nullCategories = categoryService.getCategories(new HashMap<>(), Optional.empty(), Optional.empty());
        verify(categoryRepository, times(0)).getCategoryDocuments(anySetOf(String.class));
        verify(categoryMapper, times(0)).map(any(CategoryDocumentInfo.class), any());
        assertThat(nullCategories.size(), equalTo(0));

    }

    @Test
    public void shouldCSCallESOnlyOnceAsParentIdPassedInMap() {
        Map<String, String> inputMap = Collections.singletonMap("cat1", "parentCat1");
        List<CategoryDocument> categoryDocuments = Arrays.asList(CategoryDocument.builder().id("cat1").build(), CategoryDocument.builder().id("parentCat1").build());

        when(categoryRepository.getCategoryDocuments(anySetOf(String.class))).thenReturn(categoryDocuments);

        assertThat(categoryService.getCategories(inputMap, Optional.empty(), Optional.empty()).size(), is(1));
        verify(categoryRepository, times(1)).getCategoryDocuments(anySetOf(String.class));
        verify(categoryMapper, times(1)).map(any(CategoryDocumentInfo.class), any());
    }

    @Test
    public void shouldCSCallESOnlyOnceWhenCategoryIdIsNull() {
        Map<String, String> inputMap = Collections.singletonMap(null, "parentCat1");
        List<CategoryDocument> categoryDocuments = Arrays.asList(CategoryDocument.builder().id("cat1").build(), CategoryDocument.builder().id("parentCat1").build());

        when(categoryRepository.getCategoryDocuments(anySetOf(String.class))).thenReturn(categoryDocuments);

        assertThat(categoryService.getCategories(inputMap, Optional.empty(), Optional.empty()).size(), is(0));
        verify(categoryRepository, times(1)).getCategoryDocuments(anySetOf(String.class));
        verify(categoryMapper, times(0)).map(any(CategoryDocumentInfo.class), any());
    }

    @Test
    public void shouldCSCallESTwiceAsParentCategoryIdNotPassAndSeoTitleOverrideAndSeoTagsNotInCategory() {
        Map<String, String> inputMap = Collections.singletonMap("cat1", "");
        List<CategoryDocument> categoryDocuments = new ArrayList<>(Arrays.asList(CategoryDocument.builder().id("cat1").defaultPath("parentCat1_cat1").build()));

        when(categoryRepository.getCategoryDocuments(anySetOf(String.class))).thenReturn(categoryDocuments);

        assertThat(categoryService.getCategories(inputMap, Optional.empty(), Optional.empty()).size(), is(1));
        verify(categoryMapper, times(1)).map(any(CategoryDocumentInfo.class), any());
        verify(categoryRepository, times(2)).getCategoryDocuments(anySetOf(String.class));
    }

    @Test
    public void shouldCSCallESOnceParentCategoryIdNotPassAndSeoTitleOverrideAndSeoTagsInCategory() {
        Map<String, String> inputMap = Collections.singletonMap(null, null);
        List<CategoryDocument> categoryDocuments = Arrays.asList(CategoryDocument.builder().id("cat1").seoTitleOverride("seoTitleOverride").seoTags("seoTags").defaultPath("parentCat1_cat1").build(), CategoryDocument.builder().id("parentCat1").build());

        when(categoryRepository.getCategoryDocuments(anySetOf(String.class))).thenReturn(categoryDocuments);

        assertThat(categoryService.getCategories(inputMap, Optional.empty(), Optional.empty()).size(), is(0));
        verify(categoryMapper, times(0)).map(any(CategoryDocumentInfo.class), any());
        verify(categoryRepository, times(0)).getCategoryDocuments(anySetOf(String.class));
    }

    @Test
    public void shouldCSCallESOnlyOnceAsParentIdPassedInMapAndMapperCallsTwice() {
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("cat1", "parentCat1");
        inputMap.put("cat2", "parentCat2");
        List<CategoryDocument> categoryDocuments = Arrays.asList(CategoryDocument.builder().id("cat1").seoTitleOverride("seoTitleOverride").seoTags("seoTags").defaultPath("parentCat1_cat1").build(), CategoryDocument.builder().id("parentCat1").build(),
                CategoryDocument.builder().id("cat2").seoTitleOverride("seoTitleOverride").seoTags("seoTags").defaultPath("parentCat2_cat2").build(), CategoryDocument.builder().id("parentCat2").build());

        when(categoryRepository.getCategoryDocuments(anySetOf(String.class))).thenReturn(categoryDocuments);

        assertThat(categoryService.getCategories(inputMap,Optional.empty(), Optional.empty()).size(), is(2));
        verify(categoryRepository, times(1)).getCategoryDocuments(anySetOf(String.class));
        verify(categoryMapper, times(2)).map(any(CategoryDocumentInfo.class), any());
    }

    @Test
    public void testMDCHoldsCategoryIdsAsNullWhenDebugModeIsDisabled() {
        Map<String, String> categoryIds = new HashMap<>();
        categoryIds.put("cat123", "parentCat123");
        categoryIds.put("cat1234", "parentCat1234");

        categoryService.getCategories(categoryIds, Optional.empty(), Optional.empty());
        assertNull(MDC.get("CategoryIds"));
    }
}
