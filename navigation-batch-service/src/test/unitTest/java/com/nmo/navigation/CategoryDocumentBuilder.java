package com.sixthday.navigation;

import com.sixthday.navigation.domain.ContextualProperty;
import com.sixthday.navigation.elasticsearch.documents.CategoryDocument;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class CategoryDocumentBuilder {
    private String id = "Cat00001";
    private String displayName = "Test Handbags";

    private String name = "Test Handbags";
    private String templateType = "P3";
    private String leftNavImageAvailableOverride = "cat00001";

    private String alternateSeoName = "Alternative SEO Name";
    private String seoTitleOverride = "SEO Title Override";
    private String canonicalUrl = "/cat00001/c.cat";
    private String seoContentTitle = "SEO Content title";
    private String seoContentDescription = "SEO Content description";
    private String seoTags = "<meta name=\"description\" content=\"Add a bit of edge. Free shipping & free returns.\" />";

    private Map<String, Integer> parents = new HashMap<>();
    private String defaultPath = "cat00001";

    //Flags
    private boolean boutique;
    private boolean childBoutique;
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
    private List<String> type = singletonList("LIVE");

    private List<String> children = emptyList();
    private List<ContextualProperty> contextualProperties = emptyList();
    private List<String> excludedCountries = emptyList();
    private boolean isDeleted = false;

    public CategoryDocument build() {
        return new CategoryDocument(id, displayName,
                name, templateType, leftNavImageAvailableOverride, parents, defaultPath,
                alternateSeoName, seoTitleOverride, canonicalUrl, seoContentTitle, seoContentDescription, seoTags,
                boutique, childBoutique, imageAvailable, leftNavImageAvailable, expandCategory, dontShowChildren,
                personalized, hidden, noResults, displayAsGroups, driveToGroupPDP, excludeFromPCS, isDeleted, type, children,
                contextualProperties, excludedCountries);
    }

    public CategoryDocumentBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public CategoryDocumentBuilder withTypes(String... types) {
        this.type = asList(types);
        return this;
    }

    public CategoryDocumentBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CategoryDocumentBuilder withTemplateType(String templateType) {
        this.templateType = templateType;
        return this;
    }

    public CategoryDocumentBuilder withLeftNavImageAvailableOverride(String leftNavImageAvailableOverride) {
        this.leftNavImageAvailableOverride = leftNavImageAvailableOverride;
        return this;
    }

    public CategoryDocumentBuilder withCanonicalUrl(String canonicalUrl) {
        this.canonicalUrl = canonicalUrl;
        return this;
    }

    public CategoryDocumentBuilder withBoutique(boolean boutique) {
        this.boutique = boutique;
        return this;
    }

    public CategoryDocumentBuilder withExpandCategory(boolean expandCategory) {
        this.expandCategory = expandCategory;
        return this;
    }

    public CategoryDocumentBuilder withDontShowChildren(boolean dontShowChildren) {
        this.dontShowChildren = dontShowChildren;
        return this;
    }

    public CategoryDocumentBuilder withHidden(boolean hidden) {
        this.hidden = hidden;
        return this;
    }

    public CategoryDocumentBuilder withNoResults(boolean noResults) {
        this.noResults = noResults;
        return this;
    }

    public CategoryDocumentBuilder withChildren(List<String> children) {
        this.children = children;
        return this;
    }

    public CategoryDocumentBuilder withDeletedFlag(boolean isDeleted) {
        this.isDeleted = isDeleted;
        return this;
    }

    public CategoryDocumentBuilder withChildren(String... children) {
        this.children = Arrays.asList(children);
        return this;
    }

    public CategoryDocumentBuilder withContextualProperties(ContextualProperty... contextualProperties) {
        this.contextualProperties = Arrays.asList(contextualProperties);
        return this;
    }

    public CategoryDocumentBuilder withLeftNavImageAvailable(boolean leftNavImageAvailable) {
        this.leftNavImageAvailable = leftNavImageAvailable;
        return this;
    }

    public CategoryDocumentBuilder withParents(String... categoryIds) {
        asList(categoryIds).forEach(categoryId -> this.parents.put(categoryId, 1));
        return this;
    }

    public CategoryDocumentBuilder withDefaultPath(String defaultPath) {
        this.defaultPath = defaultPath;
        return this;
    }
}
