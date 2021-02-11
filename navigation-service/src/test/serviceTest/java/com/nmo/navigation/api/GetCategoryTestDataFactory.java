package com.sixthday.navigation.api;

import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import org.elasticsearch.action.get.MultiGetItemResponse;

import java.util.*;

import static com.sixthday.navigation.CategoryTestDataFactory.sisterLinksDocument;
import static com.sixthday.navigation.CategoryTestDataFactory.topCategories;

public class GetCategoryTestDataFactory {

    public static String CATEGORY_IDS = "cat1,cat2";

    public static Iterable<String> ITERABLE_CATEGORY_IDS = Arrays.asList(CATEGORY_IDS.split(","));

    public static String CATEGORY_INDEX = "category_index";

    public static String DOCUMENT_TYPE = "_doc";

    public static String CATEGORY_DOCUMENT_AS_STRING_FOR_BREADCRUMBS = "{\"id\":\"cat1\",\"displayName\":\"Ralph Lauren\",\"url\":\"/Ralph-Lauren/Designers/cat1/c.cat\"}";

    public static String INVALID_CATEGORY_DOCUMENT_AS_STRING_FOR_BREADCRUMBS = "\"id\":\"cat1\",\"displayName\":\"Ralph Lauren\",\"url\":\"/Ralph-Lauren/Designers/cat1/c.cat\"";

    public static String CATEGORY_DOCUMENT_AS_STRING_FOR_CATEGORY_DETAILS_PLP =
            "{" +
                    "\"id\":\"cat1\"," +
                    "\"name\":\"CategoryName\"," +
                    "\"searchCriteria\":{" +
                    "\"include\":{" +
                    "\"hierarchy\":[{" +
                    "\"level1\":\"Women's Apparel\"," +
                    "\"level2\":\"Dresses\"" +
                    "},{" +
                    "\"level1\":\"Women's Apparel\"," +
                    "\"level2\":\"Skirts\"" +
                    "}]," +
                    "\"attributes\":[{" +
                    "\"Trends\":[\"DT\"]" +
                    "}]" +
                    "}," +
                    "\"exclude\":{" +
                    "\"hierarchy\":[{" +
                    "\"level1\":\"Women's Apparel\"," +
                    "\"level2\":\"Tops\"" +
                    "}]" +
                    "}" +
                    "}," +
                    "\"newArrivalLimit\": \"1\"," +
                    "\"productRefinements\":{" +
                    "\"regOnly\": \"true\"," +
                    "\"saleOnly\": \"false\"," +
                    "\"adornOnly\": \"false\"," +
                    "\"adornAndSaleOnly\": \"false\"," +
                    "\"priceRange\":{\"min\": 1.5, \"max\": 2, \"option\":\"PROMO_PRICE\"}}," +
                    "\"includeAllItems\": \"false\"," +
                    "\"imageAvailable\": \"true\"," +
                    "\"hideMobileImage\": \"true\"," +
                    "\"preferredProductIds\": [\"prod1234\",\"prod1235\",\"prod1236\"]," +
                    "\"applicableFilters\": [{\"defaultName\":\"LifeStyle\", \"alternateName\":\"lifestyle\", \"disabled\":[\"abc\"], \"values\":[\"val1\",\"val2\",\"abc\"]}]," +
                    "\"alternateSeoName\": \"nameOverride\"," +
                    "\"canonicalUrl\": \"canonicalUrl\"," +
                    "\"seoContentTitle\": \"seoTitle\"," +
                    "\"seoTags\": \"metaInformation\"," +
                    "\"seoTitleOverride\": \"titleOverride\"," +
                    "\"seoContentDescription\": \"seoContent\"," +
                    "\"contextualProperties\":[{" +
                    "\"parentId\":\"parentCat00002\",\"desktopAlternateName\":\"\"" +
                    "},{" +
                    "\"parentId\":\"parentCat00001\",\"desktopAlternateName\":\"DesktopAlternateName\"" +
                    "}]" +
                    "}";

    public static MultiGetItemResponse[] getTestMultiGetItemResponse(MultiGetItemResponse multiGetItemResponse) {
        MultiGetItemResponse[] multiGetItemResponses = new MultiGetItemResponse[2];
        multiGetItemResponses[0] = multiGetItemResponse;
        multiGetItemResponses[1] = multiGetItemResponse;
        return multiGetItemResponses;
    }

    public static Map<CategoryDocument, List<CategoryDocument>> brandLinks() {
        Map<CategoryDocument, List<CategoryDocument>> brandLinks = new HashMap<>();
        brandLinks.put(sisterLinksDocument(), topCategories());
        return brandLinks;
    }
}
