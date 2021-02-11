package com.sixthday.category.api.models;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    private boolean boutique;
    private boolean boutiqueChild;
    private boolean results;
    private boolean dynamic;
    private boolean mobileHideEntrySubcats;
    private boolean hidden;
    private boolean excludeFromPCS;
    private String id;
    private String name;
    private String templateType;
    private String redirectUrl;
    private String redirectType;
    private String firstSellableProductImageUrl;
    private String seoMetaTags;
    private String seoContentTitle;
    private String seoContentDescription;
    private String canonicalUrl;
    private String seoPageTitle;
    private String driveToSubcategoryId;
    private List<String> children;
    private List<String> excludedCountries;
    private List<String> catalogTrees;
    private SearchCriteriaResponse searchCriteria;
    private Integer newArrivalLimit;
    private List<String> preferredProductIds;
    private CategoryHeaderAsset categoryHeaderAsset;
    private List<Filter> applicableFilters;
    private SeoDetails seo;
    private String defaultSortOption;
    private List<SortOption> availableSortOptions;
    private Boolean pcsEnabled;
    private String imageTemplateType;
    private boolean displayAsGroups;
    private boolean driveToGroupPDP;
    private boolean reducedChildCount;
    private boolean newAspectRatio;
    private String pageTemplateType;
    private String defaultPath;
}
