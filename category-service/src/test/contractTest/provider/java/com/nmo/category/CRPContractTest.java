package com.sixthday.category;

import au.com.dius.pact.provider.junit.Consumer;
import au.com.dius.pact.provider.junit.PactRunner;
import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit.target.TestTarget;
import au.com.dius.pact.provider.spring.target.MockMvcTarget;
import com.sixthday.category.api.controllers.CategoryController;
import com.sixthday.category.api.models.*;
import com.sixthday.category.api.services.CategoryService;
import com.sixthday.navigation.api.elasticsearch.models.SearchCriteriaOptions;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Mockito.when;

@RunWith(PactRunner.class)
@Provider("category-service")
@Consumer("crp-svc")
@PactBroker(protocol = "${pactbroker.protocol}", host = "${pactbroker.hostname}", port = "${pactbroker.port}")
public class CRPContractTest {

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
        when(categoryService.getCategories(categoryIds, Optional.empty(), Optional.empty())).thenReturn(getCategories());
    }

    private List<Category> getCategories() {
        List<String> childrenOfCatId1 = new ArrayList<>();
        childrenOfCatId1.add("child1");
        childrenOfCatId1.add("child2");

        List<String> childrenOfCatId2 = new ArrayList<>();
        childrenOfCatId2.add("child1");
        childrenOfCatId2.add("child2");

        Map<String, String> includeHierarchy = new HashMap<>(2);
        includeHierarchy.put("level1", "Women's Apparel");
        includeHierarchy.put("level2", "Tops");

        List<Map<String, String>> includeHierarchyList = Arrays.asList(includeHierarchy);
        List<Map<String, List<String>>> includeAttributes = new ArrayList<>();

        Map<String, List<String>> excludeAttribute = new HashMap<>(1);
        excludeAttribute.put("Proportion Size", new ArrayList<>(Arrays.asList(new String[]{"Womens Apparel","Petite Apparel"})));

        List<Map<String, String>> excludeHierarchyList = new ArrayList<>();
        List<Map<String, List<String>>> excludeAttributes = Arrays.asList(excludeAttribute);

        List<String> nullValues = new ArrayList<>();
        nullValues.add(null);
        Filter designerFilter = new Filter("Designer", "Designer", Collections.emptyList(), nullValues);
        Filter sixeFilter = new Filter("Size", "Size", Collections.emptyList(), nullValues);
        Filter colorFilter = new Filter("Color", "Color", Collections.emptyList(),  nullValues);
        Filter priceFilter = new Filter("priceBandUpper", "Price", Collections.emptyList(),  nullValues);
        Filter sleeveLengthFilter = new Filter("Sleeve Length", "Sleeve Length", Collections.emptyList(), Arrays.asList("Sleeveless",
                "Ham Sammich",
                "One Shoulder",
                "Long Sleeve",
                "Strapless",
                "Short Sleeve",
                "3/4 Sleeve",
                "Off the Shoulder",
                "Cold Shoulder"));
        Filter styleFilter = new Filter("Silhouette - Top", "Style", Collections.emptyList(), Arrays.asList( "Blouse",
                "Camisole",
                "Collared/Button Down",
                "Cropped",
                "Kim1",
                "T-Shirt",
                "Tunic",
                "Button Down",
                "MyNewValue",
                "Testval",
                "Camisole/Tank",
                "T-shirt/Knit",
                "One/Off/Cold Shoulder",
                "Sweatshirts/Hoodies"));
        Filter necklineFilter = new Filter("Neckline", "Neckline", Collections.emptyList(), Arrays.asList( "Cowl",
                "Crew",
                "Halter",
                "Scoop/Boat",
                "Turtleneck",
                "V-Neck",
                "Zip-up",
                "Collared",
                "Straight",
                "High-Neck",
                "Asymmetric",
                "Sweetheart",
                "Scoop",
                "High-Neck/Boat",
                "One/Off Shoulder",
                "Strapless/Spaghetti",
                "Cardigan/Zip/Open",
                "Square"));
        Filter inStoreFilter = new Filter("In Store", "In Store", Collections.emptyList(), nullValues);
        Filter availabilityFilter = new Filter("Combined Stock Status", "Availability", Arrays.asList( "ON ORDER"),  nullValues);
        final List<String> includePromotions = new ArrayList<>();
        final List<String> excludePromotions = new ArrayList<>();
        SearchCriteriaOptions include = new SearchCriteriaOptions(includePromotions, includeHierarchyList, includeAttributes);
        SearchCriteriaOptions exclude = new SearchCriteriaOptions(excludePromotions, excludeHierarchyList, excludeAttributes);

        PriceRange priceRange = new PriceRange();
        priceRange.setOption(PriceRange.PriceRangeType.OFF);
        priceRange.setMax(BigDecimal.ZERO);
        priceRange.setMin(BigDecimal.ZERO);

        SearchCriteriaResponse searchCriteriaResponse = new SearchCriteriaResponse();
        searchCriteriaResponse.setProductClassType("INCLUDE_ALL");
        searchCriteriaResponse.setIncludeUnsellable(false);
        searchCriteriaResponse.setInclude(include);
        searchCriteriaResponse.setExclude(exclude);
        searchCriteriaResponse.setPriceRange(priceRange);
        searchCriteriaResponse.setCatalogIds(Arrays.asList("NMF17"));

        CategoryHeaderAsset categoryHeaderAsset = new CategoryHeaderAsset();
        categoryHeaderAsset.setCategoryAssetUrl(null);
        categoryHeaderAsset.setHideOsixthdaybile(false);

        SortOption priceHighToLow = new SortOption(SortOption.Option.PRICE_HIGH_TO_LOW, false);
        SortOption priceLowToHigh = new SortOption(SortOption.Option.PRICE_LOW_TO_HIGH, false);
        SortOption newestFirst = new SortOption(SortOption.Option.NEWEST_FIRST, false);
        SortOption bestMatch = new SortOption(SortOption.Option.BEST_MATCH, true);
        SortOption myFavorites = new SortOption(SortOption.Option.MY_FAVORITES, false);

        SeoDetails seoDetails = new SeoDetails(null,"/c/womens-clothing-tops-cat42960827",null,
                "<meta name=\"description\" content=\"Take a look at women's shirts & blouses that make an array of fashion statements. Free shipping & free returns on women's designer tops at sixthday.com.\" />",
                "Women's Designer Tops, Shirts & Blouses at Sixthday", null, new RedirectDetails(302,"catRedirect"));

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
                .searchCriteria(searchCriteriaResponse)
                .newArrivalLimit(0)
                .preferredProductIds(Collections.emptyList())
                .categoryHeaderAsset(categoryHeaderAsset)
                .applicableFilters(Arrays.asList(designerFilter,sixeFilter,colorFilter,priceFilter,sleeveLengthFilter,styleFilter,necklineFilter,inStoreFilter,availabilityFilter))
                .seo(seoDetails)
                .defaultSortOption("BEST_MATCH")
                .availableSortOptions(Arrays.asList(priceHighToLow,priceLowToHigh,newestFirst,bestMatch,myFavorites))
                .pcsEnabled(true)
                .imageTemplateType(null)
                .displayAsGroups(false)
                .driveToGroupPDP(true)
                .reducedChildCount(false)
                .newAspectRatio(false)
                .defaultPath("catId2_catId1")
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
                .searchCriteria(searchCriteriaResponse)
                .newArrivalLimit(0)
                .preferredProductIds(Collections.emptyList())
                .categoryHeaderAsset(categoryHeaderAsset)
                .applicableFilters(Arrays.asList(designerFilter,sixeFilter,colorFilter,priceFilter,sleeveLengthFilter,styleFilter,necklineFilter,inStoreFilter,availabilityFilter))
                .seo(seoDetails)
                .defaultSortOption("BEST_MATCH")
                .availableSortOptions(Arrays.asList(priceHighToLow,priceLowToHigh,newestFirst,bestMatch,myFavorites))
                .pcsEnabled(true)
                .imageTemplateType(null)
                .displayAsGroups(false)
                .driveToGroupPDP(true)
                .reducedChildCount(false)
                .newAspectRatio(false)
                .defaultPath("catId1_catId2")
                .build());
        return categories;
    }
}
