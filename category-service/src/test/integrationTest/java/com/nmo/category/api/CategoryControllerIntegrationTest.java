package com.sixthday.category.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.sixthday.category.CategoryServiceApplication;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.elasticsearch.models.ContextualProperty;
import com.sixthday.navigation.api.elasticsearch.models.PriceRangeAtg;
import com.sixthday.navigation.api.elasticsearch.models.ProductRefinements;
import lombok.SneakyThrows;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.*;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.port;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CategoryServiceApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
public class CategoryControllerIntegrationTest {

    private static final String CATEGORY_INDEX = "category_index";
    private static final String CATEGORY_DOCUMENT = "_doc";
    private static final String CATEGORY_URL = "/category-service/{countryCode}/categories";
    private static final String CAT_ID_1 = "catId1";
    private static final String CAT_ID_2 = "catId2";
    private static final String CHILD_1 = "child1";
    private static final String CHILD_2 = "child2";
    private static final String DESKTOP_ALTERNATE_NAME = "DesktopAlternateName";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${local.server.port}")
    int serverPort;

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Before
    public void setUp() {
        port = serverPort;
        addCategoryDocumentWithBasicDetails();
    }

    @After
    @SneakyThrows
    public void tearDown() {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.add(new DeleteRequest(CATEGORY_INDEX, CategoryDocument.DOCUMENT_TYPE, CAT_ID_1));
        bulkRequest.add(new DeleteRequest(CATEGORY_INDEX, CategoryDocument.DOCUMENT_TYPE, CAT_ID_2));
        restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    @Test
    @SneakyThrows
    public void shouldServe200WhenCategoryDocumentIsPresent() {
        Map<String, String> categoryIds = new HashMap<>();
        categoryIds.put(CAT_ID_1, "parentCatId1");

        List<String> childrenOfCatId1 = new ArrayList<>();
        childrenOfCatId1.add(CHILD_1);
        childrenOfCatId1.add(CHILD_2);

        given().contentType("application/json")
                .when()
                .body(categoryIds, ObjectMapperType.JACKSON_2)
                .post(CATEGORY_URL, Collections.singletonList("US"))
                .then()
                .statusCode(200)
                .body("[0].id", is(CAT_ID_1))
                .body("[0].name", is(DESKTOP_ALTERNATE_NAME))
                .body("[0].templateType", is("X"))
                .body("[0].redirectUrl", is("http://redirect-url"))
                .body("[0].redirectType", is("301"))
                .body("[0].firstSellableProductImageUrl", containsString("some_first_sellable_product_image_url"))
                .body("[0].seoMetaTags", is("some seo tag"))
                .body("[0].seoContentTitle", is("some seo content title"))
                .body("[0].seoContentDescription", is("some seo content description"))
                .body("[0].canonicalUrl", is("/some_canonical_url/"))
                .body("[0].seoPageTitle", is("some seo title override"))
                .body("[0].excludedCountries", is(Collections.singletonList("IN")))
                .body("[0].boutique", is(true))
                .body("[0].driveToSubcategoryId", is("cat2:cat3"))
                .body("[0].children", is(childrenOfCatId1))
                .body("[0].results", is(true))
                .body("[0].dynamic", is(true))
                .body("[0].mobileHideEntrySubcats", is(true))
                .body("[0].hidden", is(true))
                .body("[0].children", is(childrenOfCatId1))
                .body("[0].catalogTrees", is(Arrays.asList("STAGE", "LIVE", "ENDECA", "MARKETING")));
    }

    @Test
    @SneakyThrows
    public void shouldServe404WhenCategoryTemplateTypeIsNotPresent() {
        Map<String, String> categoryIds = new HashMap<>();
        categoryIds.put(CAT_ID_2, "parentCatId2");

        given().contentType("application/json")
                .when()
                .body(categoryIds, ObjectMapperType.JACKSON_2)
                .post(CATEGORY_URL, "US")
                .then()
                .statusCode(404);
    }

    @SneakyThrows
    private void addCategoryDocumentWithBasicDetails() {
        BulkProcessor.Listener mock = mock(BulkProcessor.Listener.class);
        BulkProcessor bulkProcessor = BulkProcessor
                .builder((request, bulkListener) -> restHighLevelClient.bulkAsync(request, RequestOptions.DEFAULT, bulkListener), mock)
                .setBulkActions(2)
                .setConcurrentRequests(0)
                .build();

        CategoryDocument cat1CategoryDocument = CategoryDocument.builder()
                .id(CAT_ID_1)
                .name("catName1")
                .templateType("X0: Redirect Long Desc")
                .longDescription("long description")
                .redirectType("301")
                .redirectTo("http://redirect-url")
                .firstSellableProductImageUrl("/some_first_sellable_product_image_url/")
                .seoTags("some seo tag")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some_canonical_url/")
                .alternateSeoName("some alternate seo name")
                .seoTitleOverride("some seo title override")
                .children(Arrays.asList(CHILD_1, CHILD_2))
                .contextualProperties(Arrays.asList(new ContextualProperty("parentCatId1", DESKTOP_ALTERNATE_NAME,
                        "MobileAlternateName", Arrays.asList("childCategory1", "childCategory2"),
                        "cat2:cat3", false)))
                .excludedCountries(Collections.singletonList("IN"))
                .boutique(true)
                .boutiqueChild(false)
                .mobileHideEntrySubcats(true)
                .hidden(true)
                .deleted(false)
                .results(false)
                .type(Arrays.asList("STAGE", "LIVE", "ENDECA", "MARKETING"))
                .defaultPath("cat000000_catId1")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();

        CategoryDocument cat2CategoryDocument = CategoryDocument.builder()
                .id(CAT_ID_2)
                .name("catName2")
                .children(Arrays.asList(CHILD_1, CHILD_2))
                .contextualProperties(Arrays.asList(new ContextualProperty("parentCatId2", DESKTOP_ALTERNATE_NAME,
                        "MobileAlternateName", Arrays.asList("childCategory1", "childCategory2"),
                        "cat2:cat3", false)))
                .type(Arrays.asList("LIVE"))
                .defaultPath("cat000000_T1CAT39040731_catId2")
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .build();

        bulkProcessor.add(new IndexRequest(CATEGORY_INDEX, CATEGORY_DOCUMENT, CAT_ID_1)
                .source(objectMapper.writeValueAsString(cat1CategoryDocument), XContentType.JSON));
        bulkProcessor.add(new IndexRequest(CATEGORY_INDEX, CATEGORY_DOCUMENT, CAT_ID_2)
                .source(objectMapper.writeValueAsString(cat2CategoryDocument), XContentType.JSON));
        bulkProcessor.close();
        restHighLevelClient.indices().refresh(new RefreshRequest(CATEGORY_INDEX), RequestOptions.DEFAULT);
    }
}
