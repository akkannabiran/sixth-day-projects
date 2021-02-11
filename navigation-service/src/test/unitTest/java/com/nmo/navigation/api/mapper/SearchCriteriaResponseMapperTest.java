package com.sixthday.navigation.api.mapper;

import com.sixthday.navigation.api.controllers.elasticsearch.documents.CategoryDocumentBuilder;
import com.sixthday.navigation.api.controllers.elasticsearch.documents.ProductRefinementsBuilder;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.elasticsearch.models.*;
import com.sixthday.navigation.api.mappers.SearchCriteriaResponseMapper;
import com.sixthday.navigation.api.models.response.SearchCriteriaResponse;
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
    public void testSearchCriteriaResponseMapperWhenSearchCriteriaNull() {
        SearchCriteriaResponseMapper searchCriteriaResponseMapper = new SearchCriteriaResponseMapper();
        CategoryDocument categoryDocument = new CategoryDocumentBuilder().withCmosCatalogCodes(null).withSearchCriteria(null).withShowAllProducts(true).build();

        SearchCriteriaResponse searchCriteriaResponse = searchCriteriaResponseMapper.map(categoryDocument);

        assertTrue(searchCriteriaResponse.isIncludeUnsellable());
        assertNull(searchCriteriaResponse.getInclude());
        assertNull(searchCriteriaResponse.getCatalogIds());
        assertNull(searchCriteriaResponse.getExclude());
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

        com.sixthday.navigation.api.models.response.PriceRange result = new SearchCriteriaResponseMapper().map(categoryDocument).getPriceRange();

        assertThat(result.getOption(), is(com.sixthday.navigation.api.models.response.PriceRange.PriceRangeType.valueOf(type)));
        assertThat(result.getMin(), is(new BigDecimal(min)));
        assertThat(result.getMax(), is(new BigDecimal(max)));
    }

    @Test
    public void shouldReturnOffIfPromoPriceIsNull() {
        CategoryDocument categoryDocument = new CategoryDocument();
        ProductRefinements productRefinements = new ProductRefinements();
        productRefinements.setPriceRange(null);
        categoryDocument.setProductRefinements(productRefinements);

        com.sixthday.navigation.api.models.response.PriceRange result = new SearchCriteriaResponseMapper().map(categoryDocument).getPriceRange();

        assertThat(result.getOption(), is(com.sixthday.navigation.api.models.response.PriceRange.PriceRangeType.OFF));
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

        ProductRefinements productRefinements = new ProductRefinementsBuilder().withRegOnly(true).build();

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
