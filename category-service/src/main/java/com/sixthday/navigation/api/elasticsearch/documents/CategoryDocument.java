package com.sixthday.navigation.api.elasticsearch.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sixthday.navigation.api.elasticsearch.models.Filter;
import com.sixthday.navigation.api.elasticsearch.models.ProductRefinements;
import com.sixthday.navigation.api.elasticsearch.models.ContextualProperty;
import com.sixthday.navigation.api.elasticsearch.models.SearchCriteria;
import lombok.*;
import org.springframework.util.*;
import com.sixthday.category.config.CategoryServiceConfig;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.web.util.HtmlUtils.htmlUnescape;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class CategoryDocument implements Serializable {
    public static final String DOCUMENT_TYPE = "_doc";
    private static final long serialVersionUID = 5614566373912256393L;
    private boolean boutique;
    private boolean boutiqueChild;
    private boolean mobileHideEntrySubcats;
    private boolean hidden;
    @JsonProperty(value = "isDeleted")
    private boolean deleted;
    @JsonProperty(value = "noResults")
    private boolean results;
    private boolean excludeFromPCS;
    private String id;
    @Getter(AccessLevel.NONE)
    private String name;
    private String templateType;
    private String longDescription;
    private String redirectType;
    private String redirectTo;
    private String firstSellableProductImageUrl;
    private String seoTags;
    private String seoContentTitle;
    private String seoContentDescription;
    private String alternateSeoName;
    private String seoTitleOverride;
    private String canonicalUrl;
    private String defaultPath;
    private List<String> children;
    private List<ContextualProperty> contextualProperties;
    private List<String> excludedCountries;
    private SearchCriteria searchCriteria;
    private List<String> type;
    private String leftNavImageAvailableOverride;
    private Map<String, Integer> parents;
    private boolean imageAvailable;
    private boolean leftNavImageAvailable;
    private boolean expandCategory;
    private boolean dontShowChildren;
    private boolean personalized;
    private boolean displayAsGroups;
    private boolean driveToGroupPDP;
    private boolean showAllProducts;
    private boolean includeAllItems;
    private boolean hideMobileImage;
    private String thumbImageShot;
    private Integer newArrivalLimit;
    private List<String> cmosCatalogCodes;
    private List<String> preferredProductIds;
    private ProductRefinements productRefinements;
    private String imageAvailableOverride;
    private List<Filter> applicableFilters;


    public String getName() {
        return htmlUnescape(name);
    }

    public Optional<ContextualProperty> getApplicableContextualProperty(final String parentCategoryId) {
        if (isEmpty(this.getContextualProperties()))
            return Optional.empty();
        for (ContextualProperty contextualProperty : getContextualProperties()) {
            if (contextualProperty != null && !StringUtils.isEmpty(contextualProperty.getParentId()) && contextualProperty.getParentId().equals(parentCategoryId))
                return Optional.of(contextualProperty);
        }
        return Optional.empty();
    }

    public String getName(final ContextualProperty contextualProperty) {
        return (contextualProperty != null && !StringUtils.isEmpty(contextualProperty.getDesktopAlternateName())) ? contextualProperty.getDesktopAlternateName() : getName();
    }

    public String getSeoPageTitle(final ContextualProperty contextualProperty, final CategoryDocument parentCategoryDocument, final CategoryServiceConfig categoryServiceConfig) {
        String seoContentTitle = categoryServiceConfig.getSeoContentTitle();
    	String seoPageTitleEnd = " in parent category " + seoContentTitle;
        if (!ObjectUtils.isEmpty(parentCategoryDocument)) {
            seoPageTitleEnd = " in " + parentCategoryDocument.getName() + " " + seoContentTitle;
        }
        StringJoiner joiner = new StringJoiner("");
        if (!StringUtils.isEmpty(getSeoTitleOverride())) {
            return getSeoTitleOverride();
        } else if (!StringUtils.isEmpty(getAlternateSeoName())) {
            return joiner.add(getAlternateSeoName()).add(seoPageTitleEnd).toString();
        } else if (contextualProperty != null && !StringUtils.isEmpty(contextualProperty.getDesktopAlternateName())) {
            return joiner.add(contextualProperty.getDesktopAlternateName()).add(seoPageTitleEnd).toString();
        } else {
            return joiner.add(getName()).add(seoPageTitleEnd).toString();
        }
    }

    public List<String> getChildCategoryOrder(final ContextualProperty contextualProperty) {
        List<String> resultedChildren = new ArrayList<>();
        if (contextualProperty != null && !isEmpty(contextualProperty.getChildCategoryOrder())) {
            resultedChildren.addAll(contextualProperty.getChildCategoryOrder()
                    .stream()
                    .filter(childCatId -> getChildren().indexOf(childCatId) != -1)
                    .collect(Collectors.toList()));
            resultedChildren.addAll(getChildren().stream().filter(categoryId -> resultedChildren.indexOf(categoryId) == -1).collect(Collectors.toList()));
        } else {
            resultedChildren.addAll(CollectionUtils.isEmpty(getChildren()) ? Collections.emptyList() : getChildren());
        }
        return resultedChildren;
    }

    public Optional<String> getDriveToCategoryId(ContextualProperty contextualProperty) {
        if (contextualProperty != null && !StringUtils.isEmpty(contextualProperty.getDriveToSubcategoryId())) {
            final List<String> driveToSubCategoryIds = Arrays.asList(contextualProperty.getDriveToSubcategoryId().split(":"));
            return Optional.of(driveToSubCategoryIds.get(driveToSubCategoryIds.size() - 1));
        }
        return Optional.empty();
    }

    public Optional<String> getDriveToSubCategoryIds(ContextualProperty contextualProperty) {
        if (contextualProperty != null) {
            return Optional.ofNullable(contextualProperty.getDriveToSubcategoryId());
        }
        return Optional.empty();
    }

    public String getSeoTags(CategoryDocument parentCategoryDocument, final ContextualProperty contextualProperty, final CategoryServiceConfig categoryServiceConfig) {
    	String seoContentTitle = categoryServiceConfig.getSeoContentTitle();
        String seoTag = "<meta name=\"description\" content=\"Shop {SEO_NAME} in {PARENT_CATEGORY_NAME} " + seoContentTitle + ", where you will find free shipping on the latest in fashion from top designers.\"/>";
        if (!StringUtils.isEmpty(getSeoTags()))
            return getSeoTags();
        seoTag = parentCategoryDocument != null ? seoTag.replace("{PARENT_CATEGORY_NAME}", parentCategoryDocument.getName()) : seoTag.replace(" in {PARENT_CATEGORY_NAME}", "");
        return seoTag.replace("{SEO_NAME}", !StringUtils.isEmpty(getAlternateSeoName()) ? getAlternateSeoName() : getName(contextualProperty));
    }

    public List<String> getPreferredProductIds() {
        return this.preferredProductIds == null ? Collections.emptyList() : this.preferredProductIds;
    }

    public boolean hasEmptySearchCriteria() {
        return searchCriteria == null || searchCriteria.hasEmptyIncludeAndExcludeOptions();
    }

}