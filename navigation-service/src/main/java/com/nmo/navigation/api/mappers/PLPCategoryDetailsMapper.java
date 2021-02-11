package com.sixthday.navigation.api.mappers;

import com.google.common.base.Strings;
import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.elasticsearch.models.ContextualProperty;
import com.sixthday.navigation.api.models.response.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

@Component
public class PLPCategoryDetailsMapper {

    private NavigationServiceConfig navigationServiceConfig;

    @Autowired
    public PLPCategoryDetailsMapper(final NavigationServiceConfig navigationServiceConfig) {
        this.navigationServiceConfig = navigationServiceConfig;
    }

    public PLPCategoryDetails map(CategoryDocument categoryDocument, Optional<String> parentCategoryId, Optional<String> siloCategoryId) {
        PLPCategoryDetails plpCategoryDetails = new PLPCategoryDetails();
        plpCategoryDetails.setId(categoryDocument.getId());
        plpCategoryDetails.setTemplateType(categoryDocument.getTemplateType());
        plpCategoryDetails.setDisplayAsGroups(categoryDocument.isDisplayAsGroups());
        plpCategoryDetails.setDriveToGroupPDP(categoryDocument.isDriveToGroupPDP());
        plpCategoryDetails.setSearchCriteria(new SearchCriteriaResponseMapper().map(categoryDocument));
        plpCategoryDetails.setTitle(getCategoryName(categoryDocument, parentCategoryId));
        plpCategoryDetails.setNewArrivalLimit(categoryDocument.getNewArrivalLimit());
        plpCategoryDetails.setPreferredProductIds(categoryDocument.getPreferredProductIds());
        plpCategoryDetails.setCategoryHeaderAsset(getCategoryHeaderAsset(categoryDocument));
        plpCategoryDetails.setApplicableFilters(getApplicableFilters(categoryDocument));
        plpCategoryDetails.setSeo(getSeoDetails(categoryDocument));
        plpCategoryDetails.setAvailableSortOptions(getSortOptions(categoryDocument));
        plpCategoryDetails.setDefaultSortOption(
                getDefaultSortOption(plpCategoryDetails.getAvailableSortOptions()).getValue().toString());
        plpCategoryDetails.setPcsEnabled(!categoryDocument.isExcludeFromPCS());
        plpCategoryDetails.setImageTemplateType(categoryDocument.getThumbImageShot());
        plpCategoryDetails.setReducedChildCount(getReducedChildCount(categoryDocument, siloCategoryId));
        return plpCategoryDetails;
    }

    private List<SortOption> getSortOptions(CategoryDocument categoryDocument) {
        List<SortOption> sortOptions = new ArrayList<>();
        sortOptions.add(new SortOption(SortOption.Option.PRICE_HIGH_TO_LOW, false));
        sortOptions.add(new SortOption(SortOption.Option.PRICE_LOW_TO_HIGH, false));
        if (categoryDocument.getProductRefinements().isSaleOnly()) {
            sortOptions.add(new SortOption(SortOption.Option.DISCOUNT_HIGH_TO_LOW, false));
            sortOptions.add(new SortOption(SortOption.Option.DISCOUNT_LOW_TO_HIGH, false));
        }
        if (!categoryDocument.isExcludeFromPCS()) {
            sortOptions.add(new SortOption(SortOption.Option.NEWEST_FIRST, false));
            sortOptions.add(new SortOption(SortOption.Option.BEST_MATCH, true));
        } else if (categoryDocument.getPreferredProductIds().isEmpty()) {
            sortOptions.add(new SortOption(SortOption.Option.NEWEST_FIRST, true));
        } else {
            sortOptions.add(new SortOption(SortOption.Option.NEWEST_FIRST, false));
            sortOptions.add(new SortOption(SortOption.Option.FEATURED, true));
        }
        sortOptions.add(new SortOption(SortOption.Option.MY_FAVORITES, false));
        return sortOptions;
    }

    private SortOption getDefaultSortOption(List<SortOption> options) {
        return options.stream()
                .filter(SortOption::getIsDefault)
                .findAny()
                .orElse(options.get(options.size() - 1));
    }

    private SeoDetails getSeoDetails(CategoryDocument categoryDocument) {
        return new SeoDetails(categoryDocument.getAlternateSeoName(), categoryDocument.getCanonicalUrl(),
                categoryDocument.getSeoContentTitle(), categoryDocument.getSeoTags(),
                categoryDocument.getSeoTitleOverride(), categoryDocument.getSeoContentDescription(), createRedirectDetails(categoryDocument));
    }

    private RedirectDetails createRedirectDetails(CategoryDocument categoryDocument) {
        try {
            return new RedirectDetails(Integer.parseInt(categoryDocument.getRedirectType()), categoryDocument.getRedirectTo());
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    private CategoryHeaderAsset getCategoryHeaderAsset(CategoryDocument categoryDocument) {
        CategoryHeaderAsset categoryHeaderAsset = new CategoryHeaderAsset();
        categoryHeaderAsset.setHideOnMobile(categoryDocument.isHideMobileImage());
        if (categoryDocument.isImageAvailable()) {
            categoryHeaderAsset.setCategoryAssetUrl(getHeaderAssetUrl(categoryDocument));
        }
        return categoryHeaderAsset;
    }

    private String getHeaderAssetUrl(CategoryDocument categoryDocument) {
        String categoryPathContext = StringUtils.isEmpty(categoryDocument.getImageAvailableOverride()) ?
                categoryDocument.getId() : categoryDocument.getImageAvailableOverride();
        return navigationServiceConfig.getCategoryConfig().getHeaderAssetUrl().replace("{categoryId}", categoryPathContext);
    }

    private List<Filter> getApplicableFilters(CategoryDocument categoryDocument) {
        List<Filter> filters = new ArrayList<>();
        if (!CollectionUtils.isEmpty(categoryDocument.getApplicableFilters()))
            categoryDocument.getApplicableFilters().forEach(filter -> filters.add(new Filter(filter.getDefaultName(),
                    StringUtils.isEmpty(filter.getAlternateName()) ? filter.getDefaultName() : filter.getAlternateName(), filter.getDisabled(), filter.getValues())));
        else if (categoryDocument.hasEmptySearchCriteria()) {
            List<NavigationServiceConfig.CategoryConfig.FilterOption> filterOptions = navigationServiceConfig.getCategoryConfig().getFilterOptions();
            filterOptions.forEach(filterOption -> filters.add(new Filter(filterOption.getFilterKey(), filterOption.getDisplayText(), Collections.emptyList(), null)));
        }
        return filters;
    }

    private String getCategoryName(CategoryDocument categoryDocument, Optional<String> parentCategoryId) {
        if (parentCategoryId.isPresent()) {
            List<ContextualProperty> contextualPropertyList = categoryDocument.getContextualProperties();
            if (!isEmpty(contextualPropertyList)) {
                return getCategoryNameFromContextualProperty(categoryDocument, contextualPropertyList, parentCategoryId.get());
            }
        }

        return categoryDocument.getName();
    }

    private String getCategoryNameFromContextualProperty(CategoryDocument categoryDocument, List<ContextualProperty> contextualPropertyList, String parentCategoryId) {
        for (ContextualProperty property : contextualPropertyList) {
            if (property.getParentId() != null && property.getParentId().equals(parentCategoryId) && !Strings.isNullOrEmpty(property.getDesktopAlternateName())) {
                return property.getDesktopAlternateName();
            }
        }
        return categoryDocument.getName();
    }

    private boolean getReducedChildCount(CategoryDocument categoryDocument, Optional<String> siloCategoryId) {
        String siloId = siloCategoryId.isPresent()
                ? siloCategoryId.get()
                : getDefaultSilo(categoryDocument);

        List<String> reducedChildCountSilos = navigationServiceConfig.getCategoryConfig().getReducedChildCountSilos();

        return reducedChildCountSilos.contains(siloId);
    }

    private String getDefaultSilo(CategoryDocument categoryDocument) {
        String[] catIds = categoryDocument.getDefaultPath().split("_");
        return (catIds.length > 1) ? catIds[1] : "";
    }
}
