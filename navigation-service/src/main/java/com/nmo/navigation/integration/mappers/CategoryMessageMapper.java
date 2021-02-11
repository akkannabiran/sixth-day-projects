package com.sixthday.navigation.integration.mappers;

import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.elasticsearch.models.SearchCriteria;
import com.sixthday.navigation.api.elasticsearch.models.SearchCriteriaOptions;
import com.sixthday.navigation.integration.messages.CategoryMessage;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

public class CategoryMessageMapper {
    public CategoryDocument map(CategoryMessage categoryMessage) {
        CategoryDocument categoryDocument = new CategoryDocument();

        categoryDocument.setId(categoryMessage.getId());
        categoryDocument.setDisplayName(categoryMessage.getDisplayName());
        categoryDocument.setTemplateType(categoryMessage.getTemplateType());
        categoryDocument.setName(categoryMessage.getName());
        categoryDocument.setLeftNavImageAvailableOverride(categoryMessage.getLeftNavImageAvailableOverride());
        categoryDocument.setLongDescription(categoryMessage.getLongDescription());
        categoryDocument.setFirstSellableProductImageUrl(categoryMessage.getFirstSellableProductImageUrl());

        categoryDocument.setParents(categoryMessage.getParents() == null ? emptyMap() : categoryMessage.getParents());

        //SEO Fields
        categoryDocument.setAlternateSeoName(categoryMessage.getAlternateSeoName());
        categoryDocument.setSeoTitleOverride(categoryMessage.getSeoTitleOverride());
        categoryDocument.setCanonicalUrl(categoryMessage.getCanonicalUrl());
        categoryDocument.setSeoContentTitle(categoryMessage.getSeoContentTitle());
        categoryDocument.setSeoContentDescription(categoryMessage.getSeoContentDescription());
        categoryDocument.setSeoTags(categoryMessage.getSeoTags());

        //Category Flags
        categoryDocument.setBoutique(categoryMessage.isBoutique());
        categoryDocument.setBoutiqueChild(categoryMessage.isBoutiqueChild());
        categoryDocument.setImageAvailable(categoryMessage.isImageAvailable());
        categoryDocument.setMobileHideEntrySubcats(categoryMessage.isMobileHideEntrySubcats());
        categoryDocument.setLeftNavImageAvailable(categoryMessage.isLeftNavImageAvailable());
        categoryDocument.setExpandCategory(categoryMessage.isExpandCategory());
        categoryDocument.setDontShowChildren(categoryMessage.isDontShowChildren());
        categoryDocument.setPersonalized(categoryMessage.isPersonalized());
        categoryDocument.setHidden(categoryMessage.isHidden());
        categoryDocument.setNoResults(categoryMessage.isNoResults());
        categoryDocument.setDisplayAsGroups(categoryMessage.isDisplayAsGroups());
        categoryDocument.setDriveToGroupPDP(categoryMessage.isDriveToGroupPDP());
        categoryDocument.setExcludeFromPCS(categoryMessage.isExcludeFromPCS());
        categoryDocument.setProductRefinements(categoryMessage.getProductRefinements());
        categoryDocument.setIncludeAllItems(categoryMessage.isIncludeAllItems());
        categoryDocument.setShowAllProducts(categoryMessage.isShowAllProducts());
        categoryDocument.setImageAvailable(categoryMessage.isImageAvailable());
        categoryDocument.setHideMobileImage(categoryMessage.isHideMobileImage());

        categoryDocument.setSearchCriteria(getSearchCriteria(categoryMessage));
        categoryDocument.setCmosCatalogCodes(categoryMessage.getCmosCatalogCodes() == null ? emptyList() : categoryMessage.getCmosCatalogCodes());
        categoryDocument.setNewArrivalLimit(categoryMessage.getNewArrivalLimit() == null ? 0 : categoryMessage.getNewArrivalLimit());
        categoryDocument.setContextualProperties(categoryMessage.getContextualProperties());
        categoryDocument.setChildren(categoryMessage.getChildren());
        categoryDocument.setDefaultPath(categoryMessage.getDefaultPath());
        categoryDocument.setPreferredProductIds(categoryMessage.getPreferredProductIds());
        categoryDocument.setType(categoryMessage.getType());
        categoryDocument.setApplicableFilters(categoryMessage.getApplicableFilters());
        categoryDocument.setImageAvailableOverride(categoryMessage.getImageAvailableOverride());
        categoryDocument.setExcludedCountries(categoryMessage.getExcludedCountries());
        categoryDocument.setThumbImageShot(categoryMessage.getThumbImageShot());
        categoryDocument.setRedirectTo(categoryMessage.getRedirectTo());
        categoryDocument.setRedirectType(categoryMessage.getRedirectType());
        categoryDocument.setDeleted(categoryMessage.isDeleted());
        return categoryDocument;
    }

    private SearchCriteria getSearchCriteria(CategoryMessage categoryMessage) {
        SearchCriteria searchCriteria = categoryMessage.getSearchCriteria();
        SearchCriteriaOptions include = getSearchCriteriaOption(searchCriteria.getInclude());
        SearchCriteriaOptions exclude = getSearchCriteriaOption(searchCriteria.getExclude());
        return new SearchCriteria(include, exclude);
    }

    private SearchCriteriaOptions getSearchCriteriaOption(SearchCriteriaOptions searchCriteriaOption) {
        SearchCriteriaOptions newSearchCriteriaOption = new SearchCriteriaOptions(emptyList(), emptyList(), emptyList());

        if (searchCriteriaOption.getHierarchy() != null)
            newSearchCriteriaOption.setHierarchy(searchCriteriaOption.getHierarchy());

        if (searchCriteriaOption.getAttributes() != null)
            newSearchCriteriaOption.setAttributes(searchCriteriaOption.getAttributes());

        if (searchCriteriaOption.getPromotions() != null)
            newSearchCriteriaOption.setPromotions(searchCriteriaOption.getPromotions());

        return newSearchCriteriaOption;
    }
}
