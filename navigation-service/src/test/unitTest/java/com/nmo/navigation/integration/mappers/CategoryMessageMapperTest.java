package com.sixthday.navigation.integration.mappers;

import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.elasticsearch.models.*;
import com.sixthday.navigation.api.models.SearchCriteriaBuilder;
import com.sixthday.navigation.api.models.SearchCriteriaOptionsBuilder;
import com.sixthday.navigation.integration.messages.CategoryMessage;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static com.sixthday.navigation.integration.messages.CategoryMessage.EventType.CATEGORY_UPDATED;
import static java.util.Collections.*;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CategoryMessageMapperTest {

    private CategoryMessageMapper categoryMessageMapper = new CategoryMessageMapper();
    private CategoryMessage categoryMessage;
    private SearchCriteria testSearchCriteria;

    @Before
    public void setUp() {
        categoryMessage = new CategoryMessage();
        categoryMessage.setId("Cat123");
        categoryMessage.setEventType(CATEGORY_UPDATED);
        testSearchCriteria = new SearchCriteriaBuilder().build();
        categoryMessage.setSearchCriteria(testSearchCriteria);
    }

    @Test
    public void shouldMapCategoryMessageAttributesToCategoryDocument() {
        CategoryMessage categoryMessage = new CategoryMessage();

        categoryMessage.setId("Cat123");
        categoryMessage.setDisplayName("Rommel Shoes");
        categoryMessage.setTemplateType("ChanelP3");
        categoryMessage.setName("Rommel Shoes");
        categoryMessage.setLeftNavImageAvailableOverride("LeftNavImageAvailableOverride");
        categoryMessage.setAlternateSeoName("AlternateSeoName");
        categoryMessage.setSeoTitleOverride("AlternateSeoName");
        categoryMessage.setCanonicalUrl("CanonicalUrl");
        categoryMessage.setSeoContentTitle("SeoContentTitle");
        categoryMessage.setSeoContentDescription("SeoContentDescription");
        categoryMessage.setFirstSellableProductImageUrl("http://some-url/product-image-url.jpg");
        categoryMessage.setSeoTags("<setSeoTags title=\"one title\">");

        categoryMessage.setBoutique(true);
        categoryMessage.setBoutiqueChild(true);
        categoryMessage.setImageAvailable(true);
        categoryMessage.setMobileHideEntrySubcats(true);
        categoryMessage.setLeftNavImageAvailable(true);
        categoryMessage.setExpandCategory(true);
        categoryMessage.setDontShowChildren(true);
        categoryMessage.setPersonalized(true);
        categoryMessage.setHidden(true);
        categoryMessage.setNoResults(true);
        categoryMessage.setDisplayAsGroups(true);
        categoryMessage.setDriveToGroupPDP(true);
        categoryMessage.setExcludeFromPCS(true);
        categoryMessage.setProductRefinements(new ProductRefinements());
        categoryMessage.setIncludeAllItems(false);
        categoryMessage.setParents(new HashMap<String, Integer>() {{
            put("cat000000", 0);
        }});

        categoryMessage.setSearchCriteria(new SearchCriteriaBuilder().build());
        categoryMessage.setCmosCatalogCodes(new ArrayList<>(Arrays.asList(new String[]{"NMID1", "NMID2"})));
        categoryMessage.setNewArrivalLimit(1);
        categoryMessage.setPreferredProductIds(new ArrayList<>(Arrays.asList(new String[]{"prod1234", "prod1235", "prod1236"})));
        Filter applicableFilter = new Filter("LifeStyle", "lifestyle", emptyList(), Arrays.asList("LS1", "LS2"));
        categoryMessage.setApplicableFilters(Arrays.asList(applicableFilter));
        categoryMessage.setThumbImageShot("z");
        categoryMessage.setRedirectTo("cat123");
        categoryMessage.setRedirectType("301");
        categoryMessage.setDeleted(true);

        CategoryDocument categoryDocument = categoryMessageMapper.map(categoryMessage);

        assertEquals(categoryDocument.getId(), categoryMessage.getId());
        assertEquals(categoryDocument.getDisplayName(), categoryMessage.getDisplayName());
        assertEquals(categoryDocument.getTemplateType(), categoryMessage.getTemplateType());
        assertEquals(categoryDocument.getName(), categoryMessage.getName());
        assertEquals(categoryDocument.getLeftNavImageAvailableOverride(), categoryMessage.getLeftNavImageAvailableOverride());

        assertEquals(categoryDocument.getAlternateSeoName(), categoryMessage.getAlternateSeoName());
        assertEquals(categoryDocument.getSeoTitleOverride(), categoryMessage.getSeoTitleOverride());
        assertEquals(categoryDocument.getCanonicalUrl(), categoryMessage.getCanonicalUrl());
        assertEquals(categoryDocument.getSeoContentTitle(), categoryMessage.getSeoContentTitle());
        assertEquals(categoryDocument.getSeoContentDescription(), categoryMessage.getSeoContentDescription());
        assertEquals(categoryDocument.getFirstSellableProductImageUrl(), categoryMessage.getFirstSellableProductImageUrl());
        assertEquals(categoryDocument.getSeoTags(), categoryMessage.getSeoTags());

        assertEquals(categoryDocument.isBoutique(), categoryMessage.isBoutique());
        assertEquals(categoryDocument.isBoutiqueChild(), categoryMessage.isBoutiqueChild());
        assertEquals(categoryDocument.isImageAvailable(), categoryMessage.isImageAvailable());
        assertEquals(categoryDocument.isMobileHideEntrySubcats(), categoryMessage.isMobileHideEntrySubcats());
        assertEquals(categoryDocument.isLeftNavImageAvailable(), categoryMessage.isLeftNavImageAvailable());
        assertEquals(categoryDocument.isExpandCategory(), categoryMessage.isExpandCategory());
        assertEquals(categoryDocument.isDontShowChildren(), categoryMessage.isDontShowChildren());
        assertEquals(categoryDocument.isPersonalized(), categoryMessage.isPersonalized());
        assertEquals(categoryDocument.isHidden(), categoryMessage.isHidden());
        assertEquals(categoryDocument.isNoResults(), categoryMessage.isNoResults());
        assertEquals(categoryDocument.isDisplayAsGroups(), categoryMessage.isDisplayAsGroups());
        assertEquals(categoryDocument.isDriveToGroupPDP(), categoryMessage.isDriveToGroupPDP());
        assertEquals(categoryDocument.isExcludeFromPCS(), categoryMessage.isExcludeFromPCS());
        assertEquals(categoryDocument.getSearchCriteria().getInclude().getAttributes().size(), categoryMessage.getSearchCriteria().getInclude().getAttributes().size());
        assertEquals(categoryDocument.isIncludeAllItems(), categoryMessage.isIncludeAllItems());
        assertEquals(categoryDocument.isShowAllProducts(), categoryMessage.isShowAllProducts());

        assertEquals(categoryDocument.getSearchCriteria().getExclude().getAttributes().size(), categoryMessage.getSearchCriteria().getExclude().getAttributes().size());
        assertEquals(categoryDocument.getCmosCatalogCodes(), categoryMessage.getCmosCatalogCodes());
        assertEquals(categoryDocument.getNewArrivalLimit(), categoryMessage.getNewArrivalLimit());
        assertEquals(categoryDocument.getPreferredProductIds(), categoryMessage.getPreferredProductIds());
        assertEquals(categoryDocument.getApplicableFilters(), categoryMessage.getApplicableFilters());
        assertEquals(categoryDocument.getApplicableFilters().get(0).getValues(), Arrays.asList("LS1", "LS2"));
        assertThat(categoryDocument.getThumbImageShot(), is("z"));
        assertThat(categoryDocument.getRedirectTo(), is("cat123"));
        assertThat(categoryDocument.getRedirectType(), is("301"));
        assertThat(categoryDocument.getParents().get("cat000000"), is(0));
        assertThat(categoryDocument.isDeleted(), is(true));
    }

    @Test
    public void shouldMapSearchCriteriaWithPromotionsAndHierarchyAndAttributes() {

        Map<String, String> hierarchy = new HashMap<>();
        hierarchy.put("level1", "Woman's apparel");
        hierarchy.put("level2", "Dresses");

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("Life Style", singletonList("brands"));
        attributes.put("Dresses", singletonList("some dresses"));

        final SearchCriteriaOptions include =
                new SearchCriteriaOptionsBuilder()
                        .withAttributes(singletonList(attributes))
                        .withPromotions(Arrays.asList("promo1", "promo2"))
                        .withHierarchy(singletonList(hierarchy))
                        .build();

        final SearchCriteriaOptions exclude =
                new SearchCriteriaOptionsBuilder()
                        .withAttributes(singletonList(attributes))
                        .withPromotions(Arrays.asList("promo1", "promo2"))
                        .withHierarchy(singletonList(hierarchy))
                        .build();

        final SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setInclude(include);
        searchCriteria.setExclude(exclude);
        categoryMessage = new CategoryMessage();
        categoryMessage.setSearchCriteria(searchCriteria);

        CategoryDocument categoryDocument = categoryMessageMapper.map(categoryMessage);
        assertNotNull(categoryDocument.getSearchCriteria().getInclude());
        assertNotNull(categoryDocument.getSearchCriteria().getExclude());

        assertThat(categoryDocument.getSearchCriteria().getInclude().getPromotions(), is(Arrays.asList("promo1", "promo2")));
        assertThat(categoryDocument.getSearchCriteria().getInclude().getAttributes(), is(include.getAttributes()));
        assertThat(categoryDocument.getSearchCriteria().getInclude().getHierarchy(), is(include.getHierarchy()));

        assertThat(categoryDocument.getSearchCriteria().getExclude().getPromotions(), is(Arrays.asList("promo1", "promo2")));
        assertThat(categoryDocument.getSearchCriteria().getExclude().getAttributes(), is(exclude.getAttributes()));
        assertThat(categoryDocument.getSearchCriteria().getExclude().getHierarchy(), is(exclude.getHierarchy()));

    }

    @Test
    public void shouldReturnZeroAsDefaultValueForNewArrivalLimit() {
        categoryMessage.setNewArrivalLimit(null);
        CategoryDocument categoryDocument = categoryMessageMapper.map(categoryMessage);
        assertThat(categoryDocument.getNewArrivalLimit(), is(0));
    }

    @Test
    public void shouldReturnEmptyListAsDefaultValueForCatalogCodes() {
        categoryMessage.setCmosCatalogCodes(null);
        CategoryDocument categoryDocument = categoryMessageMapper.map(categoryMessage);
        assertThat(categoryDocument.getCmosCatalogCodes(), is(emptyList()));
    }

    @Test
    public void shouldReturnEmptyListForHierarchyAsDefaultValueForSearchCriteriaOption() {
        SearchCriteriaOptions testSearchCriteriaOptions = new SearchCriteriaOptionsBuilder().withHierarchy(null).build();
        testSearchCriteria.setInclude(testSearchCriteriaOptions);
        categoryMessage.setSearchCriteria(testSearchCriteria);
        CategoryDocument categoryDocument = categoryMessageMapper.map(categoryMessage);
        assertThat(categoryDocument.getSearchCriteria().getInclude().getHierarchy(), is(emptyList()));
    }

    @Test
    public void shouldReturnEmptyListForPromotionsAsDefaultValueForSearchCriteriaOption() {
        SearchCriteriaOptions testSearchCriteriaOptions = new SearchCriteriaOptionsBuilder().withPromotions(null).build();
        testSearchCriteria.setInclude(testSearchCriteriaOptions);
        categoryMessage.setSearchCriteria(testSearchCriteria);
        CategoryDocument categoryDocument = categoryMessageMapper.map(categoryMessage);
        assertThat(categoryDocument.getSearchCriteria().getInclude().getPromotions(), is(emptyList()));
    }

    @Test
    public void shouldReturnEmptyListForAttributesAsDefaultValueForSearchCriteriaOption() {
        SearchCriteriaOptions testSearchCriteriaOptions = new SearchCriteriaOptionsBuilder().withAttributes(null).build();
        testSearchCriteria.setInclude(testSearchCriteriaOptions);
        categoryMessage.setSearchCriteria(testSearchCriteria);
        CategoryDocument categoryDocument = categoryMessageMapper.map(categoryMessage);
        assertThat(categoryDocument.getSearchCriteria().getInclude().getAttributes(), is(emptyList()));
    }

    @Test
    public void shouldCreateCategoryMessageWhenParentsArePassedAsNull() {
        categoryMessage.setParents(null);

        CategoryDocument categoryDocument = categoryMessageMapper.map(categoryMessage);

        assertThat(categoryDocument.getParents(), is(emptyMap()));
    }
    
    @Test
    public void shouldMapEmptyApplicableFilterValuesWhenMessageHasEmptyList() {
        CategoryMessage categoryMessage = new CategoryMessage();
        categoryMessage.setId("Cat123");
        categoryMessage.setDisplayName("CategoryName");
        categoryMessage.setSearchCriteria(new SearchCriteriaBuilder().build());
        Filter applicableFilter = new Filter("Type", "WEBDIM1", emptyList(), emptyList());
        categoryMessage.setApplicableFilters(Arrays.asList(applicableFilter));

        CategoryDocument categoryDocument = categoryMessageMapper.map(categoryMessage);
        assertThat(categoryDocument.getApplicableFilters().get(0).getValues(), is(emptyList()));
    }
    
    @Test
    public void shouldMapNullApplicableFilterValuesWhenMessageHasNullValues() {
        CategoryMessage categoryMessage = new CategoryMessage();
        categoryMessage.setId("Cat123");
        categoryMessage.setDisplayName("CategoryName");
        categoryMessage.setSearchCriteria(new SearchCriteriaBuilder().build());
        Filter applicableFilter = new Filter("Category", "WEBDIM2", emptyList(), null);
        categoryMessage.setApplicableFilters(Arrays.asList(applicableFilter));

        CategoryDocument categoryDocument = categoryMessageMapper.map(categoryMessage);
        assertThat(categoryDocument.getApplicableFilters().get(0).getValues(), is(Matchers.nullValue()));
    }
    
}
