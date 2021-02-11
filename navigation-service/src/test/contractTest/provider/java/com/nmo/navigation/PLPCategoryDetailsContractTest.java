package com.sixthday.navigation;

import au.com.dius.pact.provider.junit.*;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit.loader.PactBrokerAuth;
import au.com.dius.pact.provider.junit.target.TestTarget;
import au.com.dius.pact.provider.spring.target.MockMvcTarget;
import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.api.controllers.PLPCategoryDetailsController;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.elasticsearch.models.*;
import com.sixthday.navigation.api.exceptions.CategoryNotFoundException;
import com.sixthday.navigation.api.mappers.PLPCategoryDetailsMapper;
import com.sixthday.navigation.api.services.CategoryService;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Mockito.*;

@RunWith(PactRunner.class)
@Provider("navigation-service")
@Consumer("plp-svc")
@PactBroker(protocol = "${pactbroker.protocol}", host = "${pactbroker.hostname}", port = "${pactbroker.port}",
        authentication = @PactBrokerAuth(username = "${pactbroker.username}", password = "${pactbroker.password}"))
public class PLPCategoryDetailsContractTest {

    @TestTarget
    public MockMvcTarget target = new MockMvcTarget();

    @Mock
    private CategoryService categoryService;

    private PLPCategoryDetailsMapper PLPCategoryDetailsMapper;

    private NavigationServiceConfig navigationServiceConfig;

    private CategoryDocument categoryDocument;

    private String id;
    private Optional<String> parentCategoryId;
    private Optional<String> siloCategoryId;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        navigationServiceConfig = mock(NavigationServiceConfig.class, withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS.get()));
        PLPCategoryDetailsMapper = new PLPCategoryDetailsMapper(navigationServiceConfig);
        when(navigationServiceConfig.getCategoryConfig().getHeaderAssetUrl()).thenReturn("https://devint.neimanmarcus.com/category/{categoryId}/r_head_long.html");
        when(navigationServiceConfig.getCategoryConfig().getReducedChildCountSilos()).thenReturn(Collections.singletonList("T1C0001"));
        PLPCategoryDetailsController PLPCategoryDetailsController = new PLPCategoryDetailsController(categoryService, PLPCategoryDetailsMapper);
        target.setControllers(PLPCategoryDetailsController);
        id = "cat43810733";
        parentCategoryId = Optional.of("T1C0001");
        siloCategoryId = Optional.of("T1S0001");
        setCategoryDetailsResponse();
    }

    @State("HasCategoryId")
    public void shouldReturnValidCategoryResponseIfCategoryIdIsPresent() {
        when(categoryService.getCategoryDocument("c1234")).thenReturn(categoryDocument);
    }

    @State("HasNoCategoryId")
    public void shouldReturnNotFoundResponse() {
        when(categoryService.getCategoryDocument("c1")).thenThrow(new CategoryNotFoundException("c1"));
    }
    @State("HasNoCategoryIdInES")
    public void shouldReturnNotFoundResponseWhenCategoryNotPresentInES() {
        when(categoryService.getCategoryDocument("c1")).thenThrow(new CategoryNotFoundException("c1"));
    }
    
    @State("HasNoParentCategoryId")
    public void shouldReturnValidCategoryResponseIfCategoryIdIsPresentAndNoParentCategoryIdIsPresent() {
        when(categoryService.getCategoryDocument("c12")).thenReturn(categoryDocument);
    }

    @State("HasNoSiloId")
    public void shouldReturnValidCategoryResponseIfCategoryIdIsPresentAndNoSiloIdIsPresent() {
        when(categoryService.getCategoryDocument("c12")).thenReturn(categoryDocument);
    }
    
    @State("HasSortOptionsAndOneIsDefault")
    public void shouldReturnValidCategoryResponseWithSortOptions() {
        when(categoryService.getCategoryDocument("c1234")).thenReturn(categoryDocument);
    }
    
    @State("HasImageTemplateType")
    public void shouldReturnValidCategoryResponseWithImageTemplateType() {
        when(categoryService.getCategoryDocument("c1234")).thenReturn(categoryDocument);
    }
    
    @State("HasSeoValues")
    public void shouldReturnValidCategoryResponseWithSEOValues() {
        when(categoryService.getCategoryDocument("c1234")).thenReturn(categoryDocument);
    }
    
    private void setCategoryDetailsResponse() {
        categoryDocument = new CategoryDocument();
        categoryDocument.setId(id);
        categoryDocument.setName("Women's Apparel");

        Map<String, String> includeHierarchy = new HashMap<>(2);
        includeHierarchy.put("level1", "Women's Apparel");
        includeHierarchy.put("level2", "Dresses");

        Map<String, List<String>> includeAttribute1 = new HashMap<>(1);
        includeAttribute1.put("SpotlightOn", new ArrayList<>(Arrays.asList(new String[]{"DT"})));

        Map<String, List<String>> includeAttribute2 = new HashMap<>(1);
        includeAttribute2.put("Trends", new ArrayList<>(Arrays.asList(new String[]{"DT"})));

        List<Map<String, String>> includeHierarchyList = Arrays.asList(includeHierarchy);
        List<Map<String, List<String>>> includeAttributes = Arrays.asList(includeAttribute1, includeAttribute2);

        Map<String, String> excludeHierarchy = new HashMap<>(1);
        excludeHierarchy.put("level1", "Women's Apparel");

        Map<String, List<String>> excludeAttribute = new HashMap<>(1);
        excludeAttribute.put("SpotlightOn", new ArrayList<>(Arrays.asList(new String[]{"DT"})));

        List<Map<String, String>> excludeHierarchyList = Arrays.asList(excludeHierarchy);
        List<Map<String, List<String>>> excludeAttributes = Arrays.asList(excludeAttribute);
        Filter applicableFilter = new Filter("LifeStyle", "lifestyle", Arrays.asList("DT"), null);
        final List<String> includePromotions = Arrays.asList("promotion1", "promotion2");
        final List<String> excludePromotions = Arrays.asList("promotion3");
        SearchCriteriaOptions include = new SearchCriteriaOptions(includePromotions, includeHierarchyList, includeAttributes);
        SearchCriteriaOptions exclude = new SearchCriteriaOptions(excludePromotions, excludeHierarchyList, excludeAttributes);
        categoryDocument.setSearchCriteria(new SearchCriteria(include, exclude));
        ProductRefinements productRefinements = new ProductRefinements();
        productRefinements.setRegOnly(true);
        productRefinements.setPriceRange(new PriceRangeAtg("PROMO_PRICE", BigDecimal.valueOf(500.0), BigDecimal.valueOf(1000.5)));
        categoryDocument.setProductRefinements(productRefinements);
        categoryDocument.setCmosCatalogCodes(new ArrayList<>(Arrays.asList(new String[]{"NMF17"})));
        categoryDocument.setNewArrivalLimit(0);
        categoryDocument.setPreferredProductIds(new ArrayList<>(Arrays.asList(new String[]{"prod1234", "prod1235", "prod1236"})));
        categoryDocument.setImageAvailable(true);
        categoryDocument.setHideMobileImage(true);
        categoryDocument.setImageAvailableOverride(null);
        categoryDocument.setApplicableFilters(Arrays.asList(applicableFilter));
        categoryDocument.setAlternateSeoName("Name Override");
        categoryDocument.setCanonicalUrl("/categories/cat43810733/c.cat");
        categoryDocument.setSeoContentTitle("Alice + Olivia Clothing");
        categoryDocument.setSeoTags("<meta name=\"description\" content=\"Alice + Olivia shot to prominence with its line " +
                "of form-fitting pants in bright colors. Today, the label provides chic apparel and accessories, including " +
                "ready-to-wear, shoes, and handbags. Sixthday offers an extensive selection of Alice + Olivia fashion " +
                "including Alice + Olivia pants, tops, gowns, skirts, dresses, and more.\"></meta>");
        categoryDocument.setSeoTitleOverride("Alice + Olivia Clothing at Sixthday");
        categoryDocument.setSeoContentDescription("Alice + Olivia shot to prominence with its line of form-fitting pants" +
                " in bright colors. Today, the label provides chic apparel and accessories, including ready-to-wear, shoes, and handbags. " +
                "Sixthday offers an extensive selection of Alice + Olivia fashion including Alice + Olivia pants," +
                " tops, gowns, skirts, dresses, and more.");
        categoryDocument.setRedirectType("301");
        categoryDocument.setRedirectTo("cat123");
        categoryDocument.setDisplayAsGroups(true);
        categoryDocument.setDriveToGroupPDP(true);
        categoryDocument.setDefaultPath("cat000000_cat000001_cat43810733");

        List<String> emptyChildCategoryOrder = new ArrayList<>();
        String emptyDriveToSubCategoryId = "";
        categoryDocument.setThumbImageShot("z");

        ContextualProperty contextualProperty1 = new ContextualProperty(false, false, "T1C0001", "Women's Apparel", "", emptyDriveToSubCategoryId, null, null, null, emptyChildCategoryOrder);
        List<ContextualProperty> contextualPropertyList = Arrays.asList(contextualProperty1);
        categoryDocument.setContextualProperties(contextualPropertyList);
        categoryDocument.setExcludeFromPCS(false);
        categoryDocument.setTemplateType("P3");
    }
}
