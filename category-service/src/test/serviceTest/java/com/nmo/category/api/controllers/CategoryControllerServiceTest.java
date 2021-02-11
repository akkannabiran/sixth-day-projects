package com.sixthday.category.api.controllers;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.sixthday.category.config.CategoryServiceConfig;
import com.sixthday.category.elasticsearch.repository.CategoryRepository;
import com.sixthday.category.exceptions.UnknownCategoryTemplateTypeException;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.elasticsearch.models.ContextualProperty;
import com.sixthday.navigation.api.elasticsearch.models.PriceRangeAtg;
import com.sixthday.navigation.api.elasticsearch.models.ProductRefinements;
import com.sixthday.navigation.api.elasticsearch.models.SearchCriteriaOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.*;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.cloud.consul.enabled=false", "spring.cloud.vault.enabled=false"})
public class CategoryControllerServiceTest {

    private static final String IMAGES_sixthday_COM_SOME_FIRST_SELLABLE_PRODUCT_IMAGE_URL_JPG = "//images.sixthday.com/some_first_sellable_product_image_url.jpg";
    private static final String APPLICATION_JSON = "application/json";
    private static final String DESKTOP_ALTERNATE_NAME = "DesktopAlternateName";
    private static final String CHILD_1 = "child1";
    private static final String CHILD_2 = "child2";
    private static final String CHILD_3 = "child3";
    private static final String CHILD_4 = "child4";
    private static final String CHILD_5 = "child5";
    private static final String CHILD_6 = "child6";

    @Value("${local.server.port}")
    private int port;

    @MockBean
    private CategoryRepository categoryRepository;

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private CategoryServiceConfig categoryServiceConfig;

    @MockBean
    private RestHighLevelClient restHighLevelClient;

    private String CATEGORY_URL = "/category-service/{countryCode}/categories";

    @Before
    public void setup() {
        RestAssured.port = port;
        when(categoryServiceConfig.getImageServerUrl()).thenReturn("//images.sixthday.com");
        when(categoryServiceConfig.getDefaultProductImageSrc()).thenReturn("/assets/images/no-image.c9a49578722aabed021ab4821bf0e705.jpeg");
        when(categoryServiceConfig.getThresholdLimitValue()).thenReturn(120);
    }

    @Test
    public void shouldServeListOfCategoriesWhenAllOfThemAvailableInElasticSearch() {
        Map<String, String> categoryIds = new HashMap<>();
        categoryIds.put("catId1", "parentCatId1");
        categoryIds.put("catId2", "parentCatId2");

        List<String> childrenOfCatId1 = Arrays.asList(CHILD_1, CHILD_2);
        List<String> childrenOfCatId2 = Arrays.asList(CHILD_3, CHILD_4);

        Map<String, String> hierarchyMap = new HashMap<>();
        hierarchyMap.put("level1","Accessories");
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("Material", Arrays.asList("Fur","Leather"));

        Map<String, String> excludeHierarchyMap = new HashMap<>();
        hierarchyMap.put("level1","Hats");
        Map<String, List<String>> excludeAttributes = new HashMap<>();
        attributes.put("Season", Arrays.asList("Fall"));

        CategoryDocument categoryDocument1 = CategoryDocument.builder()
                .id("catId1")
                .name("catName1")
                .templateType("X0")
                .longDescription("http://some-catId1-redirect-url")
                .redirectType("302")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url.jpg")
                .seoTags("some seo tag")
                .boutique(true)
                .boutiqueChild(false)
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .alternateSeoName("some alternate seo name")
                .seoTitleOverride("some seo title override")
                .children(childrenOfCatId1)
                .contextualProperties(Arrays.asList(new ContextualProperty("parentCatId1", DESKTOP_ALTERNATE_NAME,
                        "MobileAlternateName", Arrays.asList("childCategory1", "childCategory2"),
                        "", false)))
                .searchCriteria(com.sixthday.navigation.api.elasticsearch.models.SearchCriteria.builder()
                        .include(SearchCriteriaOptions.builder()
                                .attributes(Arrays.asList(attributes))
                                .hierarchy(Arrays.asList(hierarchyMap))
                                .promotions(Collections.singletonList("promotions"))
                                .build())
                        .exclude(SearchCriteriaOptions.builder()
                                .attributes(Arrays.asList(excludeAttributes))
                                .hierarchy(Arrays.asList(excludeHierarchyMap))
                                .promotions(Collections.singletonList("excludePromotions")).build())
                        .build())
                .results(true)
                .mobileHideEntrySubcats(true)
                .hidden(true)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();

        CategoryDocument categoryDocument2 = CategoryDocument.builder()
                .id("catId2")
                .name("catName2")
                .templateType("P3")
                .longDescription("http://some-catId2-redirect-url")
                .redirectTo("http://some-catId1")
                .redirectType("301")
                .boutique(false)
                .boutiqueChild(false)
                .excludeFromPCS(true)
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url.jpg")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .alternateSeoName("some alternate seo name")
                .canonicalUrl("/some_canonical_url/")
                .children(childrenOfCatId2)
                .contextualProperties(Arrays.asList(new ContextualProperty("parentCatId2", DESKTOP_ALTERNATE_NAME,
                        "MobileAlternateName", Arrays.asList("childCategory1", "childCategory2"), "", false)))
                .results(false)
                .mobileHideEntrySubcats(false)
                .hidden(false)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();

        List<CategoryDocument> categoryDocuments = Arrays.asList(categoryDocument1, categoryDocument2);

        CategoryServiceConfig.CategoryTemplate categoryTemplateX = new CategoryServiceConfig.CategoryTemplate();
        categoryTemplateX.setName("X0");
        categoryTemplateX.setKey("X");

        CategoryServiceConfig.CategoryTemplate categoryTemplateP = new CategoryServiceConfig.CategoryTemplate();
        categoryTemplateP.setName("P3");
        categoryTemplateP.setKey("P");

        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Arrays.asList(categoryTemplateX, categoryTemplateP));
        when(categoryRepository.getCategoryDocuments(anySetOf(String.class))).thenReturn(categoryDocuments);
        when(categoryServiceConfig.getSeoContentTitle()).thenReturn("at Sixthday");

        given()
                .contentType(APPLICATION_JSON)
                .when()
                .body(categoryIds, ObjectMapperType.JACKSON_2)
                .post(CATEGORY_URL, "US")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].id", is("catId1"))
                .body("[0].name", is(DESKTOP_ALTERNATE_NAME))
                .body("[0].templateType", is("X"))
                .body("[0].redirectUrl", is("http://some-catId1-redirect-url"))
                .body("[0].redirectType", is("302"))
                .body("[0].firstSellableProductImageUrl", is(IMAGES_sixthday_COM_SOME_FIRST_SELLABLE_PRODUCT_IMAGE_URL_JPG))
                .body("[0].seoMetaTags", is("some seo tag"))
                .body("[0].boutique", is(true))
                .body("[0].seoContentTitle", is("some seo content title"))
                .body("[0].seoContentDescription", is("some seo content description"))
                .body("[0].seoPageTitle", is("some seo title override"))
                .body("[0].canonicalUrl", is("/some_canonical_url/"))
                .body("[0].children", is(childrenOfCatId1))
                .body("[0].driveToSubcategoryId", is(""))
                .body("[0].results", is(false))
                .body("[0].dynamic", is(false))
                .body("[0].mobileHideEntrySubcats", is(true))
                .body("[0].hidden", is(true))
                .body("[1].id", is("catId2"))
                .body("[1].name", is(DESKTOP_ALTERNATE_NAME))
                .body("[1].templateType", is("P"))
                .body("[1].redirectUrl", is("http://some-catId1"))
                .body("[1].redirectType", is("301"))
                .body("[1].excludeFromPCS", is(true))
                .body("[1].firstSellableProductImageUrl", is(IMAGES_sixthday_COM_SOME_FIRST_SELLABLE_PRODUCT_IMAGE_URL_JPG))
                .body("[1].seoMetaTags", is("some seo tag"))
                .body("[1].boutique", is(false))
                .body("[1].seoContentTitle", is("some seo content title"))
                .body("[1].seoContentDescription", is("some seo content description"))
                .body("[1].seoPageTitle", is("some alternate seo name in parent category at Sixthday"))
                .body("[1].canonicalUrl", is("/some_canonical_url/"))
                .body("[1].driveToSubcategoryId", is(""))
                .body("[1].children", is(childrenOfCatId2))
                .body("[1].results", is(true))
                .body("[1].dynamic", is(true))
                .body("[1].mobileHideEntrySubcats", is(false))
                .body("[1].hidden", is(false));
    }

    @Test
    public void shouldServeListOfCategoriesWithNewAspectRatioTrueWhenMatchingCategoryIdMatchFromConfiguration() {
        Map<String, String> categoryIds = new HashMap<>();
        categoryIds.put("cat42960827", "cat000000_cat000001_cat17740747_cat42960827");

        List<String> childrenOfCatId1 = Arrays.asList(CHILD_1, CHILD_2);

        Map<String, String> hierarchyMap = new HashMap<>();
        hierarchyMap.put("level1","Accessories");
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("Material", Arrays.asList("Fur","Leather"));

        Map<String, String> excludeHierarchyMap = new HashMap<>();
        hierarchyMap.put("level1","Hats");
        Map<String, List<String>> excludeAttributes = new HashMap<>();
        attributes.put("Season", Arrays.asList("Fall"));


        CategoryDocument categoryDocument1 = CategoryDocument.builder()
                .id("cat42960827")
                .name("catName1")
                .templateType("X0")
                .longDescription("http://some-catId1-redirect-url")
                .redirectType("302")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url.jpg")
                .seoTags("some seo tag")
                .boutique(true)
                .boutiqueChild(false)
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .alternateSeoName("some alternate seo name")
                .seoTitleOverride("some seo title override")
                .children(childrenOfCatId1)
                .contextualProperties(Arrays.asList(new ContextualProperty("parentCatId1", DESKTOP_ALTERNATE_NAME,
                        "MobileAlternateName", Arrays.asList("childCategory1", "childCategory2"),
                        "", false)))
                .searchCriteria(com.sixthday.navigation.api.elasticsearch.models.SearchCriteria.builder()
                        .include(SearchCriteriaOptions.builder()
                                .attributes(Arrays.asList(attributes))
                                .hierarchy(Arrays.asList(hierarchyMap))
                                .promotions(Collections.singletonList("promotions"))
                                .build())
                        .exclude(SearchCriteriaOptions.builder()
                                .attributes(Arrays.asList(excludeAttributes))
                                .hierarchy(Arrays.asList(excludeHierarchyMap))
                                .promotions(Collections.singletonList("excludePromotions")).build())
                        .build())
                .results(true)
                .mobileHideEntrySubcats(true)
                .hidden(true)
                .defaultPath("cat000000_cat000001_cat17740747_cat42960827")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();

        List<CategoryDocument> categoryDocuments = Arrays.asList(categoryDocument1);

        CategoryServiceConfig.CategoryTemplate categoryTemplateX = new CategoryServiceConfig.CategoryTemplate();
        categoryTemplateX.setName("X0");
        categoryTemplateX.setKey("X");

        CategoryServiceConfig.CategoryTemplate categoryTemplateP = new CategoryServiceConfig.CategoryTemplate();
        categoryTemplateP.setName("P3");
        categoryTemplateP.setKey("P");

        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Arrays.asList(categoryTemplateX, categoryTemplateP));
        when(categoryServiceConfig.getNewAspectRatioCategoryIdList()).thenReturn(Arrays.asList("cat42960827"));
        when(categoryRepository.getCategoryDocuments(anySetOf(String.class))).thenReturn(categoryDocuments);

        given()
                .contentType(APPLICATION_JSON)
                .when()
                .body(categoryIds, ObjectMapperType.JACKSON_2)
                .post(CATEGORY_URL, "US")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].newAspectRatio", is(true));
    }

    @Test
    public void shouldServeListOfCategoriesWithNewAspectRatioTrueWhenAnyParentInCategoryPathMatchFromConfiguration() {
        Map<String, String> categoryIds = new HashMap<>();
        categoryIds.put("cat42960827", "cat000000_cat000001_cat17740747_cat42960827");

        List<String> childrenOfCatId1 = Arrays.asList(CHILD_1, CHILD_2);

        Map<String, String> hierarchyMap = new HashMap<>();
        hierarchyMap.put("level1","Accessories");
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("Material", Arrays.asList("Fur","Leather"));

        Map<String, String> excludeHierarchyMap = new HashMap<>();
        hierarchyMap.put("level1","Hats");
        Map<String, List<String>> excludeAttributes = new HashMap<>();
        attributes.put("Season", Arrays.asList("Fall"));


        CategoryDocument categoryDocument1 = CategoryDocument.builder()
                .id("cat42960827")
                .name("catName1")
                .templateType("X0")
                .longDescription("http://some-catId1-redirect-url")
                .redirectType("302")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url.jpg")
                .seoTags("some seo tag")
                .boutique(true)
                .boutiqueChild(false)
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .alternateSeoName("some alternate seo name")
                .seoTitleOverride("some seo title override")
                .children(childrenOfCatId1)
                .contextualProperties(Arrays.asList(new ContextualProperty("parentCatId1", DESKTOP_ALTERNATE_NAME,
                        "MobileAlternateName", Arrays.asList("childCategory1", "childCategory2"),
                        "", false)))
                .searchCriteria(com.sixthday.navigation.api.elasticsearch.models.SearchCriteria.builder()
                        .include(SearchCriteriaOptions.builder()
                                .attributes(Arrays.asList(attributes))
                                .hierarchy(Arrays.asList(hierarchyMap))
                                .promotions(Collections.singletonList("promotions"))
                                .build())
                        .exclude(SearchCriteriaOptions.builder()
                                .attributes(Arrays.asList(excludeAttributes))
                                .hierarchy(Arrays.asList(excludeHierarchyMap))
                                .promotions(Collections.singletonList("excludePromotions")).build())
                        .build())
                .results(true)
                .mobileHideEntrySubcats(true)
                .hidden(true)
                .defaultPath("cat000000_cat000001_cat17740747_cat42960827")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();

        List<CategoryDocument> categoryDocuments = Arrays.asList(categoryDocument1);

        CategoryServiceConfig.CategoryTemplate categoryTemplateX = new CategoryServiceConfig.CategoryTemplate();
        categoryTemplateX.setName("X0");
        categoryTemplateX.setKey("X");

        CategoryServiceConfig.CategoryTemplate categoryTemplateP = new CategoryServiceConfig.CategoryTemplate();
        categoryTemplateP.setName("P3");
        categoryTemplateP.setKey("P");

        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Arrays.asList(categoryTemplateX, categoryTemplateP));
        when(categoryServiceConfig.getNewAspectRatioCategoryIdList()).thenReturn(Arrays.asList("cat000001"));
        when(categoryRepository.getCategoryDocuments(anySetOf(String.class))).thenReturn(categoryDocuments);

        given()
                .contentType(APPLICATION_JSON)
                .when()
                .body(categoryIds, ObjectMapperType.JACKSON_2)
                .post(CATEGORY_URL, "US")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].newAspectRatio", is(true));
    }

    @Test
    public void shouldServeListOfCategoriesWithNewAspectRatioTrueWhenCategoryPathNotProvidedInRequest() {
        Map<String, String> categoryIds = new HashMap<>();
        categoryIds.put("cat42960827", "");

        List<String> childrenOfCatId1 = Arrays.asList(CHILD_1, CHILD_2);

        Map<String, String> hierarchyMap = new HashMap<>();
        hierarchyMap.put("level1","Accessories");
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("Material", Arrays.asList("Fur","Leather"));

        Map<String, String> excludeHierarchyMap = new HashMap<>();
        hierarchyMap.put("level1","Hats");
        Map<String, List<String>> excludeAttributes = new HashMap<>();
        attributes.put("Season", Arrays.asList("Fall"));


        CategoryDocument categoryDocument1 = CategoryDocument.builder()
                .id("cat42960827")
                .name("catName1")
                .templateType("X0")
                .longDescription("http://some-catId1-redirect-url")
                .redirectType("302")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url.jpg")
                .seoTags("some seo tag")
                .boutique(true)
                .boutiqueChild(false)
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .alternateSeoName("some alternate seo name")
                .seoTitleOverride("some seo title override")
                .children(childrenOfCatId1)
                .contextualProperties(Arrays.asList(new ContextualProperty("parentCatId1", DESKTOP_ALTERNATE_NAME,
                        "MobileAlternateName", Arrays.asList("childCategory1", "childCategory2"),
                        "", false)))
                .searchCriteria(com.sixthday.navigation.api.elasticsearch.models.SearchCriteria.builder()
                        .include(SearchCriteriaOptions.builder()
                                .attributes(Arrays.asList(attributes))
                                .hierarchy(Arrays.asList(hierarchyMap))
                                .promotions(Collections.singletonList("promotions"))
                                .build())
                        .exclude(SearchCriteriaOptions.builder()
                                .attributes(Arrays.asList(excludeAttributes))
                                .hierarchy(Arrays.asList(excludeHierarchyMap))
                                .promotions(Collections.singletonList("excludePromotions")).build())
                        .build())
                .results(true)
                .mobileHideEntrySubcats(true)
                .hidden(true)
                .defaultPath("cat000000_cat000001_cat17740747_cat42960827")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();

        List<CategoryDocument> categoryDocuments = Arrays.asList(categoryDocument1);

        CategoryServiceConfig.CategoryTemplate categoryTemplateX = new CategoryServiceConfig.CategoryTemplate();
        categoryTemplateX.setName("X0");
        categoryTemplateX.setKey("X");

        CategoryServiceConfig.CategoryTemplate categoryTemplateP = new CategoryServiceConfig.CategoryTemplate();
        categoryTemplateP.setName("P3");
        categoryTemplateP.setKey("P");

        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Arrays.asList(categoryTemplateX, categoryTemplateP));
        when(categoryServiceConfig.getNewAspectRatioCategoryIdList()).thenReturn(Arrays.asList("cat42960827"));
        when(categoryRepository.getCategoryDocuments(anySetOf(String.class))).thenReturn(categoryDocuments);

        given()
                .contentType(APPLICATION_JSON)
                .when()
                .body(categoryIds, ObjectMapperType.JACKSON_2)
                .post(CATEGORY_URL, "US")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].newAspectRatio", is(true));
    }

    @Test
    public void shouldServeListOfAvailableCategoriesWhenFewIdsNotAvailableInElasticSearch() {
        Map<String, String> categoryIds = new HashMap<>();
        categoryIds.put("catId1", "parentCatId1");
        categoryIds.put("catId2", "parentCatId2");
        categoryIds.put("catId3", "parentCatId3");
        categoryIds.put("catId4", "parentCatId4");

        String driveToSubcategoryId = "cat2:cat3";

        List<String> childrenOfCatId1 = Arrays.asList(CHILD_1, CHILD_2);
        List<String> childrenOfCatId2 = Arrays.asList(CHILD_3, CHILD_4);
        List<String> childrenOfCatId4 = Arrays.asList(CHILD_5, CHILD_6);

        CategoryDocument categoryDocument1 = CategoryDocument.builder()
                .id("catId1")
                .name("catName1")
                .templateType("X0")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url.jpg")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .seoTitleOverride("some seo title override")
                .canonicalUrl("/some_canonical_url/")
                .children(childrenOfCatId1)
                .contextualProperties(Arrays.asList(new ContextualProperty("parentCatId1", DESKTOP_ALTERNATE_NAME,
                        "MobileAlternateName", Arrays.asList("childCategory1", "childCategory2"),
                        driveToSubcategoryId, false)))
                .boutiqueChild(false)
                .boutique(false)
                .mobileHideEntrySubcats(true)
                .hidden(true)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();

        CategoryDocument categoryDocument2 = CategoryDocument.builder()
                .id("catId2")
                .name("catName2")
                .templateType("P3")
                .longDescription("http://some-catId2-redirect-url")
                .redirectTo("http://redirect-url")
                .redirectType("301")
                .excludeFromPCS(false)
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url.jpg")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .alternateSeoName("some alternate seo name")
                .canonicalUrl("/some_canonical_url/")
                .children(childrenOfCatId2)
                .contextualProperties(Arrays.asList(new ContextualProperty("parentCatId2", DESKTOP_ALTERNATE_NAME,
                        "MobileAlternateName", Arrays.asList("childCategory1", "childCategory2"),
                        driveToSubcategoryId, false)))
                .boutiqueChild(true)
                .boutique(false)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();

        CategoryDocument categoryDocument4 = CategoryDocument.builder()
                .id("catId4")
                .name("catName4")
                .templateType("P3")
                .longDescription("http://some-catId4-redirect-url")
                .redirectType("302")
                .excludeFromPCS(false)
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .children(childrenOfCatId4)
                .contextualProperties(Arrays.asList(new ContextualProperty("parentCatId4", DESKTOP_ALTERNATE_NAME,
                        "MobileAlternateName", Arrays.asList("childCategory1", "childCategory2"),
                        driveToSubcategoryId, false)))
                .boutiqueChild(false)
                .boutique(true)
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();

        List<CategoryDocument> categoryDocuments = Arrays.asList(categoryDocument1, categoryDocument2, categoryDocument4);

        CategoryServiceConfig.CategoryTemplate categoryTemplateX = new CategoryServiceConfig.CategoryTemplate();
        categoryTemplateX.setName("X0");
        categoryTemplateX.setKey("X");

        CategoryServiceConfig.CategoryTemplate categoryTemplateP2 = new CategoryServiceConfig.CategoryTemplate();
        categoryTemplateP2.setName("P3");
        categoryTemplateP2.setKey("P");

        CategoryServiceConfig.CategoryTemplate categoryTemplateP4 = new CategoryServiceConfig.CategoryTemplate();
        categoryTemplateP2.setName("P3");
        categoryTemplateP2.setKey("P");

        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Arrays.asList(categoryTemplateX, categoryTemplateP2, categoryTemplateP4));
        when(categoryRepository.getCategoryDocuments(anySetOf(String.class))).thenReturn(categoryDocuments);
        when(categoryServiceConfig.getSeoContentTitle()).thenReturn("at Sixthday");

        given()
                .contentType(APPLICATION_JSON)
                .when()
                .body(categoryIds, ObjectMapperType.JACKSON_2)
                .post(CATEGORY_URL, Collections.singletonList("US"))
                .then()
                .statusCode(200)
                .body("size()", is(3))
                .body("[0].id", is("catId1"))
                .body("[0].name", is(DESKTOP_ALTERNATE_NAME))
                .body("[0].templateType", is("X"))
                .body("[0].redirectUrl", equalTo(null))
                .body("[0].redirectType", equalTo(null))
                .body("[0].firstSellableProductImageUrl", is(IMAGES_sixthday_COM_SOME_FIRST_SELLABLE_PRODUCT_IMAGE_URL_JPG))
                .body("[0].seoMetaTags", is("some seo tag"))
                .body("[0].seoContentTitle", is("some seo content title"))
                .body("[0].seoContentDescription", is("some seo content description"))
                .body("[0].canonicalUrl", is("/some_canonical_url/"))
                .body("[0].seoPageTitle", is("some seo title override"))
                .body("[0].children", is(childrenOfCatId1))
                .body("[0].boutique", is(false))
                .body("[0].driveToSubcategoryId", is(driveToSubcategoryId))
                .body("[0].mobileHideEntrySubcats", is(true))
                .body("[0].hidden", is(true))

                .body("[1].id", is("catId2"))
                .body("[1].name", is(DESKTOP_ALTERNATE_NAME))
                .body("[1].templateType", is("P"))
                .body("[1].redirectUrl", is("http://redirect-url"))
                .body("[1].redirectType", is("301"))
                .body("[1].excludeFromPCS", is(false))
                .body("[1].firstSellableProductImageUrl", is(IMAGES_sixthday_COM_SOME_FIRST_SELLABLE_PRODUCT_IMAGE_URL_JPG))
                .body("[1].seoMetaTags", is("some seo tag"))
                .body("[1].seoContentTitle", is("some seo content title"))
                .body("[1].seoContentDescription", is("some seo content description"))
                .body("[1].canonicalUrl", is("/some_canonical_url/"))
                .body("[1].seoPageTitle", is("some alternate seo name in parent category at Sixthday"))
                .body("[1].children", is(childrenOfCatId2))
                .body("[1].boutique", is(true))
                .body("[1].mobileHideEntrySubcats", is(false))
                .body("[1].driveToSubcategoryId", is(driveToSubcategoryId))

                .body("[2].id", is("catId4"))
                .body("[2].name", is(DESKTOP_ALTERNATE_NAME))
                .body("[2].templateType", is("P"))
                .body("[2].redirectUrl", is("http://some-catId4-redirect-url"))
                .body("[2].redirectType", is("302"))
                .body("[2].excludeFromPCS", is(false))
                .body("[2].firstSellableProductImageUrl", is("/assets/images/no-image.c9a49578722aabed021ab4821bf0e705.jpeg"))
                .body("[2].seoMetaTags", is("some seo tag"))
                .body("[2].seoContentTitle", is("some seo content title"))
                .body("[2].seoContentDescription", is("some seo content description"))
                .body("[2].canonicalUrl", is("/some_canonical_url/"))
                .body("[2].seoPageTitle", is("DesktopAlternateName in parent category at Sixthday"))
                .body("[2].boutique", is(true))
                .body("[2].driveToSubcategoryId", is(driveToSubcategoryId))
                .body("[2].mobileHideEntrySubcats", is(false))
                .body("[2].children", is(childrenOfCatId4));
    }

    @Test
    public void shouldServe404WhenUnsupportedTemplateTypeFound() {
        Map<String, String> categoryIds = new HashMap<>();
        categoryIds.put("catId1", "parentCatId1");
        categoryIds.put("catId2", "parentCatId2");

        CategoryServiceConfig.CategoryTemplate unsupportedCategoryTemplate = new CategoryServiceConfig.CategoryTemplate();
        unsupportedCategoryTemplate.setName("some Category Template Type");
        unsupportedCategoryTemplate.setKey("P");

        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(unsupportedCategoryTemplate));
        when(categoryRepository.getCategoryDocuments(anySetOf(String.class))).thenThrow(UnknownCategoryTemplateTypeException.class);

        given()
                .contentType(APPLICATION_JSON)
                .when()
                .body(categoryIds, ObjectMapperType.JACKSON_2)
                .post(CATEGORY_URL, "US")
                .then()
                .statusCode(404);
    }

    @Test
    public void shouldServeListOfCategoriesWithExcludedCountries() {
        Map<String, String> categoryIds = new HashMap<>();
        categoryIds.put("catId1", "parentCatId1");
        //List<String> categoryIdList = new ArrayList<>(categoryIds.keySet());

        CategoryDocument categoryDocument1 = CategoryDocument.builder()
                .id("catId1")
                .name("catName1")
                .templateType("P3")
                .excludedCountries(Arrays.asList("US", "UZ", "CA"))
                .defaultPath("cat000000_T1CAT39040731_myCat")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();

        List<CategoryDocument> categoryDocuments = Arrays.asList(categoryDocument1);

        CategoryServiceConfig.CategoryTemplate categoryTemplateP = new CategoryServiceConfig.CategoryTemplate();
        categoryTemplateP.setName("P3");
        categoryTemplateP.setKey("P");

        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Arrays.asList(categoryTemplateP));
        when(categoryRepository.getCategoryDocuments(anySetOf(String.class))).thenReturn(categoryDocuments);

        given()
                .contentType(APPLICATION_JSON)
                .when()
                .body(categoryIds, ObjectMapperType.JACKSON_2)
                .post(CATEGORY_URL, "US")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].excludedCountries", notNullValue())
                .body("[0].excludedCountries.size()", is(3))
                .body("[0].excludedCountries[0]", is("US"))
                .body("[0].excludedCountries[1]", is("UZ"))
                .body("[0].excludedCountries[2]", is("CA"));
    }

    @Test
    public void shouldServeEmptyListWhenNoneOfTheCategoriesAreValid() {
        Map<String, String> categoryIds = new HashMap<>();
        categoryIds.put("catId1", "parentCatId1");
        categoryIds.put("catId2", "parentCatId2");

        CategoryServiceConfig.CategoryTemplate categoryTemplate = new CategoryServiceConfig.CategoryTemplate();
        categoryTemplate.setName("some Category Template Type");
        categoryTemplate.setKey("SC");

        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(categoryTemplate));
        when(categoryRepository.getCategoryDocuments(anySetOf(String.class))).thenReturn(new ArrayList<>());

        given()
                .contentType(APPLICATION_JSON)
                .when()
                .body(categoryIds, ObjectMapperType.JACKSON_2)
                .post(CATEGORY_URL, "US")
                .then()
                .body("size()", is(0))
                .statusCode(200);
    }

    @Test
    public void shouldServe404WhenGenericExceptionOccurs() {
        Map<String, String> categoryIds = new HashMap<>();
        categoryIds.put("catId1", "parentCatId1");
        categoryIds.put("catId2", "parentCatId2");

        CategoryServiceConfig.CategoryTemplate categoryTemplate = new CategoryServiceConfig.CategoryTemplate();
        categoryTemplate.setName("some Category Template Type");
        categoryTemplate.setKey("SC");

        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(categoryTemplate));
        when(categoryRepository.getCategoryDocuments(anySetOf(String.class))).thenThrow(Exception.class);

        given()
                .contentType(APPLICATION_JSON)
                .when()
                .body(categoryIds, ObjectMapperType.JACKSON_2)
                .post(CATEGORY_URL, "US")
                .then()
                .statusCode(404);
    }

    @Test
    public void shouldServe404WhenRequestIsMissingCategoryIds() {
        CategoryServiceConfig.CategoryTemplate categoryTemplate = new CategoryServiceConfig.CategoryTemplate();
        categoryTemplate.setName("some Category Template Type");
        categoryTemplate.setKey("P");

        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(categoryTemplate));

        given()
                .contentType(APPLICATION_JSON)
                .when()
                .post(CATEGORY_URL, "US")
                .then()
                .statusCode(404);
    }

    @Test
    public void shouldServe200WhenRequestHasEmptyCategoryIdMap() {
        Map<String, String> categoryIds = new HashMap<>();

        CategoryServiceConfig.CategoryTemplate categoryTemplate = new CategoryServiceConfig.CategoryTemplate();
        categoryTemplate.setName("some Category Template Type");
        categoryTemplate.setKey("P");

        when(categoryServiceConfig.getCategoryTemplates()).thenReturn(Collections.singletonList(categoryTemplate));

        given()
                .contentType(APPLICATION_JSON)
                .when()
                .body(categoryIds, ObjectMapperType.JACKSON_2)
                .post(CATEGORY_URL, "US")
                .then()
                .body("size()", is(0))
                .statusCode(200);
    }
}