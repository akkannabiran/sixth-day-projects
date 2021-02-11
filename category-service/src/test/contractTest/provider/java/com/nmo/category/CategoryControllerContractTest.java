package com.sixthday.category;

import au.com.dius.pact.provider.junit.*;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit.target.TestTarget;
import au.com.dius.pact.provider.spring.target.MockMvcTarget;
import com.sixthday.category.api.controllers.CategoryController;
import com.sixthday.category.api.models.Category;
import com.sixthday.category.api.services.CategoryService;
import com.sixthday.category.config.CategoryServiceConfig;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.mockito.Mockito.when;

@RunWith(PactRunner.class)
@Provider("category-service")
@Consumer("ctp-svc")
@PactBroker(protocol = "${pactbroker.protocol}", host = "${pactbroker.hostname}", port = "${pactbroker.port}")
public class CategoryControllerContractTest {

    @TestTarget
    public MockMvcTarget target = new MockMvcTarget();

    @Mock
    private CategoryService categoryService;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        CategoryController categoryController = new CategoryController(categoryService);
        target.setControllers(categoryController);
    }

    @State("GET_CATEGORIES")
    public void shouldReturnListOfCategoriesAsRequested() {
        Map<String, String> categoryIds = new HashMap<>();
        categoryIds.put("catId1", "parentCatId1");
        categoryIds.put("catId2", "parentCatId2");
        when(categoryService.getCategories(categoryIds,Optional.empty(), Optional.empty())).thenReturn(getCategories());
    }

    private List<Category> getCategories() {
        List<String> childrenOfCatId1 = new ArrayList<>();
        childrenOfCatId1.add("child1");
        childrenOfCatId1.add("child2");

        List<String> childrenOfCatId2 = new ArrayList<>();
        childrenOfCatId2.add("child1");
        childrenOfCatId2.add("child2");

        List<Category> categories = new ArrayList<>();
        categories.add(Category.builder()
                .id("catId1")
                .name("catName1")
                .templateType("P")
                .redirectUrl("cat1RedirectUrl")
                .boutique(true)
                .excludeFromPCS(false)
                .mobileHideEntrySubcats(false)
                .redirectType("301")
                .firstSellableProductImageUrl("/some first sellable product image url/")
                .seoMetaTags("some seo meta tags")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some canonical url/")
                .seoPageTitle("some seo page title")
                .children(childrenOfCatId1)
                .driveToSubcategoryId("cat2")
                .excludedCountries(Collections.singletonList("IN"))
                .results(false)
                .dynamic(false)
                .hidden(true)
                .catalogTrees(Arrays.asList("tree1", "tree2"))
                .build());
        categories.add(Category.builder()
                .id("catId2")
                .name("catName2")
                .templateType("X")
                .redirectUrl("cat2RedirectUrl")
                .boutique(false)
                .excludeFromPCS(true)
                .redirectType("302")
                .firstSellableProductImageUrl("/some first sellable product image url/")
                .seoMetaTags("some seo meta tags")
                .seoContentTitle("some seo content title")
                .seoContentDescription("some seo content description")
                .canonicalUrl("/some canonical url/")
                .seoPageTitle("some seo page title")
                .mobileHideEntrySubcats(true)
                .children(childrenOfCatId2)
                .driveToSubcategoryId("cat2:cat3")
                .excludedCountries(Collections.singletonList("AU"))
                .results(true)
                .dynamic(true)
                .hidden(false)
                .catalogTrees(Arrays.asList("tree3"))
                .build());
        return categories;
    }
}
