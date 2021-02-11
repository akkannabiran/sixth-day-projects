package com.sixthday.category.api.mappers;

import com.sixthday.category.api.models.*;
import com.sixthday.category.api.utils.CategoryUtil;
import com.sixthday.category.config.CategoryServiceConfig;
import com.sixthday.category.elasticsearch.repository.CategoryRepository;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.elasticsearch.models.ContextualProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class CategoryMapper {

    private CategoryServiceConfig categoryServiceConfig;
    private CategoryRepository categoryRepository;

    @Autowired
    public CategoryMapper(final CategoryServiceConfig categoryServiceConfig, final CategoryRepository categoryRepository) {
        this.categoryServiceConfig = categoryServiceConfig;
        this.categoryRepository = categoryRepository;
    }

    public Category map(final CategoryDocumentInfo categoryDocumentInfo, Optional<String> siloCategoryId) {
        final Category category = new Category();

        final Optional<ContextualProperty> contextualProperty = categoryDocumentInfo.getCategoryDocument().getApplicableContextualProperty(categoryDocumentInfo.getParentCategoryId());

        category.setChildren(categoryDocumentInfo.getCategoryDocument().getChildCategoryOrder(contextualProperty.orElse(null)));
        category.setExcludedCountries(categoryDocumentInfo.getCategoryDocument().getExcludedCountries());
        category.setId(categoryDocumentInfo.getCategoryDocument().getId());
        category.setRedirectUrl(CategoryUtil.getRedirectUrl(categoryDocumentInfo.getCategoryDocument()));
        category.setRedirectType(categoryDocumentInfo.getCategoryDocument().getRedirectType());
        category.setName(categoryDocumentInfo.getCategoryDocument().getName(contextualProperty.orElse(null)));
        category.setSeoContentDescription(categoryDocumentInfo.getCategoryDocument().getSeoContentDescription());
        category.setSeoContentTitle(categoryDocumentInfo.getCategoryDocument().getSeoContentTitle());
        category.setSeoMetaTags(categoryDocumentInfo.getCategoryDocument().getSeoTags(categoryDocumentInfo.getParentCategoryDocument(), contextualProperty.orElse(null), categoryServiceConfig));
        category.setSeoPageTitle(categoryDocumentInfo.getCategoryDocument().getSeoPageTitle(contextualProperty.orElse(null), categoryDocumentInfo.getParentCategoryDocument(), categoryServiceConfig));
        category.setTemplateType(CategoryUtil.getCategoryTemplateType(categoryDocumentInfo.getCategoryDocument(), categoryServiceConfig));
        category.setBoutique(categoryDocumentInfo.getCategoryDocument().isBoutique() || categoryDocumentInfo.getCategoryDocument().isBoutiqueChild());
        category.setBoutiqueChild(categoryDocumentInfo.getCategoryDocument().isBoutiqueChild());
        category.setDriveToSubcategoryId(categoryDocumentInfo.getCategoryDocument().getDriveToSubCategoryIds(contextualProperty.orElse(null)).orElse(null));
        category.setMobileHideEntrySubcats(categoryDocumentInfo.getCategoryDocument().isMobileHideEntrySubcats());
        category.setHidden(categoryDocumentInfo.getCategoryDocument().isHidden());
        category.setExcludeFromPCS(categoryDocumentInfo.getCategoryDocument().isExcludeFromPCS());

        final Optional<CategoryDocument> driveToCategoryDocument = getDriveToCategoryDocument(categoryDocumentInfo.getCategoryDocument().getDriveToCategoryId(contextualProperty.orElse(null)).orElse(null));
        if (driveToCategoryDocument.isPresent()) {
            category.setCanonicalUrl(driveToCategoryDocument.get().getCanonicalUrl());
            category.setFirstSellableProductImageUrl(CategoryUtil.getProductImageUrl(driveToCategoryDocument.get().getFirstSellableProductImageUrl(), categoryServiceConfig));
        } else {
            category.setCanonicalUrl(categoryDocumentInfo.getCategoryDocument().getCanonicalUrl());
            category.setFirstSellableProductImageUrl(CategoryUtil.getProductImageUrl(categoryDocumentInfo.getCategoryDocument().getFirstSellableProductImageUrl(), categoryServiceConfig));
        }

        category.setResults(!categoryDocumentInfo.getCategoryDocument().isResults());
        category.setDynamic(!CategoryUtil.isDynamicCategory(categoryDocumentInfo.getCategoryDocument()));
        category.setCatalogTrees(categoryDocumentInfo.getCategoryDocument().getType());
        category.setDisplayAsGroups(categoryDocumentInfo.getCategoryDocument().isDisplayAsGroups());
        category.setDriveToGroupPDP(categoryDocumentInfo.getCategoryDocument().isDriveToGroupPDP());
        category.setSearchCriteria(new SearchCriteriaResponseMapper().map(categoryDocumentInfo.getCategoryDocument()));
        category.setNewArrivalLimit(categoryDocumentInfo.getCategoryDocument().getNewArrivalLimit());
        category.setPreferredProductIds(categoryDocumentInfo.getCategoryDocument().getPreferredProductIds());
        category.setCategoryHeaderAsset(getCategoryHeaderAsset(categoryDocumentInfo.getCategoryDocument()));
        category.setApplicableFilters(getApplicableFilters(categoryDocumentInfo.getCategoryDocument()));
        category.setSeo(getSeoDetails(categoryDocumentInfo.getCategoryDocument()));
        category.setAvailableSortOptions(getSortOptions(categoryDocumentInfo.getCategoryDocument()));
        category.setDefaultSortOption(
                getDefaultSortOption(category.getAvailableSortOptions()).getValue().toString());
        category.setPcsEnabled(!categoryDocumentInfo.getCategoryDocument().isExcludeFromPCS());
        category.setImageTemplateType(categoryDocumentInfo.getCategoryDocument().getThumbImageShot());
        category.setReducedChildCount(getReducedChildCount(categoryDocumentInfo.getCategoryDocument(), siloCategoryId));
        category.setNewAspectRatio(categoryDocumentInfo.isNewAspectRatio());
        category.setPageTemplateType(categoryDocumentInfo.getCategoryDocument().getTemplateType());
        category.setDefaultPath(categoryDocumentInfo.getCategoryDocument().getDefaultPath());

        return category;
    }

    private Optional<CategoryDocument> getDriveToCategoryDocument(final String driveToSubCategoryId) {
        if (driveToSubCategoryId != null) {
            CategoryDocument driveToSubCategoryDocument = categoryRepository.getCategoryDocument(driveToSubCategoryId);
            if (driveToSubCategoryDocument != null) {
                return Optional.of(driveToSubCategoryDocument);
            }
        }
        return Optional.empty();
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
        categoryHeaderAsset.setHideOsixthdaybile(categoryDocument.isHideMobileImage());
        if (categoryDocument.isImageAvailable()) {
            categoryHeaderAsset.setCategoryAssetUrl(getHeaderAssetUrl(categoryDocument));
        }
        return categoryHeaderAsset;
    }

    private String getHeaderAssetUrl(CategoryDocument categoryDocument) {
        String categoryPathContext = StringUtils.isEmpty(categoryDocument.getImageAvailableOverride()) ?
                categoryDocument.getId() : categoryDocument.getImageAvailableOverride();
        return categoryServiceConfig.getHeaderAssetUrl().replace("{categoryId}", categoryPathContext);
    }

    private List<Filter> getApplicableFilters(CategoryDocument categoryDocument) {
        List<Filter> filters = new ArrayList<>();
        if (!CollectionUtils.isEmpty(categoryDocument.getApplicableFilters()))
            categoryDocument.getApplicableFilters().forEach(filter -> filters.add(new Filter(filter.getDefaultName(),
                    StringUtils.isEmpty(filter.getAlternateName()) ? filter.getDefaultName() : filter.getAlternateName(), filter.getDisabled(), filter.getValues())));
        else if (categoryDocument.hasEmptySearchCriteria()) {
            List<CategoryServiceConfig.FilterOption> filterOptions = categoryServiceConfig.getFilterOptions();
            filterOptions.forEach(filterOption -> filters.add(new Filter(filterOption.getFilterKey(), filterOption.getDisplayText(), Collections.emptyList(), null)));
        }
        return filters;
    }

    private boolean getReducedChildCount(CategoryDocument categoryDocument, Optional<String> siloCategoryId) {
        String siloId = siloCategoryId.isPresent()
                ? siloCategoryId.get()
                : getDefaultSilo(categoryDocument);

        List<String> reducedChildCountSilos = categoryServiceConfig.getReducedChildCountSilos();

        return reducedChildCountSilos.contains(siloId);
    }

    private String getDefaultSilo(CategoryDocument categoryDocument) {
        String[] catIds = categoryDocument.getDefaultPath().split("_");
        return (catIds.length > 1) ? catIds[1] : "";
    }

}
