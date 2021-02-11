package com.sixthday.category.api.utils;

import com.sixthday.category.config.CategoryServiceConfig;
import com.sixthday.category.exceptions.UnknownCategoryTemplateTypeException;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.elasticsearch.models.SearchCriteria;
import com.sixthday.navigation.api.elasticsearch.models.SearchCriteriaOptions;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CategoryUtilTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CategoryServiceConfig categoryServiceConfig;

    private CategoryServiceConfig.CategoryTemplate anyCategoryTemplate = new CategoryServiceConfig.CategoryTemplate();

    @Test
    @SneakyThrows
    public void testConstructorIsPrivate() {
        Constructor<CategoryUtil> constructor = CategoryUtil.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test(expected = UnknownCategoryTemplateTypeException.class)
    public void shouldThrowUnknownCategoryTemplateTypeExceptionWhenUnsupportedTemplateTypeFound() {
        anyCategoryTemplate.setName("X0");
        anyCategoryTemplate.setKey("some");
        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(anyCategoryTemplate));

        CategoryDocument categoryDocument = CategoryDocument.builder().id("myCat").templateType("some template").build();
        CategoryUtil.getCategoryTemplateType(categoryDocument, categoryServiceConfig);
    }

    @Test
    public void shouldReturnTemplateKeyWhenSupportedTemplateTypeFound() {
        anyCategoryTemplate.setName("X0");
        anyCategoryTemplate.setKey("X");
        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(anyCategoryTemplate));

        CategoryDocument categoryDocument = CategoryDocument.builder().id("xCat").templateType("X0").build();
        String templateKey = CategoryUtil.getCategoryTemplateType(categoryDocument, categoryServiceConfig);
        assertThat("X", equalTo(templateKey));
    }

    @Test
    public void shouldReturnLongDescriptionAsRedirectUrlValue() {
        anyCategoryTemplate.setName("X0");
        anyCategoryTemplate.setKey("X");
        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(anyCategoryTemplate));

        CategoryDocument categoryDocument = CategoryDocument.builder().id("xCat").templateType("X0").longDescription("long description redirect URL").build();
        String redirectUrl = CategoryUtil.getRedirectUrl(categoryDocument);
        assertThat("long description redirect URL", equalTo(redirectUrl));
    }

    @Test
    public void shouldReturnRedirectToAsRedirectUrlValue() {
        anyCategoryTemplate.setName("X0");
        anyCategoryTemplate.setKey("X");
        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(anyCategoryTemplate));

        CategoryDocument categoryDocument = CategoryDocument.builder().id("xCat").templateType("X0").redirectTo("redirect to redirect url").build();
        String redirectUrl = CategoryUtil.getRedirectUrl(categoryDocument);
        assertThat("redirect to redirect url", equalTo(redirectUrl));
    }

    @Test
    public void shouldReturnRedirectToAsRedirectUrlValueWhenLongDescriptionAndRedirectToArePresent() {
        anyCategoryTemplate.setName("X0");
        anyCategoryTemplate.setKey("X");
        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(anyCategoryTemplate));

        CategoryDocument categoryDocument = CategoryDocument.builder().id("xCat").templateType("X0").redirectTo("redirect to redirect url").longDescription("long description redirect URL").build();
        String redirectUrl = CategoryUtil.getRedirectUrl(categoryDocument);
        assertThat("redirect to redirect url", equalTo(redirectUrl));
    }

    @Test
    public void shouldReturnRedirectToAsNullWhenLongDescriptionAndRedirectToAreNotPresent() {
        anyCategoryTemplate.setName("X0");
        anyCategoryTemplate.setKey("X");
        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(anyCategoryTemplate));

        CategoryDocument categoryDocument = CategoryDocument.builder().id("xCat").templateType("X0").build();
        assertNull(CategoryUtil.getRedirectUrl(categoryDocument));
    }

    @Test
    public void shouldReturnImageUrlWithDomainAdded() {
        anyCategoryTemplate.setName("X0");
        anyCategoryTemplate.setKey("X");
        when(categoryServiceConfig.getImageServerUrl()).thenReturn("//images.sixthday.com");
        when(categoryServiceConfig.getDefaultProductImageSrc()).thenReturn("/assets/images/no-image.c9a49578722aabed021ab4821bf0e705.jpeg");

        CategoryDocument categoryDocument = CategoryDocument.builder().id("xCat").templateType("X0").firstSellableProductImageUrl("/image-src/product.jpg").build();
        assertThat(CategoryUtil.getProductImageUrl(categoryDocument.getFirstSellableProductImageUrl(), categoryServiceConfig), equalTo("//images.sixthday.com/image-src/product.jpg"));
    }

    @Test
    public void shouldReturnNoImageUrl() {
        anyCategoryTemplate.setName("X0");
        anyCategoryTemplate.setKey("X");
        when(categoryServiceConfig.getImageServerUrl()).thenReturn("//images.sixthday.com");
        when(categoryServiceConfig.getDefaultProductImageSrc()).thenReturn("/assets/images/no-image.c9a49578722aabed021ab4821bf0e705.jpeg");

        CategoryDocument categoryDocument = CategoryDocument.builder().id("xCat").templateType("X0").build();
        assertThat(CategoryUtil.getProductImageUrl(categoryDocument.getFirstSellableProductImageUrl(), categoryServiceConfig), equalTo("/assets/images/no-image.c9a49578722aabed021ab4821bf0e705.jpeg"));
    }

    @Test
    public void shouldReturnFalseWhenCategoryDocumentHasEmptySearchCriteria() {
        CategoryDocument categoryDocumentWithEmptySearchCriteria = CategoryDocument.builder()
                .id("xCat")
                .templateType("X0")
                .build();
        assertFalse(CategoryUtil.isDynamicCategory(categoryDocumentWithEmptySearchCriteria));
    }

    @Test
    public void shouldReturnTrueWhenCategoryDocumentHasSearchFactors() {
        Map<String, String> hierarchyMap = new HashMap<>();
        hierarchyMap.put("level1","Accessories");
        SearchCriteriaOptions include = SearchCriteriaOptions.builder()
                .attributes(Collections.emptyList())
                .hierarchy(Arrays.asList(hierarchyMap))
                .promotions(Collections.singletonList("promotions"))
                .build();

        SearchCriteriaOptions exclude = SearchCriteriaOptions.builder()
                .attributes(Collections.emptyList())
                .hierarchy(Collections.emptyList())
                .promotions(Collections.singletonList("excludePromotions"))
                .build();

        CategoryDocument categoryDocumentWithSearchCriteria = CategoryDocument.builder()
                .id("xCat")
                .templateType("X0")
                .searchCriteria(SearchCriteria.builder().include(include).exclude(exclude).build())
                .build();
        assertTrue(CategoryUtil.isDynamicCategory(categoryDocumentWithSearchCriteria));
    }

    @Test
    public void shouldReturnFalseWhenCategoryDocumentHasEmptySearchFactors() {
        CategoryDocument categoryDocumentWithEmptySearchFactors = CategoryDocument.builder()
                .id("xCat")
                .templateType("X0")
                .searchCriteria(SearchCriteria.builder().build())
                .build();
        assertFalse(CategoryUtil.isDynamicCategory(categoryDocumentWithEmptySearchFactors));
    }

    @Test
    public void shouldReturnTrueWhenCategoryDocumentHasEmptyExcludeSearchFactor() {
        Map<String, String> hierarchyMap = new HashMap<>();
        hierarchyMap.put("level1","Accessories");
        SearchCriteriaOptions include = SearchCriteriaOptions.builder()
                .attributes(Collections.emptyList())
                .hierarchy(Arrays.asList(hierarchyMap))
                .promotions(Collections.singletonList("promotions"))
                .build();
        CategoryDocument categoryDocumentWithEmptyExcludeSearchFactor = CategoryDocument.builder()
                .id("xCat")
                .templateType("X0")
                .searchCriteria(SearchCriteria.builder().include(include).build())
                .build();
        assertTrue(CategoryUtil.isDynamicCategory(categoryDocumentWithEmptyExcludeSearchFactor));
    }

    @Test
    public void shouldReturnTrueWhenCategoryDocumentHasNoNullExcludeSearchFactor() {
        Map<String, String> hierarchyMap = new HashMap<>();
        hierarchyMap.put("level1","Accessories");
        SearchCriteriaOptions include = SearchCriteriaOptions.builder()
                .attributes(Collections.emptyList())
                .hierarchy(Arrays.asList(hierarchyMap))
                .promotions(Collections.singletonList("promotions"))
                .build();
        CategoryDocument categoryDocumentWithNoNullExcludeSearchFactor = CategoryDocument.builder()
                .id("xCat")
                .templateType("X0")
                .searchCriteria(SearchCriteria.builder().include(include).exclude(SearchCriteriaOptions.builder().build()).build())
                .build();
        assertTrue(CategoryUtil.isDynamicCategory(categoryDocumentWithNoNullExcludeSearchFactor));
    }

    @Test
    public void shouldReturnTrueWhenCategoryDocumentHasEmptyIncludeSearchFactor() {
        Map<String, String> hierarchyMap = new HashMap<>();
        hierarchyMap.put("level1","Accessories");
        SearchCriteriaOptions exclude = SearchCriteriaOptions.builder()
                .attributes(Collections.emptyList())
                .hierarchy(Arrays.asList(hierarchyMap))
                .promotions(Collections.singletonList("excludePromotions"))
                .build();
        CategoryDocument categoryDocumentWithEmptyIncludeSearchFactor = CategoryDocument.builder()
                .id("xCat")
                .templateType("X0")
                .searchCriteria(SearchCriteria.builder().exclude(exclude).build())
                .build();
        assertTrue(CategoryUtil.isDynamicCategory(categoryDocumentWithEmptyIncludeSearchFactor));
    }

    @Test
    public void shouldReturnTrueWhenCategoryDocumentHasNoNullIncludeSearchFactor() {
        Map<String, String> hierarchyMap = new HashMap<>();
        hierarchyMap.put("level1","Accessories");
        SearchCriteriaOptions exclude = SearchCriteriaOptions.builder()
                .attributes(Collections.emptyList())
                .hierarchy(Arrays.asList(hierarchyMap))
                .promotions(Collections.singletonList("excludePromotions"))
                .build();
        CategoryDocument categoryDocumentWithNoNullIncludeSearchFactor = CategoryDocument.builder()
                .id("xCat")
                .templateType("X0")
                .searchCriteria(SearchCriteria.builder().exclude(exclude).include(SearchCriteriaOptions.builder().build()).build())
                .build();
        assertTrue(CategoryUtil.isDynamicCategory(categoryDocumentWithNoNullIncludeSearchFactor));
    }

    @Test
    public void shouldReturnTrueWhenCategoryDocumentHasIncludeAttributesNonNullExclude() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("Material", Arrays.asList("Fur","Leather"));
        SearchCriteriaOptions includeAttributes = SearchCriteriaOptions.builder()
                .attributes(Arrays.asList(attributes))
                .build();
        CategoryDocument categoryDocumentWithIncludeAttributesNonNullExclude = CategoryDocument.builder()
                .id("xCat")
                .templateType("X0")
                .searchCriteria(SearchCriteria.builder().include(includeAttributes).exclude(SearchCriteriaOptions.builder().build()).build())
                .build();
        assertTrue(CategoryUtil.isDynamicCategory(categoryDocumentWithIncludeAttributesNonNullExclude));
    }

    @Test
    public void shouldReturnTrueWhenCategoryDocumentHasIncludeAttributesWithNullExclude() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("Material", Arrays.asList("Fur","Leather"));
        SearchCriteriaOptions includeAttributes = SearchCriteriaOptions.builder()
                .attributes(Arrays.asList(attributes))
                .build();
        CategoryDocument categoryDocumentWithIncludeAttributesWithNullExclude = CategoryDocument.builder()
                .id("xCat")
                .templateType("X0")
                .searchCriteria(SearchCriteria.builder().include(includeAttributes).build())
                .build();
        assertTrue(CategoryUtil.isDynamicCategory(categoryDocumentWithIncludeAttributesWithNullExclude));
    }

    @Test
    public void shouldReturnTrueWhenCategoryDocumentHasIncludeHierarchyNonNullExclude() {
        Map<String, String> hierarchyMap = new HashMap<>();
        hierarchyMap.put("level1","Accessories");
        SearchCriteriaOptions includeHierarchy = SearchCriteriaOptions.builder()
                .hierarchy(Arrays.asList(hierarchyMap))
                .build();
        CategoryDocument categoryDocumentWithIncludeHierarchyNonNullExclude = CategoryDocument.builder()
                .id("xCat")
                .templateType("X0")
                .searchCriteria(SearchCriteria.builder().include(includeHierarchy).exclude(SearchCriteriaOptions.builder().build()).build())
                .build();
        assertTrue(CategoryUtil.isDynamicCategory(categoryDocumentWithIncludeHierarchyNonNullExclude));
    }

    @Test
    public void shouldReturnTrueWhenCategoryDocumentHasIncludeHierarchyWithNullExclude() {
        Map<String, String> hierarchyMap = new HashMap<>();
        hierarchyMap.put("level1","Accessories");
        SearchCriteriaOptions includeHierarchy = SearchCriteriaOptions.builder()
                .hierarchy(Arrays.asList(hierarchyMap))
                .build();
        CategoryDocument categoryDocumentWithIncludeHierarchyWithNullExclude = CategoryDocument.builder()
                .id("xCat")
                .templateType("X0")
                .searchCriteria(SearchCriteria.builder().include(includeHierarchy).build())
                .build();
        assertTrue(CategoryUtil.isDynamicCategory(categoryDocumentWithIncludeHierarchyWithNullExclude));
    }

    @Test
    public void shouldReturnTrueWhenCategoryDocumentHasIncludePromotionsNonNullExclude() {
        SearchCriteriaOptions includePromotions = SearchCriteriaOptions.builder()
                .promotions(Collections.singletonList("promotions"))
                .build();
        CategoryDocument categoryDocumentWithIncludePromotionsNonNullExclude = CategoryDocument.builder()
                .id("xCat")
                .templateType("X0")
                .searchCriteria(SearchCriteria.builder().include(includePromotions).exclude(SearchCriteriaOptions.builder().build()).build())
                .build();
        assertTrue(CategoryUtil.isDynamicCategory(categoryDocumentWithIncludePromotionsNonNullExclude));
    }

    @Test
    public void shouldReturnTrueWhenCategoryDocumentHasIncludePromotionsWithNullExclude() {
        SearchCriteriaOptions includePromotions = SearchCriteriaOptions.builder()
                .promotions(Collections.singletonList("promotions"))
                .build();
        CategoryDocument categoryDocumentWithIncludePromotionsWithNullExclude = CategoryDocument.builder()
                .id("xCat")
                .templateType("X0")
                .searchCriteria(SearchCriteria.builder().include(includePromotions).build())
                .build();
        assertTrue(CategoryUtil.isDynamicCategory(categoryDocumentWithIncludePromotionsWithNullExclude));
    }

    @Test
    public void shouldReturnTrueWhenCategoryDocumentHasExcludeAttributesWithNonNullInclude() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("Material", Arrays.asList("Fur","Leather"));
        SearchCriteriaOptions excludeAttribute = SearchCriteriaOptions.builder()
                .attributes(Arrays.asList(attributes))
                .build();
        CategoryDocument categoryDocumentWithExcludeAttributesWithNonNullInclude = CategoryDocument.builder()
                .id("xCat")
                .templateType("X0")
                .searchCriteria(SearchCriteria.builder().include(SearchCriteriaOptions.builder().build()).exclude(excludeAttribute).build())
                .build();
        assertTrue(CategoryUtil.isDynamicCategory(categoryDocumentWithExcludeAttributesWithNonNullInclude));
    }

    @Test
    public void shouldReturnTrueWhenCategoryDocumentHasExcludeAttributesWithNullInclude() {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("Material", Arrays.asList("Fur","Leather"));
        SearchCriteriaOptions excludeAttribute = SearchCriteriaOptions.builder()
                .attributes(Arrays.asList(attributes))
                .build();
        CategoryDocument categoryDocumentWithExcludeAttributesWithNullInclude = CategoryDocument.builder()
                .id("xCat")
                .templateType("X0")
                .searchCriteria(SearchCriteria.builder().exclude(excludeAttribute).build())
                .build();
        assertTrue(CategoryUtil.isDynamicCategory(categoryDocumentWithExcludeAttributesWithNullInclude));
    }

    @Test
    public void shouldReturnTrueWhenCategoryDocumentHasExcludeHierarchyNonNullInclude() {
        Map<String, String> hierarchyMap = new HashMap<>();
        hierarchyMap.put("level1","Accessories");
        SearchCriteriaOptions excludeHierarchy = SearchCriteriaOptions.builder()
                .hierarchy(Arrays.asList(hierarchyMap))
                .build();
        CategoryDocument categoryDocumentWithExcludeHierarchyNonNullInclude = CategoryDocument.builder()
                .id("xCat")
                .templateType("X0")
                .searchCriteria(SearchCriteria.builder().include(SearchCriteriaOptions.builder().build()).exclude(excludeHierarchy).build())
                .build();
        assertTrue(CategoryUtil.isDynamicCategory(categoryDocumentWithExcludeHierarchyNonNullInclude));
    }

    @Test
    public void shouldReturnTrueWhenCategoryDocumentHasExcludeHierarchyWithNullInclude() {
        Map<String, String> hierarchyMap = new HashMap<>();
        hierarchyMap.put("level1","Accessories");
        SearchCriteriaOptions excludeHierarchy = SearchCriteriaOptions.builder()
                .hierarchy(Arrays.asList(hierarchyMap))
                .build();
        CategoryDocument categoryDocumentWithExcludeHierarchyWithNullInclude = CategoryDocument.builder()
                .id("xCat")
                .templateType("X0")
                .searchCriteria(SearchCriteria.builder().exclude(excludeHierarchy).build())
                .build();
        assertTrue(CategoryUtil.isDynamicCategory(categoryDocumentWithExcludeHierarchyWithNullInclude));
    }

    @Test
    public void shouldReturnTrueWhenCategoryDocumentHasExcludePromotionsNonNullInclude() {
        SearchCriteriaOptions excludePromotions = SearchCriteriaOptions.builder()
                .promotions(Collections.singletonList("promotions"))
                .build();
        CategoryDocument categoryDocumentWithExcludePromotionsNonNullInclude = CategoryDocument.builder()
                .id("xCat")
                .templateType("X0")
                .searchCriteria(SearchCriteria.builder().include(SearchCriteriaOptions.builder().build()).exclude(excludePromotions).build())
                .build();
        assertTrue(CategoryUtil.isDynamicCategory(categoryDocumentWithExcludePromotionsNonNullInclude));
    }

    @Test
    public void shouldReturnTrueWhenCategoryDocumentHasExcludePromotionsWithNullInclude() {
        SearchCriteriaOptions excludePromotions = SearchCriteriaOptions.builder()
                .promotions(Collections.singletonList("promotions"))
                .build();
        CategoryDocument categoryDocumentWithExcludePromotionsWithNullInclude = CategoryDocument.builder()
                .id("xCat")
                .templateType("X0")
                .searchCriteria(SearchCriteria.builder().exclude(excludePromotions).build())
                .build();
        assertTrue(CategoryUtil.isDynamicCategory(categoryDocumentWithExcludePromotionsWithNullInclude));
    }

    @Test
    public void shouldReturnFalseWhenCategoryDocumentHasEmptyIncludeExclude() {
        CategoryDocument categoryDocumentWithEmptyIncludeExclude = CategoryDocument.builder()
                .id("xCat")
                .templateType("X0")
                .searchCriteria(SearchCriteria.builder().include(SearchCriteriaOptions.builder().build()).exclude(SearchCriteriaOptions.builder().build()).build())
                .build();
        assertFalse(CategoryUtil.isDynamicCategory(categoryDocumentWithEmptyIncludeExclude));
    }

    @Test
    public void shouldReturnFalseWhenCategoryDocumentHasEmptyIncludeNullExclude() {
        CategoryDocument categoryDocumentWithEmptyIncludeNullExclude = CategoryDocument.builder()
                .id("xCat")
                .templateType("X0")
                .searchCriteria(SearchCriteria.builder().include(SearchCriteriaOptions.builder().build()).build())
                .build();
        assertFalse(CategoryUtil.isDynamicCategory(categoryDocumentWithEmptyIncludeNullExclude));
    }

    @Test
    public void shouldReturnFalseWhenCategoryDocumentHasExcludePromotionsWithNullInclude() {
        CategoryDocument categoryDocumentWithEmptyExcludeNullInclude = CategoryDocument.builder()
                .id("xCat")
                .templateType("X0")
                .searchCriteria(SearchCriteria.builder().exclude(SearchCriteriaOptions.builder().build()).build())
                .build();
        assertFalse(CategoryUtil.isDynamicCategory(categoryDocumentWithEmptyExcludeNullInclude));
    }
}
