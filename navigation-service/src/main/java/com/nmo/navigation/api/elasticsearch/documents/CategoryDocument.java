package com.sixthday.navigation.api.elasticsearch.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sixthday.navigation.api.elasticsearch.models.*;
import lombok.*;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.*;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class CategoryDocument implements Serializable {
    public static final String DOCUMENT_TYPE = "_doc";
    private static final long serialVersionUID = 5614566373912256393L;
    private String id;
    private String displayName;

    private String name;
    private String leftNavImageAvailableOverride;
    private String templateType;
    private String defaultPath;
    private String firstSellableProductImageUrl;

    private String longDescription;

    private Map<String, Integer> parents;

    //SEO Fields
    private String alternateSeoName;
    private String seoTitleOverride;
    private String canonicalUrl;
    private String seoContentTitle;
    private String seoContentDescription;
    private String seoTags;

    //Flags
    private boolean boutique;
    private boolean boutiqueChild;
    private boolean imageAvailable;
    private boolean mobileHideEntrySubcats;
    private boolean leftNavImageAvailable;
    private boolean expandCategory;
    private boolean dontShowChildren;
    private boolean personalized;
    private boolean hidden;
    private boolean noResults;
    private boolean displayAsGroups;
    private boolean driveToGroupPDP;
    private boolean excludeFromPCS;
    private boolean showAllProducts;
    private boolean includeAllItems;
    private boolean hideMobileImage;
    private String thumbImageShot;

    private SearchCriteria searchCriteria;

    private Integer newArrivalLimit;

    private List<String> cmosCatalogCodes;
    private List<String> preferredProductIds;

    private List<ContextualProperty> contextualProperties;
    private ProductRefinements productRefinements;
    private List<String> children;
    private List<String> type;

    private String imageAvailableOverride;
    private List<Filter> applicableFilters;
    private List<String> excludedCountries;
    private String redirectType;
    private String redirectTo;

    @JsonProperty(value = "isDeleted")
    private boolean deleted;

    public List<String> getPreferredProductIds() {
        return this.preferredProductIds == null ? Collections.emptyList() : this.preferredProductIds;
    }

    private Optional<ContextualProperty> getApplicablePropertiesForCategory(String parentCategoryId) {
        if (isEmpty(this.getContextualProperties())) {
            return Optional.empty();
        }
        return this.getContextualProperties().stream()
                .filter(contextualProperty -> parentCategoryId.equals(contextualProperty.getParentId()))
                .findAny();
    }

    public String getDesktopAlternateName(String parentCategoryId) {
        Optional<ContextualProperty> applicablePropertiesForCategory = getApplicablePropertiesForCategory(parentCategoryId);
        boolean alternateCategoryNameContextPropertyExist = applicablePropertiesForCategory.isPresent() && !StringUtils.isEmpty(applicablePropertiesForCategory.get().getDesktopAlternateName());
        return alternateCategoryNameContextPropertyExist ? applicablePropertiesForCategory.get().getDesktopAlternateName() : this.getName();
    }

    public String getMobileAlternateName(String parentCategoryId) {
        Optional<ContextualProperty> applicablePropertiesForCategory = getApplicablePropertiesForCategory(parentCategoryId);
        boolean alternateCategoryNameContextPropertyExist = applicablePropertiesForCategory.isPresent() && !StringUtils.isEmpty(applicablePropertiesForCategory.get().getMobileAlternateName());
        return alternateCategoryNameContextPropertyExist ? applicablePropertiesForCategory.get().getMobileAlternateName() : this.getName();
    }

    public boolean hasEmptySearchCriteria() {
        return searchCriteria == null || searchCriteria.hasEmptyIncludeAndExcludeOptions();
    }
}