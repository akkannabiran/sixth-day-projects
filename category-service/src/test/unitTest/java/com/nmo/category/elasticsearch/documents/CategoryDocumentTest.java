package com.sixthday.category.elasticsearch.documents;

import com.sixthday.category.config.CategoryServiceConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.elasticsearch.models.ContextualProperty;

import com.sixthday.navigation.api.elasticsearch.models.Filter;
import com.sixthday.navigation.api.elasticsearch.models.SearchCriteria;
import lombok.SneakyThrows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CategoryDocumentTest {
	
	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private CategoryServiceConfig categoryServiceConfig;

    @Test
    public void shouldAssertCategoryDocumentProperties() {
        List<String> childCategoryOrder = new ArrayList<>();
        childCategoryOrder.add("childCategory1");
        childCategoryOrder.add("childCategory2");
        String driveToSubcategoryId = "";

        ContextualProperty contextualProperty1 = new ContextualProperty("parentCategoryId", "DesktopAlternateName", "MobileAlternateName", childCategoryOrder, driveToSubcategoryId, false);
        ContextualProperty contextualProperty2 = new ContextualProperty("idCat1", "", "", childCategoryOrder, driveToSubcategoryId, false);
        final List<ContextualProperty> contextualProperties = Arrays.asList(contextualProperty1, contextualProperty2);

        List<String> children = new ArrayList<>();
        children.add("child1");
        children.add("child2");

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id("myCat")
                .name("Category Name")
                .templateType("some template type")
                .longDescription("http://some-redirect-url")
                .redirectType("some redirect type")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url/")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .alternateSeoName("some alternate seo name")
                .seoTitleOverride("some seo title override")
                .canonicalUrl("/some_canonical_url/")
                .children(children)
                .contextualProperties(contextualProperties)
                .excludedCountries(Collections.singletonList("US"))
                .mobileHideEntrySubcats(true)
                .hidden(true)
                .preferredProductIds(Arrays.asList("prod1","prod2"))
                .searchCriteria(SearchCriteria.builder().build())
                .build();

        assertThat(categoryDocument.getId(), is("myCat"));
        assertThat(categoryDocument.getName(), is("Category Name"));
        assertThat(categoryDocument.getTemplateType(), is("some template type"));
        assertThat(categoryDocument.getLongDescription(), is("http://some-redirect-url"));
        assertThat(categoryDocument.getRedirectType(), is("some redirect type"));
        assertThat(categoryDocument.getRedirectTo(), is("some redirect to value"));
        assertThat(categoryDocument.getFirstSellableProductImageUrl(), is("/some_first_sellable_product_image_url/"));
        assertThat(categoryDocument.getSeoTags(), is("some seo tag"));
        assertThat(categoryDocument.getSeoContentTitle(), is("some seo content title"));
        assertThat(categoryDocument.getSeoContentDescription(), is("some seo content description"));
        assertThat(categoryDocument.getAlternateSeoName(), is("some alternate seo name"));
        assertThat(categoryDocument.getSeoTitleOverride(), is("some seo title override"));
        assertThat(categoryDocument.getCanonicalUrl(), is("/some_canonical_url/"));
        assertThat(categoryDocument.getChildren(), is(children));
        assertThat(categoryDocument.getExcludedCountries(), is(Collections.singletonList("US")));
        assertThat(categoryDocument.getContextualProperties(), is(contextualProperties));
        assertThat(categoryDocument.getPreferredProductIds(), is(Arrays.asList("prod1","prod2")));
        assertNull(categoryDocument.getSearchCriteria().getInclude());
        assertNull(categoryDocument.getSearchCriteria().getExclude());
    }

    @Test
    public void shouldUseHtmlUnescapeToFormatName() {
        CategoryDocument categoryDocument = CategoryDocument.builder()
                .name("Herm&#232;s")
                .build();

        assertThat(categoryDocument.getName(), is("Hermès"));
    }

    @Test
    public void shouldAssertCategoryDocumentHiddenProperties() {
        CategoryDocument categoryDocument = CategoryDocument.builder()
                .templateType("some template type")
                .hidden(true)
                .build();

        assertThat(categoryDocument.isHidden(), is(true));
    }

    @Test
    public void shouldAssertCategoryDocumentHideMobileEntrySubcatsProperties() {
        CategoryDocument categoryDocument = CategoryDocument.builder()
                .templateType("some template type")
                .mobileHideEntrySubcats(true)
                .build();

        assertThat(categoryDocument.isMobileHideEntrySubcats(), is(true));
    }

    @Test
    public void shouldGetNameBasedOnContextualProperty() {
        List<String> childCategoryOrder = new ArrayList<>();
        childCategoryOrder.add("childCategory1");
        childCategoryOrder.add("childCategory2");
        String driveToSubcategoryId = "";

        ContextualProperty contextualProperty1 = new ContextualProperty("parentCategoryId", "DesktopAlternateName", "MobileAlternateName", childCategoryOrder, driveToSubcategoryId, false);
        ContextualProperty contextualProperty2 = new ContextualProperty("idCat1", "", "", childCategoryOrder, driveToSubcategoryId, false);
        final List<ContextualProperty> contextualProperties = Arrays.asList(contextualProperty1, contextualProperty2);

        List<String> children = new ArrayList<>();
        children.add("childCategory1");
        children.add("child1");
        children.add("child2");

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id("myCat")
                .name("Category Name")
                .templateType("some template type")
                .longDescription("http://some-redirect-url")
                .redirectType("some redirect type")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url/")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .alternateSeoName("some alternate seo name")
                .seoTitleOverride("some seo title override")
                .children(children)
                .contextualProperties(contextualProperties)
                .build();

        ContextualProperty contextualProperty = categoryDocument.getApplicableContextualProperty("parentCategoryId").orElse(null);
        String name = categoryDocument.getName(contextualProperty);
        assertThat(name, is("DesktopAlternateName"));
        assertNotNull(contextualProperty);
        assertThat(contextualProperty.getDesktopAlternateName(), is("DesktopAlternateName"));
    }

    @Test
    public void shouldGetNameBasedOnDefaultCondition() {
        List<String> childCategoryOrder = new ArrayList<>();
        childCategoryOrder.add("childCategory1");
        childCategoryOrder.add("childCategory2");
        String driveToSubcategoryId = "";

        ContextualProperty contextualProperty1 = new ContextualProperty("parentCategoryIdNew", "DesktopAlternateName", "MobileAlternateName", childCategoryOrder, driveToSubcategoryId, false);
        ContextualProperty contextualProperty2 = new ContextualProperty("idCat1", "", "", childCategoryOrder, driveToSubcategoryId, false);
        final List<ContextualProperty> contextualProperties = Arrays.asList(contextualProperty1, contextualProperty2);

        List<String> children = new ArrayList<>();
        children.add("childCategory1");
        children.add("child1");
        children.add("child2");

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id("myCat")
                .name("Category Name")
                .templateType("some template type")
                .longDescription("http://some-redirect-url")
                .redirectType("some redirect type")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url/")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .alternateSeoName("some alternate seo name")
                .seoTitleOverride("some seo title override")
                .children(children)
                .contextualProperties(contextualProperties)
                .build();

        final Optional<ContextualProperty> contextualProperty = categoryDocument.getApplicableContextualProperty("parentCategoryId");
        String name = categoryDocument.getName(contextualProperty.orElse(null));
        assertThat(name, is("Category Name"));
        assertFalse(contextualProperty.isPresent());
    }

    @Test
    public void shouldGetNameBasedOnDefaultConditionAndContextualPropertyIsNull() {
        List<String> children = new ArrayList<>();
        children.add("childCategory1");
        children.add("child1");
        children.add("child2");

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id("myCat")
                .name("Category Name")
                .templateType("some template type")
                .longDescription("http://some-redirect-url")
                .redirectType("some redirect type")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url/")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .alternateSeoName("some alternate seo name")
                .seoTitleOverride("some seo title override")
                .children(children)
                .contextualProperties(null)
                .build();

        final Optional<ContextualProperty> contextualProperty = categoryDocument.getApplicableContextualProperty("parentCategoryId");
        String name = categoryDocument.getName(contextualProperty.orElse(null));
        assertThat(name, is("Category Name"));
        assertFalse(contextualProperty.isPresent());
    }

    @Test
    public void shouldGetNameBasedOnContextualPropertyDesktopAlternateName() {
        List<String> childCategoryOrder = new ArrayList<>();
        childCategoryOrder.add("childCategory1");
        childCategoryOrder.add("childCategory2");
        String driveToSubcategoryId = "";

        ContextualProperty contextualProperty1 = ContextualProperty.builder()
                .parentId("parentCategoryId")
                .mobileAlternateName("MobileAlternateName")
                .childCategoryOrder(childCategoryOrder)
                .driveToSubcategoryId(driveToSubcategoryId)
                .redText(false)
                .build();

        ContextualProperty contextualProperty2 = ContextualProperty.builder()
                .parentId("Id1")
                .mobileAlternateName("")
                .childCategoryOrder(childCategoryOrder)
                .driveToSubcategoryId(driveToSubcategoryId)
                .redText(false)
                .build();

        final List<ContextualProperty> contextualProperties = Arrays.asList(contextualProperty1, contextualProperty2);

        List<String> children = new ArrayList<>();
        children.add("childCategory1");
        children.add("child1");
        children.add("child2");

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id("myCat")
                .name("Category Name")
                .templateType("some template type")
                .longDescription("http://some-redirect-url")
                .redirectType("some redirect type")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url/")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .alternateSeoName("some alternate seo name")
                .seoTitleOverride("some seo title override")
                .children(children)
                .contextualProperties(contextualProperties)
                .build();

        final Optional<ContextualProperty> contextualProperty = categoryDocument.getApplicableContextualProperty("parentCategoryId");
        String name = categoryDocument.getName(contextualProperty.orElse(null));
        assertThat(name, is("Category Name"));
    }

    @Test
    public void shouldGetSeoPageTitleBasedOnSeoTitleOverride() {
        List<String> childCategoryOrder = new ArrayList<>();
        childCategoryOrder.add("childCategory1");
        childCategoryOrder.add("childCategory2");
        String driveToSubcategoryId = "";

        ContextualProperty contextualProperty1 = new ContextualProperty("parentCategoryId", "DesktopAlternateName", "MobileAlternateName", childCategoryOrder, driveToSubcategoryId, false);
        ContextualProperty contextualProperty2 = new ContextualProperty("idCat1", "", "", childCategoryOrder, driveToSubcategoryId, false);
        final List<ContextualProperty> contextualProperties = Arrays.asList(contextualProperty1, contextualProperty2);

        List<String> children = new ArrayList<>();
        children.add("childCategory1");
        children.add("child1");
        children.add("child2");

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id("myCat")
                .name("Category Name")
                .templateType("some template type")
                .longDescription("http://some-redirect-url")
                .redirectType("some redirect type")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url/")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .alternateSeoName("some alternate seo name")
                .seoTitleOverride("some seo title override")
                .children(children)
                .contextualProperties(contextualProperties)
                .build();

        CategoryDocument parentCategoryDocument = CategoryDocument.builder()
                .id("myCat")
                .name("Category Name")
                .templateType("some template type")
                .longDescription("http://some-redirect-url")
                .redirectType("some redirect type")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url/")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .alternateSeoName("some alternate seo name")
                .seoTitleOverride("some seo title override")
                .children(children)
                .contextualProperties(contextualProperties)
                .build();
        
        when(categoryServiceConfig.getSeoContentTitle()).thenReturn("at Neiman Marcus");

        final Optional<ContextualProperty> contextualProperty = categoryDocument.getApplicableContextualProperty("parentCategoryId");
        String seoPageTitle = categoryDocument.getSeoPageTitle(contextualProperty.orElse(null), parentCategoryDocument, categoryServiceConfig);
        assertThat(seoPageTitle, is("some seo title override"));
        assertTrue(contextualProperty.isPresent());
        assertThat(contextualProperty.get().getDesktopAlternateName(), is("DesktopAlternateName"));
    }

    @Test
    public void shouldGetSeoPageTitleBasedOnAlternateSeoName() {
        List<String> childCategoryOrder = new ArrayList<>();
        childCategoryOrder.add("childCategory1");
        childCategoryOrder.add("childCategory2");
        String driveToSubcategoryId = "";

        ContextualProperty contextualProperty1 = new ContextualProperty("parentCategoryId", "DesktopAlternateName", "MobileAlternateName", childCategoryOrder, driveToSubcategoryId, false);
        ContextualProperty contextualProperty2 = new ContextualProperty("idCat1", "", "", childCategoryOrder, driveToSubcategoryId, false);
        final List<ContextualProperty> contextualProperties = Arrays.asList(contextualProperty1, contextualProperty2);

        List<String> children = new ArrayList<>();
        children.add("childCategory1");
        children.add("child1");
        children.add("child2");

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id("myCat")
                .name("Category Name")
                .templateType("some template type")
                .longDescription("http://some-redirect-url")
                .redirectType("some redirect type")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url/")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .alternateSeoName("some alternate seo name")
                .children(children)
                .contextualProperties(contextualProperties)
                .build();

        CategoryDocument parentCategoryDocument = CategoryDocument.builder()
                .id("myCat")
                .name("Category Name")
                .templateType("some template type")
                .longDescription("http://some-redirect-url")
                .redirectType("some redirect type")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url/")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .alternateSeoName("some alternate seo name")
                .children(children)
                .contextualProperties(contextualProperties)
                .build();
        
        when(categoryServiceConfig.getSeoContentTitle()).thenReturn("at Neiman Marcus");

        final ContextualProperty contextualProperty = categoryDocument.getApplicableContextualProperty("parentCategoryId").orElse(null);
        String seoPageTitle = categoryDocument.getSeoPageTitle(contextualProperty, parentCategoryDocument, categoryServiceConfig);
        assertThat(seoPageTitle, is("some alternate seo name in Category Name at Neiman Marcus"));
        assertNotNull(contextualProperty);
        assertThat(contextualProperty.getDesktopAlternateName(), is("DesktopAlternateName"));
    }

    @Test
    public void shouldGetSeoPageTitleBasedOnContextualProperty() {
        List<String> childCategoryOrder = new ArrayList<>();
        childCategoryOrder.add("childCategory1");
        childCategoryOrder.add("childCategory2");
        String driveToSubcategoryId = "";

        ContextualProperty contextualProperty1 = new ContextualProperty("parentCategoryId", "DesktopAlternateName", "MobileAlternateName", childCategoryOrder, driveToSubcategoryId, false);
        ContextualProperty contextualProperty2 = new ContextualProperty("idCat1", "", "", childCategoryOrder, driveToSubcategoryId, false);
        final List<ContextualProperty> contextualProperties = Arrays.asList(contextualProperty1, contextualProperty2);

        List<String> children = new ArrayList<>();
        children.add("childCategory1");
        children.add("child1");
        children.add("child2");

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id("myCat")
                .name("Category Name")
                .templateType("some template type")
                .longDescription("http://some-redirect-url")
                .redirectType("some redirect type")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url/")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .children(children)
                .contextualProperties(contextualProperties)
                .build();

        CategoryDocument parentCategoryDocument = CategoryDocument.builder()
                .id("myCat")
                .name("Category Name")
                .seoTags("some seo tag")
                .build();
        
        when(categoryServiceConfig.getSeoContentTitle()).thenReturn("at Neiman Marcus");

        final ContextualProperty contextualProperty = categoryDocument.getApplicableContextualProperty("parentCategoryId").orElse(null);
        String seoPageTitle = categoryDocument.getSeoPageTitle(contextualProperty, parentCategoryDocument, categoryServiceConfig);
        assertThat(seoPageTitle, is("DesktopAlternateName in Category Name at Neiman Marcus"));
        assertNotNull(contextualProperty);
        assertThat(contextualProperty.getDesktopAlternateName(), is("DesktopAlternateName"));
    }

    @Test
    public void shouldGetSeoPageTitleBasedOnContextualPropertyDesktopAlternateName() {
        List<String> childCategoryOrder = new ArrayList<>();
        childCategoryOrder.add("childCategory1");
        childCategoryOrder.add("childCategory2");
        String driveToSubcategoryId = "";

        ContextualProperty contextualProperty1 = ContextualProperty.builder()
                .parentId("parentCategoryId")
                .mobileAlternateName("MobileAlternateName")
                .childCategoryOrder(childCategoryOrder)
                .driveToSubcategoryId(driveToSubcategoryId)
                .redText(false)
                .build();

        ContextualProperty contextualProperty2 = ContextualProperty.builder()
                .parentId("parentCatId")
                .mobileAlternateName("")
                .childCategoryOrder(childCategoryOrder)
                .driveToSubcategoryId(driveToSubcategoryId)
                .redText(false)
                .build();

        final List<ContextualProperty> contextualProperties = Arrays.asList(contextualProperty1, contextualProperty2);

        List<String> children = new ArrayList<>();
        children.add("childCategory1");
        children.add("child1");
        children.add("child2");

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id("myCat")
                .name("Category Name")
                .templateType("some template type")
                .longDescription("http://some-redirect-url")
                .redirectType("some redirect type")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url/")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .children(children)
                .contextualProperties(contextualProperties)
                .build();

        CategoryDocument parentCategoryDocument = CategoryDocument.builder()
                .id("myCat")
                .name("Category Name")
                .templateType("some template type")
                .longDescription("http://some-redirect-url")
                .redirectType("some redirect type")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url/")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .children(children)
                .contextualProperties(contextualProperties)
                .build();
        
        when(categoryServiceConfig.getSeoContentTitle()).thenReturn("at Neiman Marcus");

        final Optional<ContextualProperty> contextualProperty = categoryDocument.getApplicableContextualProperty("parentCategoryId");
        String seoPageTitle = categoryDocument.getSeoPageTitle(contextualProperty.orElse(null), parentCategoryDocument, categoryServiceConfig);
        assertThat(seoPageTitle, is("Category Name in Category Name at Neiman Marcus"));
    }

    @Test
    public void shouldGetSeoPageTitleBasedOnDefaultCondition() {
        List<String> childCategoryOrder = new ArrayList<>();
        childCategoryOrder.add("childCategory1");
        childCategoryOrder.add("childCategory2");
        String driveToSubcategoryId = "";

        ContextualProperty contextualProperty1 = new ContextualProperty("anyparentCategoryId", "DesktopAlternateName", "MobileAlternateName", childCategoryOrder, driveToSubcategoryId, false);
        ContextualProperty contextualProperty2 = new ContextualProperty("idCat1", "", "", childCategoryOrder, driveToSubcategoryId, false);
        final List<ContextualProperty> contextualProperties = Arrays.asList(contextualProperty1, contextualProperty2);

        List<String> children = new ArrayList<>();
        children.add("childCategory1");
        children.add("child1");
        children.add("child2");

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id("myCat")
                .name("Category Name")
                .templateType("some template type")
                .longDescription("http://some-redirect-url")
                .redirectType("some redirect type")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url/")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .children(children)
                .contextualProperties(contextualProperties)
                .build();

        CategoryDocument parentCategoryDocument = CategoryDocument.builder()
                .id("myCat")
                .name("Category Name")
                .templateType("some template type")
                .longDescription("http://some-redirect-url")
                .redirectType("some redirect type")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url/")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .children(children)
                .contextualProperties(contextualProperties)
                .build();
        
        when(categoryServiceConfig.getSeoContentTitle()).thenReturn("at Neiman Marcus");

        final Optional<ContextualProperty> contextualProperty = categoryDocument.getApplicableContextualProperty("parentCategoryId");
        String seoPageTitle = categoryDocument.getSeoPageTitle(contextualProperty.orElse(null), parentCategoryDocument, categoryServiceConfig);
        assertThat(seoPageTitle, is("Category Name in Category Name at Neiman Marcus"));
    }

    @Test
    public void shouldReturnOptionalEmptyContextualPropertyWhenItIsNull() {
        final List<ContextualProperty> contextualProperties = Arrays.asList(null, null);

        List<String> children = new ArrayList<>();
        children.add("child1");
        children.add("child2");

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id("myCat")
                .name("Category Name")
                .templateType("some template type")
                .longDescription("http://some-redirect-url")
                .redirectType("some redirect type")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url/")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .children(children)
                .contextualProperties(contextualProperties)
                .build();

        ContextualProperty contextualProperty = categoryDocument.getApplicableContextualProperty("parentCategoryId").orElse(null);
        assertNull(contextualProperty);
    }

    @Test
    public void shouldReturnOptionalEmptyContextualPropertyWhenItsParentCategoryIdIsNotAvailable() {
        List<String> childCategoryOrder = new ArrayList<>();
        childCategoryOrder.add("childCategory1");
        childCategoryOrder.add("childCategory2");
        String driveToSubcategoryId = "";

        ContextualProperty contextualProperty1 = ContextualProperty.builder()
                .desktopAlternateName("DesktopAlternateName")
                .mobileAlternateName("MobileAlternateName")
                .childCategoryOrder(childCategoryOrder)
                .driveToSubcategoryId(driveToSubcategoryId)
                .redText(false)
                .build();

        ContextualProperty contextualProperty2 = ContextualProperty.builder()
                .desktopAlternateName("")
                .mobileAlternateName("")
                .childCategoryOrder(childCategoryOrder)
                .driveToSubcategoryId(driveToSubcategoryId)
                .redText(false)
                .build();

        final List<ContextualProperty> contextualProperties = Arrays.asList(contextualProperty1, contextualProperty2);

        List<String> children = new ArrayList<>();
        children.add("child1");
        children.add("child2");

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id("myCat")
                .name("Category Name")
                .templateType("some template type")
                .longDescription("http://some-redirect-url")
                .redirectType("some redirect type")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url/")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .children(children)
                .contextualProperties(contextualProperties)
                .build();

        final ContextualProperty contextualProperty = categoryDocument.getApplicableContextualProperty("parentCategoryId").orElse(null);
        assertNull(contextualProperty);
    }

    @Test
    public void shouldReturnChildCategoryOrder() {
        List<String> childCategoryOrder = new ArrayList<>();
        childCategoryOrder.add("childCategory1");
        childCategoryOrder.add("childCategory2");
        String driveToSubcategoryId = "";

        ContextualProperty contextualProperty1 = ContextualProperty.builder()
                .parentId("parentCategoryId")
                .desktopAlternateName("DesktopAlternateName")
                .mobileAlternateName("MobileAlternateName")
                .driveToSubcategoryId(driveToSubcategoryId)
                .redText(false)
                .build();

        ContextualProperty contextualProperty2 = ContextualProperty.builder()
                .parentId("parentCatId")
                .desktopAlternateName("")
                .mobileAlternateName("")
                .childCategoryOrder(childCategoryOrder)
                .driveToSubcategoryId(driveToSubcategoryId)
                .redText(false)
                .build();

        final List<ContextualProperty> contextualProperties = Arrays.asList(contextualProperty1, contextualProperty2);

        List<String> children = new ArrayList<>();
        children.add("child1");
        children.add("child2");

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id("myCat")
                .name("Category Name")
                .templateType("some template type")
                .longDescription("http://some-redirect-url")
                .redirectType("some redirect type")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url/")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .children(children)
                .contextualProperties(contextualProperties)
                .build();

        final Optional<ContextualProperty> contextualProperty = categoryDocument.getApplicableContextualProperty("parentCategoryId");
        List<String> resultantChildCategoryOrder = categoryDocument.getChildCategoryOrder(contextualProperty.orElse(null));
        assertThat(resultantChildCategoryOrder, is(children));
    }

    @Test
    public void shouldGetDriveToSubCategoryId() {
        List<String> childCategoryOrder = new ArrayList<>();
        childCategoryOrder.add("childCategory1");
        childCategoryOrder.add("childCategory2");
        String driveToSubcategoryId = "driveToChildCategoryId:driveToChildCategoryIdImmediate";

        ContextualProperty contextualProperty1 = new ContextualProperty("parentCategoryId", "DesktopAlternateName", "MobileAlternateName", childCategoryOrder, driveToSubcategoryId, false);
        ContextualProperty contextualProperty2 = new ContextualProperty("idCat1", "", "", childCategoryOrder, driveToSubcategoryId, false);
        final List<ContextualProperty> contextualProperties = Arrays.asList(contextualProperty1, contextualProperty2);

        List<String> children = new ArrayList<>();
        children.add("childCategory1");
        children.add("child1");
        children.add("child2");

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id("myCat")
                .name("Category Name")
                .templateType("some template type")
                .longDescription("http://some-redirect-url")
                .redirectType("some redirect type")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url/")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .alternateSeoName("some alternate seo name")
                .seoTitleOverride("some seo title override")
                .children(children)
                .contextualProperties(contextualProperties)
                .build();

        final Optional<ContextualProperty> contextualProperty = categoryDocument.getApplicableContextualProperty("parentCategoryId");
        Optional<String> driveToSubCategoryId = categoryDocument.getDriveToCategoryId(contextualProperty.orElse(null));
        assertThat(driveToSubCategoryId, is(Optional.of("driveToChildCategoryIdImmediate")));
    }

    @Test
    public void shouldReturnEmptyDriveToSubCategoryIdWhenItsPresentButContextualPropertyIsNull() {
        List<ContextualProperty> contextualProperties = Arrays.asList(null, null);

        List<String> children = new ArrayList<>();
        children.add("childCategory1");
        children.add("child1");
        children.add("child2");

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id("myCat")
                .name("Category Name")
                .templateType("some template type")
                .longDescription("http://some-redirect-url")
                .redirectType("some redirect type")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url/")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .alternateSeoName("some alternate seo name")
                .seoTitleOverride("some seo title override")
                .children(children)
                .contextualProperties(contextualProperties)
                .build();

        final Optional<ContextualProperty> contextualProperty = categoryDocument.getApplicableContextualProperty("parentCategoryId");
        Optional<String> driveToSubCategoryId = categoryDocument.getDriveToCategoryId(contextualProperty.orElse(null));
        assertThat(driveToSubCategoryId, is(Optional.empty()));
        assertFalse(contextualProperty.isPresent());
    }

    @Test
    public void shouldReturnEmptyDriveToSubCategoryIdWhenItsNotPresentButContextualPropertyIsPresent() {
        List<String> childCategoryOrder = new ArrayList<>();
        childCategoryOrder.add("childCategory1");
        childCategoryOrder.add("childCategory2");
        String driveToSubcategoryId = "";

        ContextualProperty contextualProperty1 = new ContextualProperty("parentCategoryId", "DesktopAlternateName", "MobileAlternateName", childCategoryOrder, driveToSubcategoryId, false);
        ContextualProperty contextualProperty2 = new ContextualProperty("idCat1", "", "", childCategoryOrder, driveToSubcategoryId, false);
        final List<ContextualProperty> contextualProperties = Arrays.asList(contextualProperty1, contextualProperty2);

        List<String> children = new ArrayList<>();
        children.add("childCategory1");
        children.add("child1");
        children.add("child2");

        CategoryDocument categoryDocument = CategoryDocument.builder()
                .id("myCat")
                .name("Category Name")
                .templateType("some template type")
                .longDescription("http://some-redirect-url")
                .redirectType("some redirect type")
                .redirectTo("some redirect to value")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url/")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .alternateSeoName("some alternate seo name")
                .seoTitleOverride("some seo title override")
                .children(children)
                .contextualProperties(contextualProperties)
                .build();

        final Optional<ContextualProperty> contextualProperty = categoryDocument.getApplicableContextualProperty("parentCategoryId");
        Optional<String> driveToSubCategoryId = categoryDocument.getDriveToCategoryId(contextualProperty.orElse(null));
        assertThat(driveToSubCategoryId, is(Optional.empty()));
        assertTrue(contextualProperty.isPresent());
    }

    @Test
    public void testReturnsDefaultSeoTags_whenCatedocumentWithOnlySeoTags() {
        CategoryDocument categoryDocument = CategoryDocument.builder().seoTags("seoTags").build();
        when(categoryServiceConfig.getSeoContentTitle()).thenReturn("at Neiman Marcus");
        assertThat(categoryDocument.getSeoTags(null, null, categoryServiceConfig), is("seoTags"));
    }

    @Test
    public void testReturnsSeoTags_whenAtlCatNameTitAndNameOvrAndParentCatNamePresent() {
        CategoryDocument categoryDocument = CategoryDocument.builder().name("name").seoTitleOverride("Plum").alternateSeoName("Peaches").build();
        CategoryDocument parentCategoryDocument = CategoryDocument.builder().name("parentName").build();
        ContextualProperty contextualProperty = ContextualProperty.builder().desktopAlternateName("Banana").build();
        when(categoryServiceConfig.getSeoContentTitle()).thenReturn("at Neiman Marcus");
        assertThat(categoryDocument.getSeoTags(parentCategoryDocument, contextualProperty, categoryServiceConfig), is("<meta name=\"description\" content=\"Shop Peaches in parentName at Neiman Marcus, where you will find free shipping on the latest in fashion from top designers.\"/>"));
    }

    @Test
    public void testReturnsDefaultSeoTags_whenAtlCatNameTitAndNameOvrAndParentCatNamePresent() {
        CategoryDocument categoryDocument = CategoryDocument.builder().name("name").seoTitleOverride("Plum").alternateSeoName("Peaches").seoTags("Default SEO Tags").build();
        CategoryDocument parentCategoryDocument = CategoryDocument.builder().name("parentName").build();
        ContextualProperty contextualProperty = ContextualProperty.builder().desktopAlternateName("Banana").build();
        when(categoryServiceConfig.getSeoContentTitle()).thenReturn("at Neiman Marcus");
        assertThat(categoryDocument.getSeoTags(parentCategoryDocument, contextualProperty, categoryServiceConfig), is("Default SEO Tags"));
    }

    @Test
    public void testReturnsSeoTags_whenAltCatNameAndTitleOverridePresent() {
        CategoryDocument categoryDocument = CategoryDocument.builder().name("name").seoTitleOverride("Plum").build();
        CategoryDocument parentCategoryDocument = CategoryDocument.builder().name("parentName").build();
        ContextualProperty contextualProperty = ContextualProperty.builder().desktopAlternateName("Banana").build();
        when(categoryServiceConfig.getSeoContentTitle()).thenReturn("at Neiman Marcus");
        assertThat(categoryDocument.getSeoTags(parentCategoryDocument, contextualProperty, categoryServiceConfig), is("<meta name=\"description\" content=\"Shop Banana in parentName at Neiman Marcus, where you will find free shipping on the latest in fashion from top designers.\"/>"));
    }

    @Test
    public void testReturnsDefaultSeoTags_whenAltCatNameAndTitleOverrideSeoTagsPresent() {
        CategoryDocument categoryDocument = CategoryDocument.builder().name("name").seoTitleOverride("Plum").seoTags("Default SEO Tags").build();
        CategoryDocument parentCategoryDocument = CategoryDocument.builder().name("parentName").build();
        ContextualProperty contextualProperty = ContextualProperty.builder().desktopAlternateName("Banana").build();
        when(categoryServiceConfig.getSeoContentTitle()).thenReturn("at Neiman Marcus");
        assertThat(categoryDocument.getSeoTags(parentCategoryDocument, contextualProperty, categoryServiceConfig), is("Default SEO Tags"));
    }

    @Test
    public void testReturnsSeoTags_whenAltCatNameAndParentCatNamePresent() {
        CategoryDocument categoryDocument = CategoryDocument.builder().name("name").build();
        CategoryDocument parentCategoryDocument = CategoryDocument.builder().name("parentName").build();
        ContextualProperty contextualProperty = ContextualProperty.builder().desktopAlternateName("Banana").build();
        when(categoryServiceConfig.getSeoContentTitle()).thenReturn("at Neiman Marcus");
        assertThat(categoryDocument.getSeoTags(parentCategoryDocument, contextualProperty, categoryServiceConfig), is("<meta name=\"description\" content=\"Shop Banana in parentName at Neiman Marcus, where you will find free shipping on the latest in fashion from top designers.\"/>"));
    }

    @Test
    public void testReturnsDefaultSeoTags_whenAltCatNameAndParentCatNamePresent() {
        CategoryDocument categoryDocument = CategoryDocument.builder().name("name").seoTags("Default SEO Tags").build();
        CategoryDocument parentCategoryDocument = CategoryDocument.builder().name("parentName").build();
        ContextualProperty contextualProperty = ContextualProperty.builder().desktopAlternateName("Banana").build();
        when(categoryServiceConfig.getSeoContentTitle()).thenReturn("at Neiman Marcus");
        assertThat(categoryDocument.getSeoTags(parentCategoryDocument, contextualProperty, categoryServiceConfig), is("Default SEO Tags"));
    }

    @Test
    public void testReturnsSeoTags_whenCatNameAndParentCatNamePresent() {
        CategoryDocument categoryDocument = CategoryDocument.builder().name("name").build();
        CategoryDocument parentCategoryDocument = CategoryDocument.builder().name("parentName").build();
        when(categoryServiceConfig.getSeoContentTitle()).thenReturn("at Neiman Marcus");
        assertThat(categoryDocument.getSeoTags(parentCategoryDocument, null, categoryServiceConfig), is("<meta name=\"description\" content=\"Shop name in parentName at Neiman Marcus, where you will find free shipping on the latest in fashion from top designers.\"/>"));
    }

    @Test
    public void testReturnsDefaultSeoTags_whenCatNameAndParentCatNamePresent() {
        CategoryDocument categoryDocument = CategoryDocument.builder().name("name").seoTags("Default SEO Tags").build();
        CategoryDocument parentCategoryDocument = CategoryDocument.builder().name("parentName").build();
        when(categoryServiceConfig.getSeoContentTitle()).thenReturn("at Neiman Marcus");
        assertThat(categoryDocument.getSeoTags(parentCategoryDocument, null, categoryServiceConfig), is("Default SEO Tags"));
    }

    @Test
    public void testReturnsSeoTags_whenCatNameTitleNameOverrideAndParentCatNamePresent() {
        CategoryDocument categoryDocument = CategoryDocument.builder().name("name").seoTitleOverride("Plum").alternateSeoName("Peaches").build();
        CategoryDocument parentCategoryDocument = CategoryDocument.builder().name("parentName").build();
        when(categoryServiceConfig.getSeoContentTitle()).thenReturn("at Neiman Marcus");
        assertThat(categoryDocument.getSeoTags(parentCategoryDocument, null, categoryServiceConfig), is("<meta name=\"description\" content=\"Shop Peaches in parentName at Neiman Marcus, where you will find free shipping on the latest in fashion from top designers.\"/>"));
    }

    @Test
    public void testReturnsDefaultSeoTags_whenCatNameTitleNameOverrideAndParentCatNamePresent() {
        CategoryDocument categoryDocument = CategoryDocument.builder().name("name").seoTitleOverride("Plum").alternateSeoName("Peaches").seoTags("Default SEO Tags").build();
        CategoryDocument parentCategoryDocument = CategoryDocument.builder().name("parentName").build();
        when(categoryServiceConfig.getSeoContentTitle()).thenReturn("at Neiman Marcus");
        assertThat(categoryDocument.getSeoTags(parentCategoryDocument, null, categoryServiceConfig), is("Default SEO Tags"));
    }

    @Test
    public void testReturnsSeoTags_whenCatNameAndNameOverrideAndParentCatNamePresent() {
        CategoryDocument categoryDocument = CategoryDocument.builder().name("name").alternateSeoName("Peaches").build();
        CategoryDocument parentCategoryDocument = CategoryDocument.builder().name("parentName").build();
        when(categoryServiceConfig.getSeoContentTitle()).thenReturn("at Neiman Marcus");
        assertThat(categoryDocument.getSeoTags(parentCategoryDocument, null, categoryServiceConfig), is("<meta name=\"description\" content=\"Shop Peaches in parentName at Neiman Marcus, where you will find free shipping on the latest in fashion from top designers.\"/>"));
    }

    @Test
    public void testReturnsDefaultSeoTags_whenCatNameAndNameOverrideAndParentCatNamePresent() {
        CategoryDocument categoryDocument = CategoryDocument.builder().name("name").alternateSeoName("Peaches").seoTags("Default SEO Tags").build();
        CategoryDocument parentCategoryDocument = CategoryDocument.builder().name("parentName").build();
        when(categoryServiceConfig.getSeoContentTitle()).thenReturn("at Neiman Marcus");
        assertThat(categoryDocument.getSeoTags(parentCategoryDocument, null, categoryServiceConfig), is("Default SEO Tags"));
    }

    @Test
    public void testReturnsSeoTags_whenCatNameAndTitleOverrideAndParentCatNamePresent() {
        CategoryDocument categoryDocument = CategoryDocument.builder().name("name").seoTitleOverride("Plum").build();
        CategoryDocument parentCategoryDocument = CategoryDocument.builder().name("parentName").build();
        when(categoryServiceConfig.getSeoContentTitle()).thenReturn("at Neiman Marcus");
        assertThat(categoryDocument.getSeoTags(parentCategoryDocument, null, categoryServiceConfig), is("<meta name=\"description\" content=\"Shop name in parentName at Neiman Marcus, where you will find free shipping on the latest in fashion from top designers.\"/>"));
    }

    @Test
    public void testReturnsDefaultSeoTags_whenCatNameAndTitleOverrideAndParentCatNamePresent() {
        CategoryDocument categoryDocument = CategoryDocument.builder().name("name").seoTitleOverride("Plum").seoTags("Default SEO Tags").build();
        CategoryDocument parentCategoryDocument = CategoryDocument.builder().name("parentName").build();
        when(categoryServiceConfig.getSeoContentTitle()).thenReturn("at Neiman Marcus");
        assertThat(categoryDocument.getSeoTags(parentCategoryDocument, null, categoryServiceConfig), is("Default SEO Tags"));
    }

    @Test(expected = NullPointerException.class)
    public void testReturnsSeoTags_whenSeoTagsAndParentCategoryNameAndAlternateSeoNameAreNotPresent() {
        CategoryDocument categoryDocument = CategoryDocument.builder().id("id").build();
        when(categoryServiceConfig.getSeoContentTitle()).thenReturn("at Neiman Marcus");
        assertThat(categoryDocument.getSeoTags(null, null, null), is("seoTags"));
    }
    
    @Test
    public void testReturnCatalogTreesWhenTypeIsSet() {
        CategoryDocument categoryDocument1 = CategoryDocument.builder().type(Arrays.asList("ABC", "XYZ")).build();
        assertThat(categoryDocument1.getType(), is(Arrays.asList("ABC", "XYZ")));
        
        CategoryDocument categoryDocument2 = CategoryDocument.builder().type(Collections.emptyList()).build();
        assertThat(categoryDocument2.getType(), is(Collections.emptyList()));
        
        CategoryDocument categoryDocument3 = CategoryDocument.builder().type(null).build();
        assertThat(categoryDocument3.getType(), nullValue());
    }
    
    @Test
    @SneakyThrows
    public void testObjectMapperSetsCatalogTreesWhenTypeIsSetInJSON() {
    	String json = "{\"type\": [\"ABC\",\"XYZ\"]}";
        CategoryDocument categoryDocument1 = new ObjectMapper().readValue(json, CategoryDocument.class);
        assertThat(categoryDocument1.getType(), is(Arrays.asList("ABC", "XYZ")));
        
        String json2 = "{\"type\": []}";
        CategoryDocument categoryDocument2 = new ObjectMapper().readValue(json2, CategoryDocument.class);
        assertThat(categoryDocument2.getType(), is(Collections.emptyList()));
        
        String json3 = "{}";
        CategoryDocument categoryDocument3 = new ObjectMapper().readValue(json3, CategoryDocument.class);
        assertThat(categoryDocument3.getType(), nullValue());
        
    }

    @Test
    @SneakyThrows
    public void testObjectMapperSetsFilterWhenFilterIsSetInJSON() {
        String json = "{\"applicableFilters\": [{\n" +
                "        \"defaultName\": \"Designer\",\n" +
                "        \"disabled\": [],\n" +
                "        \"values\": [\n" +
                "          null\n" +
                "        ]\n" +
                "      }]}";
        Filter filter =new Filter();
        filter.setDefaultName("Designer");
        CategoryDocument categoryDocument1 = new ObjectMapper().readValue(json, CategoryDocument.class);
        assertThat(categoryDocument1.getApplicableFilters().get(0).getDefaultName(), is("Designer"));

    }
}