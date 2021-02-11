package com.sixthday.category.api.utils;

import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class CategoryServiceUtilTest {

    @Test
    @SneakyThrows
    public void testConstructorIsPrivate() {
        Constructor<CategoryServiceUtil> constructor = CategoryServiceUtil.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void shouldReturnFalseToMakeSecondCallToESNoSeoTags() {
        CategoryDocument categoryDocument = CategoryDocument.builder().id("cat1").seoTitleOverride("seoTitleOverride").build();
        assertFalse(CategoryServiceUtil.useCategoryDocumentToBuildTheResponse(categoryDocument, ""));
    }

    @Test
    public void shouldReturnFalseToMakeSecondCallToESNoSeoTitleOverride() {
        CategoryDocument categoryDocument = CategoryDocument.builder().id("cat1").seoTags("seoTags").build();
        assertFalse(CategoryServiceUtil.useCategoryDocumentToBuildTheResponse(categoryDocument, ""));
    }

    @Test
    public void shouldReturnFalseToMakeSecondCallToESNoSeoTagsAndNoSeoTitleOverride() {
        CategoryDocument categoryDocument = CategoryDocument.builder().id("cat1").build();
        assertTrue(CategoryServiceUtil.useCategoryDocumentToBuildTheResponse(categoryDocument, "parentCat1"));
    }

    @Test
    public void ShouldReturnTrueNoSecondCallToES() {
        CategoryDocument categoryDocument = CategoryDocument.builder().id("cat1").seoTags("seoTags").seoTitleOverride("seoTitleOverride").build();
        assertTrue(CategoryServiceUtil.useCategoryDocumentToBuildTheResponse(categoryDocument, ""));
    }

    @Test
    public void shouldReturnFalseToMakeSecondCallToESNoSeoTagsAndNoSeoTitleOverrideNoParentCatIdInIput() {
        CategoryDocument categoryDocument = CategoryDocument.builder().id("cat1").build();
        assertFalse(CategoryServiceUtil.useCategoryDocumentToBuildTheResponse(categoryDocument, ""));
    }

    @Test
    public void shouldGetParentCatIdFromInputMap() {
        CategoryDocument categoryDocument = CategoryDocument.builder().id("cat1").defaultPath("cat0_cat1").build();

        for (Map.Entry entry : Collections.singletonMap("cat", "parentCat1").entrySet()) {
            assertThat(CategoryServiceUtil.getParentCategoryId(entry, categoryDocument), is("parentCat1"));
        }
    }

    @Test
    public void shouldGetParentCatIdFromDefaultMap() {
        CategoryDocument categoryDocument = CategoryDocument.builder().id("cat1").defaultPath("cat0_cat1").build();

        for (Map.Entry entry : Collections.singletonMap("cat", "").entrySet()) {
            assertThat(CategoryServiceUtil.getParentCategoryId(entry, categoryDocument), is("cat0"));
        }
    }

    @Test
    public void shouldReturnEmptyCategoryWhenRequestHasMoreThanThresholdLimitValueCategories() {
        Map<String, String> categoryIds = new HashMap<>();
        for (int i = 0; i < 121; i++) {
            categoryIds.put("cat" + i, "parentCat" + i);
        }

        assertFalse(CategoryServiceUtil.isValidRequest(categoryIds, 120));
    }

    @Test
    public void shouldReturnEmptyCategoryWhenRequestHasEmptyInput() {
        Map<String, String> categoryIds = new HashMap<>();

        assertFalse(CategoryServiceUtil.isValidRequest(categoryIds, 120));
    }

    @Test
    public void shouldValidInputToCategoryService() {
        Map<String, String> categoryIds = Collections.singletonMap("cat", "parentCat1");

        assertTrue(CategoryServiceUtil.isValidRequest(categoryIds, 120));
    }

    @Test
    public void shouldCategoryIdsSizeEqalToMapKeyAndValueSize() {
        Map<String, String> categoryIds = Collections.singletonMap("cat", "parentCat1");
        assertThat(CategoryServiceUtil.collectCategoryIdsFromMap(categoryIds).size(), is(2));
    }

    @Test
    public void shouldFilterNullValueAndReturn() {
        Map<String, String> categoryIds = Collections.singletonMap("cat", null);
        assertThat(CategoryServiceUtil.collectCategoryIdsFromMap(categoryIds).size(), is(1));
    }

    @Test
    public void shouldFilterNullKeyAndReturn() {
        Map<String, String> categoryIds = Collections.singletonMap(null, "cat");
        assertThat(CategoryServiceUtil.collectCategoryIdsFromMap(categoryIds).size(), is(1));
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionWhenGetSizeOnNullMap() {
        CategoryServiceUtil.collectCategoryIdsFromMap(null).size();
    }

    @Test
    public void shouldCategoryIdsSizeWhenMapIsEmpty() {
        Map<String, String> categoryIds = Collections.emptyMap();
        assertThat(CategoryServiceUtil.collectCategoryIdsFromMap(categoryIds).size(), is(0));
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionWhenInputMapIsNull() {
        CategoryServiceUtil.collectCategoryIdsFromMap(null);
    }

    @Test
    public void shouldReturnCurrentCategoryIdWhenNoNavPathAndNoDefaultPathProvidedInRequest() {
        Map<String, String> categoryIds = new HashMap<>();
        categoryIds.put("cat42960827", "cat17740747");
        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id("cat42960827").defaultPath("").build();
        for (Map.Entry<String, String > entrySet: categoryIds.entrySet()) {
            List<String> pathCategoryIds = CategoryServiceUtil.collectPathCategoryIds(Optional.empty(), entrySet, categoryDocument);
            assertThat(pathCategoryIds.size(), is(1));
            assertThat(pathCategoryIds.get(0), is("cat42960827"));
        }

    }

    @Test
    public void shouldReturnCategoryIdListFromDefaultPathWhenNoNavPathInRequest() {
        Map<String, String> categoryIds = new HashMap<>();
        categoryIds.put("cat42960827", "");
        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id("cat42960827").defaultPath("cat000000_cat000001_cat17740747_cat42960827").build();
        for (Map.Entry<String, String > entrySet: categoryIds.entrySet()) {
            List<String> pathCategoryIds = CategoryServiceUtil.collectPathCategoryIds(Optional.empty(), entrySet, categoryDocument);
            assertThat(pathCategoryIds.size(), is(4));
            assertThat(Arrays.asList("cat000000", "cat000001", "cat17740747", "cat42960827"), is(pathCategoryIds));
        }
    }

    @Test
    public void shouldReturnCategoryIdListFromNavPathWhenNavPathInRequest() {
        Map<String, String> categoryIds = new HashMap<>();
        categoryIds.put("cat42960827", "cat17740747");
        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id("cat42960827").defaultPath("cat000000_cat000001_cat17740747_cat42960827").build();
        for (Map.Entry<String, String > entrySet: categoryIds.entrySet()) {
            List<String> pathCategoryIds = CategoryServiceUtil.collectPathCategoryIds(Optional.of("cat0_cat1_cat2_cat42960827"), entrySet, categoryDocument);
            assertThat(pathCategoryIds.size(), is(4));
            assertThat(Arrays.asList("cat0", "cat1", "cat2", "cat42960827"), is(pathCategoryIds));
        }
    }

    @Test
    public void shouldReturnCategoryIdListWhenPathNOTProvidedInRequestAndPathNotProvidedInDefaultPath() {
        Map<String, String> categoryIds = new HashMap<>();
        categoryIds.put("shouldReturnThis", "");
        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id("shouldNOTReturnThis").defaultPath("").build();
        for (Map.Entry<String, String > entrySet: categoryIds.entrySet()) {
            List<String> pathCategoryIds = CategoryServiceUtil.collectPathCategoryIds(Optional.empty(), entrySet, categoryDocument);
            assertThat(pathCategoryIds.size(), is(1));
            assertThat(pathCategoryIds.get(0), is("shouldReturnThis"));
        }
    }

    @Test
    public void shouldReturnEmptyListWhenBothCategoryIdAndDefaultPathNotProvided() {
        Map<String, String> categoryIds = new HashMap<>();
        categoryIds.put("", "");
        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id("shouldNOTReturnThis").defaultPath("").build();
        for (Map.Entry<String, String > entrySet: categoryIds.entrySet()) {
            assertThat(CategoryServiceUtil.collectPathCategoryIds(Optional.empty(), entrySet, categoryDocument).size(), is(0));
        }
    }
}