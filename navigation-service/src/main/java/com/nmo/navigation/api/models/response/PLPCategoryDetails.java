package com.sixthday.navigation.api.models.response;

import lombok.Data;

import java.util.List;

@Data
public class PLPCategoryDetails {
    private String id;
    private SearchCriteriaResponse searchCriteria;
    private String title;
    private String templateType;
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
}
