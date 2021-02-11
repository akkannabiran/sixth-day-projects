package com.sixthday.category.api.utils;

import com.sixthday.category.config.CategoryServiceConfig;
import com.sixthday.category.exceptions.UnknownCategoryTemplateTypeException;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Slf4j
public class CategoryUtil {

    private CategoryUtil() {
    }

    public static String getCategoryTemplateType(final CategoryDocument categoryDocument, final CategoryServiceConfig categoryServiceConfig) {
        Optional<CategoryServiceConfig.CategoryTemplate> categoryTemplate = categoryServiceConfig
                .getCategoryTemplates()
                .stream().filter(localCategoryTemplate -> localCategoryTemplate.getName().equals(categoryDocument.getTemplateType())).findAny();
        if (categoryTemplate.isPresent()) {
            return categoryTemplate.get().getKey();
        } else {
            log.error("Found unknown template type {} for category {}", categoryDocument.getTemplateType(), categoryDocument.getId());
            throw new UnknownCategoryTemplateTypeException(categoryDocument.getId(), categoryDocument.getTemplateType());
        }
    }

    public static String getRedirectUrl(final CategoryDocument categoryDocument) {
        if (!StringUtils.isEmpty(categoryDocument.getRedirectTo())) {
            return categoryDocument.getRedirectTo();
        } else if (!StringUtils.isEmpty(categoryDocument.getLongDescription())) {
            return categoryDocument.getLongDescription();
        }
        return null;
    }

    public static String getProductImageUrl(final String firstSellableProductImageUrl, final CategoryServiceConfig categoryServiceConfig) {
        return StringUtils.isEmpty(firstSellableProductImageUrl)
                ? categoryServiceConfig.getDefaultProductImageSrc()
                : categoryServiceConfig.getImageServerUrl().concat(firstSellableProductImageUrl);
    }

    public static boolean isDynamicCategory(CategoryDocument categoryDocument) {
        return categoryDocument.getSearchCriteria() != null && isSearchCriteriaEmpty(categoryDocument);
    }

    private static boolean isSearchCriteriaEmpty(CategoryDocument categoryDocument) {
        return categoryDocument.getSearchCriteria().getExclude() != null && hasExcludeSearchFactors(categoryDocument) ||
                categoryDocument.getSearchCriteria().getInclude() != null && hasIncludeSearchFactors(categoryDocument);
    }

    private static boolean hasExcludeSearchFactors(CategoryDocument categoryDocument) {
        return !CollectionUtils.isEmpty(categoryDocument.getSearchCriteria().getExclude().getAttributes()) ||
                !CollectionUtils.isEmpty(categoryDocument.getSearchCriteria().getExclude().getHierarchy()) ||
                !CollectionUtils.isEmpty(categoryDocument.getSearchCriteria().getExclude().getPromotions());
    }

    private static boolean hasIncludeSearchFactors(CategoryDocument categoryDocument) {
        return !CollectionUtils.isEmpty(categoryDocument.getSearchCriteria().getInclude().getAttributes()) ||
                !CollectionUtils.isEmpty(categoryDocument.getSearchCriteria().getInclude().getHierarchy()) ||
                !CollectionUtils.isEmpty(categoryDocument.getSearchCriteria().getInclude().getPromotions());
    }
}
