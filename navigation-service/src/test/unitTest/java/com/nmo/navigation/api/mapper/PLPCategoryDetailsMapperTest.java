package com.sixthday.navigation.api.mapper;

import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.api.config.NavigationServiceConfig.CategoryConfig.FilterOption;
import com.sixthday.navigation.api.controllers.elasticsearch.documents.CategoryDocumentBuilder;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.elasticsearch.models.ProductRefinements;
import com.sixthday.navigation.api.mappers.PLPCategoryDetailsMapper;
import com.sixthday.navigation.api.models.response.*;
import com.sixthday.navigation.api.models.response.SortOption.Option;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static com.sixthday.navigation.api.data.CategoryTestDataFactory.*;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PLPCategoryDetailsMapperTest {

    @InjectMocks
    private PLPCategoryDetailsMapper plpCategoryDetailsMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private NavigationServiceConfig navigationServiceConfig;

    private CategoryDocument testCategoryDocument;

    private String ROOT_CATEGORY_ID = "cat000000";
    private Optional<String> EMPTY_PARENT_CATEGORY_ID = Optional.empty();
    private Optional<String> HOME_SILO_CATEGORY_ID = Optional.of("siloCat000553");
    private Optional<String> OTHER_SILO_CATEGORY_ID = Optional.of("siloCat000001");
    private Optional<String> PARENT_CATEGORY_ID = Optional.of("parentCat00001");
    private Optional<String> PARENT_CATEGORY_ID_WITH_EMPTY_DESKTOP_NAME = Optional.of("parentCat00002");
    private Optional<String> INVALID_PARENT_CATEGORY_ID = Optional.of("parentCat00003");
    private Optional<String> PARENT_CATEGORY_ID_WITH_NO_DESKTOP_NAME = Optional.of("parentCat00004");

    @Before
    public void setUp() {
        when(navigationServiceConfig.getCategoryConfig().getHeaderAssetUrl()).thenReturn("/category/{categoryId}/r_long_header.html");
        when(navigationServiceConfig.getCategoryConfig().getReducedChildCountSilos()).thenReturn(Arrays.asList(HOME_SILO_CATEGORY_ID.get()));

    }

    @Test
    public void shouldReturnPLPCategoryDetailsWithNameWhenNoParentIdIsGiven() {
        testCategoryDocument = getTestCategoryDocument("cat123");
        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, EMPTY_PARENT_CATEGORY_ID, Optional.empty());
        assertThat(plpCategoryDetails.getId(), is(testCategoryDocument.getId()));
        assertThat(plpCategoryDetails.getTitle(), is(testCategoryDocument.getName()));
    }

    @Test
    public void shouldMapTemplateType() {
        testCategoryDocument = getTestCategoryDocument("cat123");
        testCategoryDocument.setTemplateType("P4");
        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, EMPTY_PARENT_CATEGORY_ID, Optional.empty());
        assertThat(plpCategoryDetails.getTemplateType(), is(testCategoryDocument.getTemplateType()));
    }

    @Test
    public void shouldMapCategoryGroupsFlags() {
        testCategoryDocument = getTestCategoryDocument("cat123");
        testCategoryDocument.setDisplayAsGroups(true);
        testCategoryDocument.setDriveToGroupPDP(true);
        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, EMPTY_PARENT_CATEGORY_ID, Optional.empty());
        assertEquals(plpCategoryDetails.isDisplayAsGroups(), testCategoryDocument.isDisplayAsGroups());
        assertEquals(plpCategoryDetails.isDriveToGroupPDP(), testCategoryDocument.isDriveToGroupPDP());
    }

    @Test
    public void shouldReturnPLPCategoryDetailsWithDesktopAlternateNameWhenParentIdIsGiven() {
        testCategoryDocument = getTestCategoryDocument("cat123");
        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, PARENT_CATEGORY_ID, Optional.empty());
        assertThat(plpCategoryDetails.getId(), is(testCategoryDocument.getId()));
        assertThat(plpCategoryDetails.getTitle(), is(testCategoryDocument.getContextualProperties().get(0).getDesktopAlternateName()));
    }

    @Test
    public void shouldNotErrorOutWhenContextualPropertiesIsNull() {
        testCategoryDocument = new CategoryDocumentBuilder().withId("cat123").withContextualProperties(null).build();
        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, PARENT_CATEGORY_ID, Optional.empty());
        assertThat(plpCategoryDetails.getId(), is(testCategoryDocument.getId()));
    }

    @Test
    public void shouldReturnPLPCategoryDetailsWithNameWhenParentIdHasNoDesktopAlternateName() {
        testCategoryDocument = getTestCategoryDocument("cat123");
        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, PARENT_CATEGORY_ID_WITH_NO_DESKTOP_NAME, Optional.empty());
        assertThat(plpCategoryDetails.getId(), is(testCategoryDocument.getId()));
        assertThat(plpCategoryDetails.getTitle(), is(testCategoryDocument.getName()));
    }

    @Test
    public void shouldReturnPLPCategoryDetailsWithNameWhenParentIdHasEmptyDesktopAlternateName() {
        testCategoryDocument = getTestCategoryDocument("cat123");
        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, PARENT_CATEGORY_ID_WITH_EMPTY_DESKTOP_NAME, Optional.empty());
        assertThat(plpCategoryDetails.getId(), is(testCategoryDocument.getId()));
        assertThat(plpCategoryDetails.getTitle(), is(testCategoryDocument.getName()));
    }

    @Test
    public void shouldReturnPLPCategoryDetailsWithNameWhenInvalidParentIdIsGiven() {
        testCategoryDocument = getTestCategoryDocument("cat123");
        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, INVALID_PARENT_CATEGORY_ID, Optional.empty());
        assertThat(plpCategoryDetails.getId(), is(testCategoryDocument.getId()));
        assertThat(plpCategoryDetails.getTitle(), is(testCategoryDocument.getName()));
    }

    @Test
    public void shouldSendResponseAsEmptyWhenPreferredProductIdsIsNull() {
        testCategoryDocument = new CategoryDocumentBuilder().withId("cat123").withPreferredProductIds(null).build();
        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, PARENT_CATEGORY_ID, Optional.empty());
        assertThat(plpCategoryDetails.getId(), is(testCategoryDocument.getId()));
        assertThat(plpCategoryDetails.getPreferredProductIds().size(), is(0));
    }

    @Test
    public void shouldReturnPLPCategoryDetailsWithCategoryHeaderAssetWhenImageAvailableIsTrueAndBlankImageOverrideIdAndHideMobileImageTrue() {
        testCategoryDocument = new CategoryDocumentBuilder().withId("cat123").withImageAvailable(true).withImageOverrideCategoryId("").withHideMobileImage(true).build(); //image overrideId has default value must be cleared
        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, PARENT_CATEGORY_ID, Optional.empty());
        assertThat(plpCategoryDetails.getCategoryHeaderAsset().getCategoryAssetUrl(), is("/category/cat123/r_long_header.html"));
        assertThat(plpCategoryDetails.getCategoryHeaderAsset().isHideOnMobile(), is(true));
    }

    @Test
    public void shouldNotReturnPLPCategoryDetailsWithCategoryHeaderAssetWhenImageAvailableIsFalse() {
        testCategoryDocument = new CategoryDocumentBuilder().withImageAvailable(false).build();
        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, PARENT_CATEGORY_ID, Optional.empty());
        assertThat(plpCategoryDetails.getCategoryHeaderAsset().getCategoryAssetUrl(), is(nullValue()));
    }

    @Test
    public void shouldReturnPLPCategoryDetailsWithCategoryHeaderAssetWhenImageAvailableIsTrueAndImageOverrideIsNotNull() {
        testCategoryDocument = new CategoryDocumentBuilder().withImageAvailable(true).withImageOverrideCategoryId("cat456").build();
        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, PARENT_CATEGORY_ID, Optional.empty());
        assertThat(plpCategoryDetails.getCategoryHeaderAsset().getCategoryAssetUrl(), is("/category/cat456/r_long_header.html"));
    }

    @Test
    public void shouldSendResponseAsEmptyWhenApplicableFiltersIsNull() {
        testCategoryDocument = new CategoryDocumentBuilder().withId("cat123").withApplicableFilters(null).build();
        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, PARENT_CATEGORY_ID, Optional.empty());
        assertThat(plpCategoryDetails.getId(), is(testCategoryDocument.getId()));
        assertThat(plpCategoryDetails.getApplicableFilters().size(), is(0));
    }

    @Test
    public void shouldReturnPLPCategoryDetailsWithApplicableFiltersWhenTheyArePresentInCategoryDocument() {
        List<com.sixthday.navigation.api.elasticsearch.models.Filter> expectedApplicableFilters = Arrays.asList(new com.sixthday.navigation.api.elasticsearch.models.Filter("filter1", "filter 1", emptyList(), null),
                new com.sixthday.navigation.api.elasticsearch.models.Filter("filter2", null, Collections.singletonList("abc"), Arrays.asList("Val1", "Val2")));

        testCategoryDocument = new CategoryDocumentBuilder().withId("cat123").withApplicableFilters(new ArrayList<>(expectedApplicableFilters)).build();
        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, PARENT_CATEGORY_ID, Optional.empty());
        assertThat(plpCategoryDetails.getId(), is(testCategoryDocument.getId()));
        assertThat(plpCategoryDetails.getApplicableFilters().size(), is(2));
        assertThat(plpCategoryDetails.getApplicableFilters().get(0), is(new Filter("filter1", "filter 1", emptyList(), null)));
        assertThat(plpCategoryDetails.getApplicableFilters().get(1), is(new Filter("filter2", "filter2", Collections.singletonList("abc"), Arrays.asList("Val1", "Val2"))));
    }
    
    @Test
    public void shouldReturnPLPCategoryDetailsWithApplicableFiltersWithEmptyValuesWhenTheyArePresentInCategoryDocumentWithEmptyValues() {
        List<com.sixthday.navigation.api.elasticsearch.models.Filter> expectedApplicableFilters = Arrays.asList(new com.sixthday.navigation.api.elasticsearch.models.Filter("filter1", "filter 1", emptyList(), null),
                new com.sixthday.navigation.api.elasticsearch.models.Filter("filter2", null, Collections.singletonList("abc"), Collections.emptyList()));

        testCategoryDocument = new CategoryDocumentBuilder().withId("cat123").withApplicableFilters(new ArrayList<>(expectedApplicableFilters)).build();
        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, PARENT_CATEGORY_ID, Optional.empty());
        assertThat(plpCategoryDetails.getId(), is(testCategoryDocument.getId()));
        assertThat(plpCategoryDetails.getApplicableFilters().size(), is(2));
        assertThat(plpCategoryDetails.getApplicableFilters().get(0), is(new Filter("filter1", "filter 1", emptyList(), null)));
        assertThat(plpCategoryDetails.getApplicableFilters().get(1), is(new Filter("filter2", "filter2", Collections.singletonList("abc"), Collections.emptyList())));
    }

    @Test
    public void shouldReturnPLPCategoriesWithSeoDetailsWhenContentAndTitleArePresent() {
        SeoDetails expectedSeoDetails = new SeoDetails("name override", "canonicalUrl", "title", "seo tags", "title override", "content", new RedirectDetails(301, "cat123"));
        testCategoryDocument = new CategoryDocumentBuilder().withId("cat123").withSeoTitleOverride("title override").withCanonicalUrl("canonicalUrl").withAlternateSeoName("name override").withSeoTags("seo tags").withSeoContentDescription("content").withSeoContentTitle("title").build();

        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, PARENT_CATEGORY_ID, Optional.empty());
        assertThat(plpCategoryDetails.getSeo().getNameOverride(), is(expectedSeoDetails.getNameOverride()));
        assertThat(plpCategoryDetails.getSeo().getCanonicalUrl(), is(expectedSeoDetails.getCanonicalUrl()));
        assertThat(plpCategoryDetails.getSeo().getTitle(), is(expectedSeoDetails.getTitle()));
        assertThat(plpCategoryDetails.getSeo().getMetaInformation(), is(expectedSeoDetails.getMetaInformation()));
        assertThat(plpCategoryDetails.getSeo().getTitleOverride(), is(expectedSeoDetails.getTitleOverride()));
        assertThat(plpCategoryDetails.getSeo().getContent(), is(expectedSeoDetails.getContent()));
        assertThat(plpCategoryDetails.getSeo().getRedirectDetails().getHttpCode(), is(expectedSeoDetails.getRedirectDetails().getHttpCode()));
        assertThat(plpCategoryDetails.getSeo().getRedirectDetails().getRedirectToCategory(), is(expectedSeoDetails.getRedirectDetails().getRedirectToCategory()));
    }

    @Test
    public void shouldReturnOrderedSortOptionsWhenNotExcludingPCSAndNoPreferredProducts() {
        testCategoryDocument = new CategoryDocumentBuilder().withExcludeFromPCS(true).build();
        testCategoryDocument.setPreferredProductIds(emptyList());

        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, Optional.empty(), Optional.empty());

        assertThat(plpCategoryDetails.getAvailableSortOptions().size(), is(4));
        assertThat(plpCategoryDetails.getAvailableSortOptions().get(0).getValue(), is(Option.PRICE_HIGH_TO_LOW));
        assertThat(plpCategoryDetails.getAvailableSortOptions().get(0).getIsDefault(), is(false));

        assertThat(plpCategoryDetails.getAvailableSortOptions().get(1).getValue(), is(Option.PRICE_LOW_TO_HIGH));
        assertThat(plpCategoryDetails.getAvailableSortOptions().get(1).getIsDefault(), is(false));

        assertThat(plpCategoryDetails.getAvailableSortOptions().get(2).getValue(), is(Option.NEWEST_FIRST));
        assertThat(plpCategoryDetails.getAvailableSortOptions().get(2).getIsDefault(), is(true));

        assertThat(plpCategoryDetails.getAvailableSortOptions().get(3).getValue(), is(Option.MY_FAVORITES));
        assertThat(plpCategoryDetails.getAvailableSortOptions().get(3).getIsDefault(), is(false));

    }

    @Test
    public void shouldReturnOrderedSortOptionsWhenNotExcludingPCSWithPreferredProducts() {
        testCategoryDocument = new CategoryDocumentBuilder().withExcludeFromPCS(true).build(); //default builder adds 3 products

        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, Optional.empty(), Optional.empty());

        assertThat(plpCategoryDetails.getAvailableSortOptions().size(), is(5));

        assertThat(plpCategoryDetails.getAvailableSortOptions().get(0).getValue(), is(Option.PRICE_HIGH_TO_LOW));
        assertThat(plpCategoryDetails.getAvailableSortOptions().get(0).getIsDefault(), is(false));

        assertThat(plpCategoryDetails.getAvailableSortOptions().get(1).getValue(), is(Option.PRICE_LOW_TO_HIGH));
        assertThat(plpCategoryDetails.getAvailableSortOptions().get(1).getIsDefault(), is(false));

        assertThat(plpCategoryDetails.getAvailableSortOptions().get(2).getValue(), is(Option.NEWEST_FIRST));
        assertThat(plpCategoryDetails.getAvailableSortOptions().get(2).getIsDefault(), is(false));

        assertThat(plpCategoryDetails.getAvailableSortOptions().get(3).getValue(), is(Option.FEATURED));
        assertThat(plpCategoryDetails.getAvailableSortOptions().get(3).getIsDefault(), is(true));

        assertThat(plpCategoryDetails.getAvailableSortOptions().get(4).getValue(), is(Option.MY_FAVORITES));
        assertThat(plpCategoryDetails.getAvailableSortOptions().get(4).getIsDefault(), is(false));
    }

    @Test
    public void shouldReturnOrderedSortOptionsWhenExcludingPCSFalse() {
        testCategoryDocument = new CategoryDocumentBuilder().build();

        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, Optional.empty(), Optional.empty());

        assertThat(plpCategoryDetails.getAvailableSortOptions().size(), is(5));

        assertThat(plpCategoryDetails.getAvailableSortOptions().get(0).getValue(), is(Option.PRICE_HIGH_TO_LOW));
        assertThat(plpCategoryDetails.getAvailableSortOptions().get(0).getIsDefault(), is(false));

        assertThat(plpCategoryDetails.getAvailableSortOptions().get(1).getValue(), is(Option.PRICE_LOW_TO_HIGH));
        assertThat(plpCategoryDetails.getAvailableSortOptions().get(1).getIsDefault(), is(false));

        assertThat(plpCategoryDetails.getAvailableSortOptions().get(2).getValue(), is(Option.NEWEST_FIRST));
        assertThat(plpCategoryDetails.getAvailableSortOptions().get(2).getIsDefault(), is(false));

        assertThat(plpCategoryDetails.getAvailableSortOptions().get(3).getValue(), is(Option.BEST_MATCH));
        assertThat(plpCategoryDetails.getAvailableSortOptions().get(3).getIsDefault(), is(true));

        assertThat(plpCategoryDetails.getAvailableSortOptions().get(4).getValue(), is(Option.MY_FAVORITES));
        assertThat(plpCategoryDetails.getAvailableSortOptions().get(4).getIsDefault(), is(false));
    }

    @Test
    public void shouldReturnDefaultSortOption() {
        testCategoryDocument = new CategoryDocumentBuilder().build(); //has default products

        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, Optional.empty(), Optional.empty());

        assertThat(plpCategoryDetails.getDefaultSortOption(), is(Option.BEST_MATCH.toString()));
    }

    @Test
    public void shouldReturnPCSEnabledFalseWhenPCSExcludeFlagIsTrue() {
        boolean excludePCS = true;
        testCategoryDocument = new CategoryDocumentBuilder().withExcludeFromPCS(excludePCS).build();

        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, Optional.empty(), Optional.empty());

        assertThat(plpCategoryDetails.getPcsEnabled(), is(!excludePCS));
    }

    @Test
    public void shouldReturnPCSEnabledTrueWhenPCSExcludeFlagIsFalse() {
        boolean excludePCS = false;
        testCategoryDocument = new CategoryDocumentBuilder().withExcludeFromPCS(excludePCS).build();

        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, Optional.empty(), Optional.empty());

        assertThat(plpCategoryDetails.getPcsEnabled(), is(!excludePCS));
    }

    @Test
    public void shouldReturnImageTemplateType() {
        testCategoryDocument = new CategoryDocumentBuilder().withThumbImageShot("z").build();

        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, Optional.empty(), Optional.empty());

        assertThat(plpCategoryDetails.getImageTemplateType(), is("z"));
    }

    @Test
    public void shouldReturnCategoryRedirectTypeAndRedirectUrlInSeoObject() {
        testCategoryDocument = new CategoryDocumentBuilder().withRedirectType("301").withRedirectTo("cat123").build();

        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, Optional.empty(), Optional.empty());

        assertThat(plpCategoryDetails.getSeo().getRedirectDetails().getHttpCode(), is(301));
        assertThat(plpCategoryDetails.getSeo().getRedirectDetails().getRedirectToCategory(), is("cat123"));
    }

    @Test
    public void shouldReturnDiscountSortOptionsWhenCategoryProductRefinementsIsSaleOnly() {
        ProductRefinements saleOnlyRefinement = new ProductRefinements();
        saleOnlyRefinement.setSaleOnly(true);
        testCategoryDocument = new CategoryDocumentBuilder().withProductRefinements(saleOnlyRefinement).build();

        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, Optional.empty(), Optional.empty());

        assertThat(plpCategoryDetails.getAvailableSortOptions().size(), is(7));

        assertThat(plpCategoryDetails.getAvailableSortOptions().get(0).getValue(), is(Option.PRICE_HIGH_TO_LOW));
        assertThat(plpCategoryDetails.getAvailableSortOptions().get(0).getIsDefault(), is(false));

        assertThat(plpCategoryDetails.getAvailableSortOptions().get(1).getValue(), is(Option.PRICE_LOW_TO_HIGH));
        assertThat(plpCategoryDetails.getAvailableSortOptions().get(1).getIsDefault(), is(false));

        assertThat(plpCategoryDetails.getAvailableSortOptions().get(2).getValue(), is(Option.DISCOUNT_HIGH_TO_LOW));
        assertThat(plpCategoryDetails.getAvailableSortOptions().get(2).getIsDefault(), is(false));

        assertThat(plpCategoryDetails.getAvailableSortOptions().get(3).getValue(), is(Option.DISCOUNT_LOW_TO_HIGH));
        assertThat(plpCategoryDetails.getAvailableSortOptions().get(3).getIsDefault(), is(false));

        assertThat(plpCategoryDetails.getAvailableSortOptions().get(4).getValue(), is(Option.NEWEST_FIRST));
        assertThat(plpCategoryDetails.getAvailableSortOptions().get(4).getIsDefault(), is(false));

        assertThat(plpCategoryDetails.getAvailableSortOptions().get(5).getValue(), is(Option.BEST_MATCH));
        assertThat(plpCategoryDetails.getAvailableSortOptions().get(5).getIsDefault(), is(true));

        assertThat(plpCategoryDetails.getAvailableSortOptions().get(6).getValue(), is(Option.MY_FAVORITES));
        assertThat(plpCategoryDetails.getAvailableSortOptions().get(6).getIsDefault(), is(false));
    }

    @Test
    public void shouldReturnFilterOptionsFromConsulConfigWhenCategoryHasNoDimensionsAndAttributesFilterOptionsAreEmpty() {
        FilterOption designerFilter = new FilterOption();
        designerFilter.setDisplayText("Designer");
        designerFilter.setFilterKey("Designer");

        FilterOption categoryFilter = new FilterOption();
        categoryFilter.setDisplayText("Category");
        categoryFilter.setFilterKey("level1");

        FilterOption instoreFilter = new FilterOption();
        instoreFilter.setDisplayText("In Store");
        instoreFilter.setFilterKey("inStore");

        FilterOption typeFilter = new FilterOption();
        typeFilter.setDisplayText("Type");
        typeFilter.setFilterKey("level2");

        List<FilterOption> manualCategoryFilterOptions = new ArrayList<>();
        manualCategoryFilterOptions.add(designerFilter);
        manualCategoryFilterOptions.add(categoryFilter);
        manualCategoryFilterOptions.add(instoreFilter);
        manualCategoryFilterOptions.add(typeFilter);

        CategoryDocument manualCategoryDocument = getCategoryDocumentWithEmptySearchCriteria();
        when(navigationServiceConfig.getCategoryConfig().getFilterOptions()).thenReturn(manualCategoryFilterOptions);

        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(manualCategoryDocument, Optional.empty(), Optional.empty());

        List<Filter> applicableFilters = plpCategoryDetails.getApplicableFilters();
        assertThat(applicableFilters.get(0).getFilterKey(), is("Designer"));
        assertThat(applicableFilters.get(0).getDisplayText(), is("Designer"));
        assertThat(applicableFilters.get(1).getFilterKey(), is("level1"));
        assertThat(applicableFilters.get(1).getDisplayText(), is("Category"));
        assertThat(applicableFilters.get(2).getFilterKey(), is("inStore"));
        assertThat(applicableFilters.get(2).getDisplayText(), is("In Store"));
        assertThat(applicableFilters.get(3).getFilterKey(), is("level2"));
        assertThat(applicableFilters.get(3).getDisplayText(), is("Type"));
    }

    @Test
    public void shouldReturnEmptyFilterOptionsWhenCategoryFilterOptionsAndConsulConfigurationsAreEmpty() {
        testCategoryDocument = getTestCategoryDocument("cat123");
        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, Optional.empty(), Optional.empty());
        List<Filter> applicableFilters = plpCategoryDetails.getApplicableFilters();
        assertThat(applicableFilters.isEmpty(), is(true));
    }

    @Test
    public void shouldNotReturnFilterOptionsFromConsulConfigWhenCategoryHasValidSearchCriteriaButFilterOptionsAreEmpty() {
        FilterOption designerFilter = new FilterOption();
        designerFilter.setDisplayText("Designer");
        designerFilter.setFilterKey("Designer");

        FilterOption categoryFilter = new FilterOption();
        categoryFilter.setDisplayText("Category");
        categoryFilter.setFilterKey("level1");

        FilterOption instoreFilter = new FilterOption();
        instoreFilter.setDisplayText("In Store");
        instoreFilter.setFilterKey("inStore");

        FilterOption typeFilter = new FilterOption();
        typeFilter.setDisplayText("Type");
        typeFilter.setFilterKey("level2");

        List<FilterOption> manualCategoryFilterOptions = new ArrayList<>();
        manualCategoryFilterOptions.add(designerFilter);
        manualCategoryFilterOptions.add(categoryFilter);
        manualCategoryFilterOptions.add(instoreFilter);
        manualCategoryFilterOptions.add(typeFilter);

        testCategoryDocument = getTestCategoryDocument("cat123");
        when(navigationServiceConfig.getCategoryConfig().getFilterOptions()).thenReturn(manualCategoryFilterOptions);

        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, Optional.empty(), Optional.empty());

        List<Filter> applicableFilters = plpCategoryDetails.getApplicableFilters();
        assertThat(applicableFilters.isEmpty(), is(true));
    }

    @Test
    public void shouldSetReduceChildCountToTrueWhenSiloIdIsInReduceChildConfiguration() {
        testCategoryDocument = getTestCategoryDocument("cat123");

        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, PARENT_CATEGORY_ID, HOME_SILO_CATEGORY_ID);

        assertEquals(true, plpCategoryDetails.isReducedChildCount());
    }

    @Test
    public void shouldSetReducedChildCountToFalseWhenSiloIdIsNotInReduceChildConfiguration() {
        testCategoryDocument = getTestCategoryDocument("cat123");

        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, PARENT_CATEGORY_ID, OTHER_SILO_CATEGORY_ID);

        assertEquals(false, plpCategoryDetails.isReducedChildCount());
    }

    @Test
    public void shouldUseDefaultSiloWhenSiloIdIsNotPresent() {
        testCategoryDocument = getTestCategoryDocument("cat123");
        String defaultPath = ROOT_CATEGORY_ID
                + "_" + HOME_SILO_CATEGORY_ID.get()
                + "_" + PARENT_CATEGORY_ID.get()
                + "_" + CATEGORY_ID;
        testCategoryDocument.setDefaultPath(defaultPath);

        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, PARENT_CATEGORY_ID, Optional.empty());

        assertEquals(true, plpCategoryDetails.isReducedChildCount());
    }

    @Test
    public void testReturnsEmptyStringWhenDefaultPathHasSingleCategoryId() {
        testCategoryDocument = getTestCategoryDocument("cat123");
        testCategoryDocument.setDefaultPath("cat1");
        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, INVALID_PARENT_CATEGORY_ID, Optional.empty());
        assertThat(plpCategoryDetails.getId(), is(testCategoryDocument.getId()));
        assertThat(plpCategoryDetails.getTitle(), is(testCategoryDocument.getName()));
    }

    @Test
    public void testShouldReturnNullDirectDetailsWhenRedirectTypeIsInvalid() {
        testCategoryDocument = getTestCategoryDocument("cat123");
        testCategoryDocument.setRedirectType("");
        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, INVALID_PARENT_CATEGORY_ID, Optional.empty());
        assertNull(plpCategoryDetails.getSeo().getRedirectDetails());
    }

    @Test
    public void shouldReturnPLPCategoryDetailsWithNameWhenParentIdIsNull() {
        testCategoryDocument = getTestCategoryDocument("cat123");
        testCategoryDocument.getContextualProperties().get(0).setParentId(null);
        testCategoryDocument.getContextualProperties().get(1).setParentId(null);
        PLPCategoryDetails plpCategoryDetails = plpCategoryDetailsMapper.map(testCategoryDocument, INVALID_PARENT_CATEGORY_ID, Optional.empty());
        assertThat(plpCategoryDetails.getId(), is(testCategoryDocument.getId()));
        assertThat(plpCategoryDetails.getTitle(), is(testCategoryDocument.getName()));
    }
}

