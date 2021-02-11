package com.sixthday.navigation.elasticsearch.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sixthday.navigation.batch.processor.LeftNavTreeProcessor;
import com.sixthday.navigation.domain.ContextualProperty;
import lombok.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
@Builder
public class CategoryDocument implements Serializable {
    private String id;
    private String displayName;

    private String name;
    private String templateType;
    private String leftNavImageAvailableOverride;
    private Map<String, Integer> parents;
    private String defaultPath;

    private String alternateSeoName;
    private String seoTitleOverride;
    private String canonicalUrl;
    private String seoContentTitle;
    private String seoContentDescription;
    private String seoTags;

    private boolean boutique;
    private boolean boutiqueChild;
    private boolean imageAvailable;
    private boolean leftNavImageAvailable;
    private boolean expandCategory;
    private boolean dontShowChildren;
    private boolean personalized;
    private boolean hidden;
    private boolean noResults;
    private boolean displayAsGroups;
    private boolean driveToGroupPDP;
    private boolean excludeFromPCS;
    @JsonProperty("isDeleted")
    private boolean isDeleted;

    private List<String> type;

    private List<String> children;
    private List<ContextualProperty> contextualProperties;

    private List<String> excludedCountries;

    public ContextualProperty getApplicablePropertiesForCategory(String parentCategoryId) {
        if (isEmpty(this.getContextualProperties())) {
            return null;
        }
        for (ContextualProperty contextualProperty : getContextualProperties()) {
            if (contextualProperty != null && contextualProperty.getParentId() != null && parentCategoryId.equals(contextualProperty.getParentId())) {
                return contextualProperty;
            }
        }
        return null;
    }

    public String getCategoryName(ContextualProperty contextualProperty) {
        return (contextualProperty != null && !StringUtils.isEmpty(contextualProperty.getDesktopAlternateName())) ? contextualProperty.getDesktopAlternateName() : getName();
    }

    public String getBoutiqueTextAdornments(ContextualProperty contextualProperty) {
        return contextualProperty != null ? contextualProperty.getBoutiqueTextAdornments() : null;
    }

    public boolean isBoutiqueTextAdornmentsOverride(ContextualProperty contextualProperty) {
        return contextualProperty != null ? contextualProperty.isBoutiqueTextAdornmentsOverride() : false;
    }

    public boolean isRedTextAvailable(ContextualProperty contextualProperty) {
        return contextualProperty != null && contextualProperty.isRedText();
    }

    public List<String> getChildCategoryOrder(ContextualProperty contextualProperty) {
        List<String> resultedChildren = new ArrayList<>();
        if (contextualProperty != null && isNotEmpty(contextualProperty.getChildCategoryOrder())) {
            resultedChildren.addAll(contextualProperty.getChildCategoryOrder()
                    .stream()
                    .filter(childCatId -> getChildren().indexOf(childCatId) != -1)
                    .collect(Collectors.toList()));
            resultedChildren.addAll(getChildren().stream().filter(categoryId -> resultedChildren.indexOf(categoryId) == -1).collect(Collectors.toList()));
        } else {
            resultedChildren.addAll(getChildren());
        }
        return resultedChildren;
    }

    public String getDriveToSubCategoryId(ContextualProperty contextualProperty, LeftNavTreeProcessor leftNavTreeProcessor) {
        if (contextualProperty != null && !StringUtils.isEmpty(contextualProperty.getDriveToSubcategoryId())) {
            String driveToPath = StringUtils.replace(contextualProperty.getDriveToSubcategoryId(), ":", "_");
            if (isValidDriveToSubCategory(leftNavTreeProcessor, driveToPath)) {
                return driveToPath;
            }
        }
        return "";
    }

    private boolean isValidDriveToSubCategory(LeftNavTreeProcessor leftNavTreeProcessor, String driveToPath) {
        String[] catIds = driveToPath.split("_");
        if (catIds.length >= 2) {
            Optional<CategoryDocument> childCategoryDocument = leftNavTreeProcessor.getCategoryDocument(catIds[0]);
            Optional<CategoryDocument> grandChildCategoryDocument = leftNavTreeProcessor.getCategoryDocument(catIds[1]);

            return childCategoryDocument.isPresent() &&
                    isValidSubCategory(childCategoryDocument.get(), childCategoryDocument.get().getChildren(), catIds[1]) &&
                    grandChildCategoryDocument.isPresent() &&
                    isValidSubCategory(grandChildCategoryDocument.get(), getChildren(), catIds[0]);
        }
        Optional<CategoryDocument> childCategoryDocument = leftNavTreeProcessor.getCategoryDocument(catIds[0]);
        return childCategoryDocument.isPresent() && isValidSubCategory(childCategoryDocument.get(), getChildren(), catIds[0]);
    }

    public boolean isValidSubCategory(CategoryDocument categoryDocument, List<String> children, String driveToCatId) {
        return !categoryDocument.hasNoResultsOrIsHiddenOrIsDeleted() && !CollectionUtils.isEmpty(children) && children.stream().anyMatch(catId -> catId.equals(driveToCatId));
    }

    public boolean hasNoResultsOrIsHiddenOrIsDeleted() {
        return isNoResults() || isHidden() || isDeleted();
    }

    public Optional<String> getDriveToSubCategoryIds(ContextualProperty contextualProperty) {
        if (contextualProperty != null) {
            return Optional.ofNullable(contextualProperty.getDriveToSubcategoryId());
        }
        return Optional.empty();
    }
}

