package com.sixthday.navigation.api.controllers.elasticsearch.documents;

import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.elasticsearch.models.*;
import com.sixthday.navigation.api.models.SearchCriteriaBuilder;

import java.util.*;

public class CategoryDocumentBuilder {
    private String id = "Cat00001";
    private String displayName = "Test Handbags";

    private String name = "Test Handbags";
    private String leftNavImageAvailableOverride = "cat00001";
    private String templateType = "template_type";
    private String longDescription = "longDescription";
    private String firstSellableProductImageUrl = "http://some-product-shot-url.jpg";
    private Map<String, Integer> parents = new HashMap<>();

    private String alternateSeoName = "Alternative SEO Name";
    private String seoTitleOverride = "SEO Title Override";
    private String canonicalUrl = "/c/nameCat1-idCat1";
    private String seoContentTitle = "SEO Content title";
    private String seoContentDescription = "SEO Content description";
    private String seoTags = "<meta name=\"description\" content=\"Add a bit of edge. Free shipping & free returns.\" />";
    private String defaultPath = "/nameCat1/nameCat2/idCat2_idCat1/c.cat";

    //Flags
    private boolean boutique;
    private boolean boutiqueChild;
    private boolean imageAvailable;
    private boolean mobileHideEntrySubcats;
    private boolean htmlAvailable;
    private boolean expandCategory;
    private boolean dontShowChildren;
    private boolean personalized;
    private boolean hidden;
    private boolean noResults;
    private boolean displayAsGroups;
    private boolean driveToGroupPDP;
    private boolean excludeFromPCS;
    private boolean showAllProducts = true;
    private boolean hideMobileImage;
    private boolean includeAllItems;
    private String thumbImageShot = "z";

    private SearchCriteria searchCriteria = new SearchCriteriaBuilder().build();
    private List<String> cmosCatalogCodes = new ArrayList<>(Arrays.asList(new String[]{"NMID1", "NMID2"}));
    private Integer newArrivalLimit = 1;
    private List<String> preferredProductIds = new ArrayList<>(Arrays.asList(new String[]{"prod1234", "prod1235", "prod1236"}));
    private List<String> children = new ArrayList<>();
    private List<String> type = new ArrayList<>();
    private List<String> childCategoryOrder = new ArrayList<>();
    private String driveToSubcategoryId = "";
    private String imageOverrideCategoryId = "overrideCatId";
    private List<Filter> applicableFilters = new ArrayList<>();
    private List<String> excludedCountries = new ArrayList<>();

    //Contextual Properties
    private ContextualProperty contextualProperty1 = new ContextualProperty(false, false, "parentCat00001", "DesktopAlternateName", "MobileAlternateName", driveToSubcategoryId, null, null, null, childCategoryOrder);
    private ContextualProperty contextualProperty2 = new ContextualProperty(false, false, "idCat1", "", "", driveToSubcategoryId, null, null, null, childCategoryOrder);
    private List<ContextualProperty> contextualProperties = Arrays.asList(contextualProperty1, contextualProperty2);
    private ProductRefinements productRefinements = new ProductRefinementsBuilder().build();
    private String redirectTo = "cat123";
    private String redirectType = "301";
    private boolean isDeleted = false;

    public CategoryDocument build() {
        return new CategoryDocument(id, displayName, name, leftNavImageAvailableOverride, templateType, defaultPath, firstSellableProductImageUrl, longDescription, parents,
                alternateSeoName, seoTitleOverride, canonicalUrl, seoContentTitle, seoContentDescription, seoTags,
                boutique, boutiqueChild, imageAvailable, mobileHideEntrySubcats, htmlAvailable, expandCategory, dontShowChildren, personalized, hidden,
                noResults, displayAsGroups, driveToGroupPDP, excludeFromPCS, showAllProducts, includeAllItems, hideMobileImage,
                thumbImageShot, searchCriteria, newArrivalLimit, cmosCatalogCodes, preferredProductIds, contextualProperties,
                productRefinements, children, type, imageOverrideCategoryId, applicableFilters, excludedCountries, redirectType, redirectTo, isDeleted);
    }

    public CategoryDocumentBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public CategoryDocumentBuilder withCmosCatalogCodes(List<String> cmosCatalogCodes) {
        this.cmosCatalogCodes = cmosCatalogCodes;
        return this;
    }

    public CategoryDocumentBuilder withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public CategoryDocumentBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CategoryDocumentBuilder withLongDescription(String longDescription) {
        this.longDescription = longDescription;
        return this;
    }

    public CategoryDocumentBuilder withLeftNavImageAvailableOverride(String leftNavImageAvailableOverride) {
        this.leftNavImageAvailableOverride = leftNavImageAvailableOverride;
        return this;
    }

    public CategoryDocumentBuilder withAlternateSeoName(String alternateSeoName) {
        this.alternateSeoName = alternateSeoName;
        return this;
    }

    public CategoryDocumentBuilder withSeoTitleOverride(String seoTitleOverride) {
        this.seoTitleOverride = seoTitleOverride;
        return this;
    }

    public CategoryDocumentBuilder withCanonicalUrl(String canonicalUrl) {
        this.canonicalUrl = canonicalUrl;
        return this;
    }

    public CategoryDocumentBuilder withSeoContentTitle(String seoContentTitle) {
        this.seoContentTitle = seoContentTitle;
        return this;
    }

    public CategoryDocumentBuilder withSearchCriteria(SearchCriteria searchCriteria) {
        this.searchCriteria = searchCriteria;

        return this;
    }

    public CategoryDocumentBuilder withSeoContentDescription(String seoContentDescription) {
        this.seoContentDescription = seoContentDescription;
        return this;
    }

    public CategoryDocumentBuilder withSeoTags(String seoTags) {
        this.seoTags = seoTags;
        return this;
    }

    public CategoryDocumentBuilder withBoutique(boolean boutique) {
        this.boutique = boutique;
        return this;
    }

    public CategoryDocumentBuilder withImageAvailable(boolean imageAvailable) {
        this.imageAvailable = imageAvailable;
        return this;
    }

    public CategoryDocumentBuilder withHtmlAvailable(boolean htmlAvailable) {
        this.htmlAvailable = htmlAvailable;
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

    public CategoryDocumentBuilder withPersonalized(boolean personalized) {
        this.personalized = personalized;
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

    public CategoryDocumentBuilder withDisplayAsGroups(boolean displayAsGroups) {
        this.displayAsGroups = displayAsGroups;
        return this;
    }

    public CategoryDocumentBuilder withDriveToGroupPDP(boolean driveToGroupPDP) {
        this.driveToGroupPDP = driveToGroupPDP;
        return this;
    }

    public CategoryDocumentBuilder withExcludeFromPCS(boolean excludeFromPCS) {
        this.excludeFromPCS = excludeFromPCS;
        return this;
    }

    public CategoryDocumentBuilder withShowAllProducts(boolean showAllProducts) {
        this.showAllProducts = showAllProducts;
        return this;
    }

    public CategoryDocumentBuilder withNewArrivalLimit(Integer newArrivalLimit) {
        this.newArrivalLimit = newArrivalLimit;
        return this;
    }

    public CategoryDocumentBuilder withThumbImageShot(String thumbImageShot) {
        this.thumbImageShot = thumbImageShot;
        return this;
    }

    public CategoryDocumentBuilder withContextualProperties(List<ContextualProperty> contextualProperties) {
        this.contextualProperties = contextualProperties;
        return this;
    }

    public CategoryDocumentBuilder withDefaultPath(String defaultPath) {
        this.defaultPath = defaultPath;
        return this;
    }

    public CategoryDocumentBuilder withPreferredProductIds(List<String> preferredProductIds) {
        this.preferredProductIds = preferredProductIds;
        return this;
    }

    public CategoryDocumentBuilder withImageOverrideCategoryId(String imageOverrideCategoryId) {
        this.imageOverrideCategoryId = imageOverrideCategoryId;
        return this;
    }

    public CategoryDocumentBuilder withHideMobileImage(boolean hideMobileImage) {
        this.hideMobileImage = hideMobileImage;
        return this;
    }

    public CategoryDocumentBuilder withApplicableFilters(List<Filter> applicableFilters) {
        this.applicableFilters = applicableFilters;
        return this;
    }

    public CategoryDocumentBuilder withExcludedCountries(List<String> excludedCountries) {
        this.excludedCountries = excludedCountries;
        return this;
    }

    public CategoryDocumentBuilder withProductRefinements(ProductRefinements productRefinements) {
        this.productRefinements = productRefinements;

        return this;
    }

    public CategoryDocumentBuilder withRedirectType(String redirectType) {
        this.redirectType = redirectType;

        return this;
    }

    public CategoryDocumentBuilder withRedirectTo(String redirectTo) {
        this.redirectTo = redirectTo;

        return this;
    }

    public CategoryDocumentBuilder withDeletedFlag(boolean deleted) {
        this.isDeleted = deleted;

        return this;
    }
    
    public CategoryDocumentBuilder withFilter(String name, String altName, List<String> disabledVals, List<String> values) {
      Filter filter = new Filter(name, altName, disabledVals, values);
      this.applicableFilters.add(filter);
      return this;
    }
}
