package com.sixthday.category.api.utils;

import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CategoryServiceUtil {

    private CategoryServiceUtil() {
    }

    public static boolean useCategoryDocumentToBuildTheResponse(final CategoryDocument categoryDocument, final String parentCategoryId) {
        return !StringUtils.isEmpty(parentCategoryId) || !StringUtils.isEmpty(categoryDocument.getSeoTags()) && !StringUtils.isEmpty(categoryDocument.getSeoTitleOverride());
    }

    public static String getParentCategoryId(final Map.Entry<String, String> entrySet, final CategoryDocument categoryDocument) {
        if (!StringUtils.isEmpty(entrySet.getValue()))
            return entrySet.getValue();
        else {
            final String[] paths = categoryDocument.getDefaultPath().split("_");
            return paths[paths.length - 2];
        }
    }

    public static boolean isValidRequest(final Map<String, String> categoryIds, final int thresholdLimitValue) {
        return !CollectionUtils.isEmpty(categoryIds) && categoryIds.size() <= thresholdLimitValue;
    }

    public static Set<String> collectCategoryIdsFromMap(final Map<String, String> categoryIds) {
        Set<String> ids = new HashSet<>();
        ids.addAll(categoryIds.keySet().parallelStream().filter(key -> !StringUtils.isEmpty(key)).collect(Collectors.toSet()));
        ids.addAll(categoryIds.values().parallelStream().filter(key -> !StringUtils.isEmpty(key)).collect(Collectors.toSet()));
        return ids;
    }

    public static List<String> collectPathCategoryIds(final Optional<String> navPath, final Map.Entry<String, String> entrySet, final CategoryDocument categoryDocument) {
        return Stream.of(navPath.orElse(""), categoryDocument.getDefaultPath(), entrySet.getKey())
                .filter(categoryPath -> !StringUtils.isEmpty(categoryPath))
                    .findFirst()
                    .map(categoryPath -> Arrays.asList(categoryPath.split("_")))
                    .orElse(Collections.emptyList());
    }
}