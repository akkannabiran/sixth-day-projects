package com.sixthday.category.api.mappers;

import com.sixthday.category.api.models.PriceRange;
import com.sixthday.category.api.models.SearchCriteriaResponse;

import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.elasticsearch.models.PriceRangeAtg;
import com.sixthday.navigation.api.elasticsearch.models.ProductRefinements;
import com.sixthday.navigation.api.elasticsearch.models.SearchCriteria;
import com.sixthday.navigation.api.elasticsearch.models.SearchCriteriaOptions;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

@RunWith(JUnitParamsRunner.class)
public class SearchCriteriaResponseMapperTest {

    @Test
    public void testSearchCriteriaResponseMapperWhenSearchCriterisEmpty() {
        SearchCriteriaResponseMapper searchCriteriaResponseMapper = new SearchCriteriaResponseMapper();
        CategoryDocument categoryDocument = CategoryDocument.builder().cmosCatalogCodes(null)
                .productRefinements(ProductRefinements.builder()
                        .saleOnly(false)
                        .adornOnly(false)
                        .adornAndSaleOnly(false)
                        .regOnly(false)
                        .priceRange(PriceRangeAtg.builder().option("OFF").min(BigDecimal.ZERO).max(BigDecimal.ZERO).build())
                        .build())
                .searchCriteria(SearchCriteria.builder()
                .include(SearchCriteriaOptions.builder()
                        .hierarchy(Collections.emptyList())
                        .attributes(Collections.emptyList())
                        .promotions(Collections.emptyList())
                        .build())
                .exclude(SearchCriteriaOptions.builder()
                        .hierarchy(Collections.emptyList())
                        .attributes(Collections.emptyList())
                        .promotions(Collections.emptyList())
                        .build())
                .build()).showAllProducts(true).build();

        SearchCriteriaResponse searchCriteriaResponse = searchCriteriaResponseMapper.map(categoryDocument);

        assertTrue(searchCriteriaResponse.isIncludeUnsellable());
        assertNull(searchCriteriaResponse.getCatalogIds());
        assertTrue(searchCriteriaResponse.getInclude().hasEmptyHierarchyAndAttributes());
        assertTrue(searchCriteriaResponse.getExclude().hasEmptyHierarchyAndAttributes());
    }

    @Test
    @Parameters({"PROMO_PRICE,20,30",
            "TYPE113_PERCENT_OFF,10,50",
            "OFF,0,0"})
    public void shouldSetTheCorrectPriceRangeBasedOnTheCategoryDocumentData(String type, double min, double max) {
        CategoryDocument categoryDocument = new CategoryDocument();
        ProductRefinements productRefinements = new ProductRefinements();
        productRefinements.setPriceRange(new PriceRangeAtg(type, new BigDecimal(min), new BigDecimal(max)));
        categoryDocument.setProductRefinements(productRefinements);

        PriceRange result = new SearchCriteriaResponseMapper().map(categoryDocument).getPriceRange();

        assertThat(result.getOption(), is(PriceRange.PriceRangeType.valueOf(type)));
        assertThat(result.getMin(), is(new BigDecimal(min)));
        assertThat(result.getMax(), is(new BigDecimal(max)));
    }

    @Test
    public void shouldReturnOffIfPromoPriceIsNull() {
        CategoryDocument categoryDocument = new CategoryDocument();
        ProductRefinements productRefinements = new ProductRefinements();
        productRefinements.setPriceRange(null);
        categoryDocument.setProductRefinements(productRefinements);

        PriceRange result = new SearchCriteriaResponseMapper().map(categoryDocument).getPriceRange();

        assertThat(result.getOption(), is(PriceRange.PriceRangeType.OFF));
        assertThat(result.getMin(), is(new BigDecimal(0)));
        assertThat(result.getMax(), is(new BigDecimal(0)));
    }

    @Test
    public void testSearchCriteriaResponseMapperWhenSearchCriteriaNotNull() {
        SearchCriteriaResponseMapper searchCriteriaResponseMapper = new SearchCriteriaResponseMapper();
        Map<String, String> hierarchy = new HashMap<>(2);
        Map<String, List<String>> attributes = new HashMap<>(2);
        hierarchy.put("level1", "Women's Apparel");
        hierarchy.put("level2", "Dresses");
        attributes.put("Sale", new ArrayList<>(Arrays.asList(new String[]{"Designer sale"})));
        attributes.put("Material", new ArrayList<>(Arrays.asList(new String[]{"Corduroy"})));
        List<Map<String, String>> searchCriteriaHierarchy = Arrays.asList(hierarchy);
        List<Map<String, List<String>>> searchCriteriaAttributes = Arrays.asList(attributes);
        List<String> catalogIds = new ArrayList<>(Arrays.asList(new String[]{"NMID1", "NMID2"}));
        final List<String> promotions = Arrays.asList("promo1", "promo2");
        SearchCriteriaOptions include = new SearchCriteriaOptions(promotions, searchCriteriaHierarchy, searchCriteriaAttributes);
        CategoryDocument categoryDocument = new CategoryDocument();
        SearchCriteriaOptions exclude = null;
        categoryDocument.setSearchCriteria(new SearchCriteria(include, exclude));

        ProductRefinements productRefinements = ProductRefinements.builder().regOnly(true).build();

        categoryDocument.setProductRefinements(productRefinements);
        categoryDocument.setShowAllProducts(true);
        categoryDocument.setCmosCatalogCodes(catalogIds);
        SearchCriteriaResponse searchCriteriaResponse = searchCriteriaResponseMapper.map(categoryDocument);

        assertTrue(searchCriteriaResponse.isIncludeUnsellable());
        assertEquals(searchCriteriaResponse.getInclude(), include);
        assertNull(searchCriteriaResponse.getExclude());
        assertEquals(searchCriteriaResponse.getCatalogIds(), catalogIds);
        assertEquals(searchCriteriaResponse.getInclude().getAttributes(), searchCriteriaAttributes);
        assertEquals(searchCriteriaResponse.getProductClassType(), "REGULAR_PRICE_ONLY");
    }
}
