package com.sixthday.navigation.api.data;

import com.sixthday.navigation.api.controllers.elasticsearch.documents.CategoryDocumentBuilder;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.elasticsearch.models.ContextualProperty;
import com.sixthday.navigation.api.elasticsearch.models.SearchCriteria;
import com.sixthday.navigation.api.exceptions.CategoryNotFoundException;
import com.sixthday.navigation.api.models.Breadcrumb;
import com.sixthday.navigation.api.models.SisterSite;
import com.sixthday.navigation.api.models.response.BrandLinks;
import org.elasticsearch.action.get.MultiGetItemResponse;

import java.util.*;

import static java.util.Arrays.asList;

public class CategoryTestDataFactory {

    public static String CATEGORY_ID = "cat123";
    public static String CATEGORY_IDS = "cat2_cat1";
    public static String CATEGORY_IDS_WITH_ROOTID = "cat1_cat2_cat000000";
    public static List<String> CATEGORY_ID_LIST = asList("cat1", "cat2");
    public static String CATEGORY_INDEX = "category_index";
    public static String DOCUMENT_TYPE = "category";
    public static String BREADCRUMB_EXCEPTION_MSG = "Breadcrumb information is not available because category cat2_cat1 is not found.";
    public static String CATEGORY_EXCEPTION_MSG = CategoryNotFoundException.ERROR_MESSAGE + CATEGORY_ID;
    public static String CATEGORY_DOCUMENT_AS_STRING = "{\"id\":\"cat1\",\"children\":[\"sisCat\"],\"displayName\":\"Ralph Lauren\",\"url\":\"/Ralph-Lauren/Designers/cat1/c.cat\"}";
    public static String INVALID_CATEGORY_DOCUMENT_AS_STRING = "\"id\":\"cat1\",\"displayName\":\"Ralph Lauren\",\"url\":\"/Ralph-Lauren/Designers/cat1/c.cat\"";
    public static String DEFAULT_BREADCRUMB = "cat2_cat1";
    public static String SEO_FOOTER_CATEGORY_ID = "ftr000000";

    public static String rootCat = "cat000000";
    public static String stageCat = "cat400731";
    public static String marketingCat = "cat8900735";
    public static String topNavCat = "topNavCat";
    public static String catCouldBeReplaced = "catCouldBeReplaced";
    public static String someCat = "someCat";
    public static String catSelected = "catSelected";
    public static String catBoutique = "catBoutique";
    public static String catToDriveTo = "catToDriveTo";
    public static String catToDriveToSecond = "catToDriveToSecond";
    public static String CATEGORY_IDS_WITH_ROOTID_WITH_REPLACEABLE_CAT = rootCat + "_" + topNavCat + "_" + catCouldBeReplaced + "_" + someCat + "_" + catSelected;
    public static List<String> CATEGORY_ID_LIST_WITHOUT_ROOTID_WITH_REPLACEABLE_CAT = asList(topNavCat, catCouldBeReplaced, someCat, catSelected);
    public static String CATEGORY_IDS_WITH_ROOTID_WITH_REPLACEABLE_CAT_AND_BOUTIQUE_SELECTED = rootCat + "_" + topNavCat + "_" + "_" + catCouldBeReplaced + "_" + someCat + "_" + catSelected;
    public static List<String> CATEGORY_ID_LIST_WITH_ROOTID_WITH_REPLACEABLE_CAT_AND_BOUTIQUE_SELECTED = asList(topNavCat, catCouldBeReplaced, someCat, catSelected);
    public static String CATEGORY_IDS_WITH_ROOTID_WITH_REPLACEABLE_CAT_AND_BOUTIQUE_IN_HIERARCHY = rootCat + "_" + topNavCat + "_" + catCouldBeReplaced + "_" + catBoutique + "_" + someCat + "_" + catSelected;
    public static List<String> CATEGORY_IDS_LIST_WITHOUT_ROOTID_WITH_REPLACEABLE_CAT_AND_BOUTIQUE_IN_HIERARCHY = asList(topNavCat, catCouldBeReplaced, catBoutique, someCat, catSelected);
    public static String CATEGORY_DESIGNER = "designerCat";
    public static String DESIGNER_CATEGORY_IDS = "cat02_cat03_cat01_cat02";
    public static List<String> ITERABLE_DESIGNER_CATEGORY_IDS = asList(DESIGNER_CATEGORY_IDS.split(","));

    public static List<Breadcrumb> getTestBreadcrumbs() {
        List<Breadcrumb> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(new Breadcrumb("idCat1", "nameCat1", "", "/nameCat1/idCat1/c.cat"));
        breadcrumbs.add(new Breadcrumb("idCat2", "nameCat2", "", "/nameCat1/nameCat2/idCat2_idCat1/c.cat"));
        return breadcrumbs;
    }

    public static List<Breadcrumb> getDefaultTestBreadcrumbs() {
        List<Breadcrumb> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(new Breadcrumb("idCat1", "nameCat1", "", "/nameCat1/idCat1/c.cat?navpath=idCat0000_idCat1"));
        breadcrumbs.add(new Breadcrumb("idCat2", "nameCat2", "", "/nameCat1/nameCat2/idCat2_idCat1/c.cat?navpath=idCat0000_idCat1_idCat2"));
        return breadcrumbs;
    }

    public static List<Breadcrumb> getInvalidTestBreadcrumbs() {
        List<Breadcrumb> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(new Breadcrumb("idCat1", "nameCat1", "", "/nameCat1/idCat1/c.cat"));
        return breadcrumbs;
    }

    public static Breadcrumb getTestBreadcrumb() {
        return new Breadcrumb("idCat1", "nameCat1", "", "/cat1/cat.c");
    }

    public static List<CategoryDocument> getEmptyTestCategoryDocuments() {
        List<CategoryDocument> categoryDocuments = new ArrayList<>();
        return categoryDocuments;
    }

    public static List<CategoryDocument> getTestCategoryDocuments() {
        List<CategoryDocument> categoryDocuments = new ArrayList<>();
        ContextualProperty contextualProperty = new ContextualProperty(false, false, "cat000000", "desktopAlternateName", "mobileAlternateName", null, null, null, null, null);
        CategoryDocument category1 = new CategoryDocumentBuilder()
                .withId("idCat1")
                .withName("nameCat1")
                .withContextualProperties(Arrays.asList(contextualProperty))
                .build();
        CategoryDocument category2 = new CategoryDocumentBuilder()
                .withId("idCat2")
                .withName("nameCat2")
                .build();
        CategoryDocument category3 = new CategoryDocumentBuilder()
                .withId("parentCat00001")
                .withName("nameCat3")
                .withContextualProperties(new ArrayList<>())
                .build();
        CategoryDocument category4 = new CategoryDocumentBuilder()
                .withId("idCat3")
                .withName("nameCat4")
                .build();

        categoryDocuments.add(category1);
        categoryDocuments.add(category2);
        categoryDocuments.add(category3);
        categoryDocuments.add(category4);

        return categoryDocuments;
    }

    public static CategoryDocument getTestCategoryDocument(String id) {
        return new CategoryDocumentBuilder().withId(id).build();
    }

    public static MultiGetItemResponse[] getTestMultiGetItemResponse(MultiGetItemResponse multiGetItemResponse) {
        MultiGetItemResponse[] multiGetItemResponses = new MultiGetItemResponse[2];
        multiGetItemResponses[0] = multiGetItemResponse;
        multiGetItemResponses[1] = multiGetItemResponse;

        return multiGetItemResponses;
    }

    public static Map<CategoryDocument, List<CategoryDocument>> brandLinksCategories() {

        Map<CategoryDocument, List<CategoryDocument>> brandLinks = new HashMap<>();

        List<CategoryDocument> categoryDocuments = new ArrayList<>();

        CategoryDocument parentCategory1 = new CategoryDocumentBuilder()
                .withName("parent1")
                .withLongDescription("/url1")
                .build();

        CategoryDocument childCategory1 = new CategoryDocumentBuilder()
                .withName("child1")
                .withLongDescription("/urlc1")
                .build();
        CategoryDocument childCategory2 = new CategoryDocumentBuilder()
                .withName("child2")
                .withLongDescription("/urlc2")
                .build();

        categoryDocuments.add(childCategory1);
        categoryDocuments.add(childCategory2);

        brandLinks.put(parentCategory1, categoryDocuments);

        return brandLinks;
    }

    public static BrandLinks expectedBrandLinks() {
        BrandLinks brandLinks = new BrandLinks();

        List sisterSites = new ArrayList<>();

        sisterSites.add(new SisterSite("parent1", "/url1",
                asList(new SisterSite.TopCategory("child1", "/urlc1"),
                        new SisterSite.TopCategory("child2", "/urlc2"))));

        brandLinks.setSisterSites(sisterSites);
        return brandLinks;
    }

    public static List<CategoryDocument> getCategoryDocumentsList() {
        List<CategoryDocument> categoryDocumentList = new ArrayList<>();
        categoryDocumentList.add(getCategoryDocumentTopNav());
        categoryDocumentList.add(getCategoryDocumentThatCouldBeReplaced());
        categoryDocumentList.add(getCategoryDocumentThatWasSelectedByUser());
        return categoryDocumentList;
    }

    public static CategoryDocument getDesignerCategoryDocument() {
        return new CategoryDocumentBuilder()
                .withId(CATEGORY_DESIGNER)
                .withDisplayName("Designer")
                .withCanonicalUrl("/Designer")
                .build();
    }

    public static List<CategoryDocument> getCategoryDocumentListWithBoutiqueTrueInHierarchyAndSelectedWithDriveToSubCat() {
        List<CategoryDocument> categoryDocumentListWithBoutiqueTrue = new ArrayList<>();
        categoryDocumentListWithBoutiqueTrue.add(getCategoryDocumentTopNav());
        categoryDocumentListWithBoutiqueTrue.add(getCategoryDocumentThatCouldBeReplaced());
        categoryDocumentListWithBoutiqueTrue.add(getCategoryDocumentWithBoutiqueTrue());
        categoryDocumentListWithBoutiqueTrue.add(getTestCategoryDocument(someCat));
        categoryDocumentListWithBoutiqueTrue.add(getCategoryDocumentThatWasSelectedByUserWithContextPropDriveToSubCat());
        return categoryDocumentListWithBoutiqueTrue;
    }

    public static List<CategoryDocument> getCategoryDocumentListWithSelectedAsBoutiqueTrue() {
        List<CategoryDocument> categoryDocumentListWithSelectedAsBoutiqueTrue = new ArrayList<>();
        categoryDocumentListWithSelectedAsBoutiqueTrue.add(getCategoryDocumentTopNav());
        categoryDocumentListWithSelectedAsBoutiqueTrue.add(getCategoryDocumentThatCouldBeReplaced());
        categoryDocumentListWithSelectedAsBoutiqueTrue.add(getCategoryDocumentThatWasSelectedByUserWithBoutiqueTrue());
        return categoryDocumentListWithSelectedAsBoutiqueTrue;
    }

    public static List<CategoryDocument> getCategoryDocumentListWithDriveToSubCatContextProp() {
        List<CategoryDocument> categoryDocumentListWithDriveToSubCatContextProp = new ArrayList<>();
        categoryDocumentListWithDriveToSubCatContextProp.add(getCategoryDocumentTopNav());
        categoryDocumentListWithDriveToSubCatContextProp.add(getCategoryDocumentThatCouldBeReplaced());
        categoryDocumentListWithDriveToSubCatContextProp.add(getTestCategoryDocument("someCat"));
        categoryDocumentListWithDriveToSubCatContextProp.add(getCategoryDocumentThatWasSelectedByUserWithContextPropDriveToSubCat());
        return categoryDocumentListWithDriveToSubCatContextProp;
    }

    public static List<CategoryDocument> getCategoryDocumentListModifiedWithDriveToSubCatCategories() {
        List<CategoryDocument> categoryDocumentListModifiedWithDriveToSubCatCategories = new ArrayList<>();
        categoryDocumentListModifiedWithDriveToSubCatCategories.add(getCategoryDocumentTopNav());
        categoryDocumentListModifiedWithDriveToSubCatCategories.add(getCategoryDocumentThatCouldBeReplaced());
        categoryDocumentListModifiedWithDriveToSubCatCategories.add(getTestCategoryDocument("someCat"));
        categoryDocumentListModifiedWithDriveToSubCatCategories.add(getCategoryDocumentThatWasSelectedByUserWithContextPropDriveToSubCat());
        categoryDocumentListModifiedWithDriveToSubCatCategories.add(getCategoryDocumentDriveTo());
        categoryDocumentListModifiedWithDriveToSubCatCategories.add(getCategoryDocumentDriveToSecond());
        return categoryDocumentListModifiedWithDriveToSubCatCategories;
    }

    public static List<CategoryDocument> getCategoryDocumentListModifiedWithDesignerCategoryAndBoutiqueSelected() {
        List<CategoryDocument> categoryDocumentsListModifiedWithDesignerCategory = new ArrayList<>();
        categoryDocumentsListModifiedWithDesignerCategory.add(getCategoryDocumentThatIsReplacing());
        categoryDocumentsListModifiedWithDesignerCategory.add(getCategoryDocumentThatWasSelectedByUserWithBoutiqueTrue());
        return categoryDocumentsListModifiedWithDesignerCategory;
    }

    public static List<CategoryDocument> getCategoryDocumentListModifiedWithDesignerCategory() {
        List<CategoryDocument> categoryDocumentListModifiedWithDesignerCategory = new ArrayList<>();
        categoryDocumentListModifiedWithDesignerCategory.add(getDesignerCategoryDocument());
        categoryDocumentListModifiedWithDesignerCategory.add(getCategoryDocumentWithBoutiqueTrue());
        categoryDocumentListModifiedWithDesignerCategory.add(getTestCategoryDocument(someCat));
        categoryDocumentListModifiedWithDesignerCategory.add(getCategoryDocumentThatWasSelectedByUser());
        return categoryDocumentListModifiedWithDesignerCategory;
    }

    public static List<CategoryDocument> getCategoryDocumentListWithBoutiqueTrueAndDriveToSubCatContextProp() {
        List<CategoryDocument> categoryDocumentListWithBoutiqueTrueAndDriveToSubCatContextProp = new ArrayList<>();
        categoryDocumentListWithBoutiqueTrueAndDriveToSubCatContextProp.add(getCategoryDocumentTopNav());
        categoryDocumentListWithBoutiqueTrueAndDriveToSubCatContextProp.add(getCategoryDocumentThatCouldBeReplaced());
        categoryDocumentListWithBoutiqueTrueAndDriveToSubCatContextProp.add(getTestCategoryDocument(someCat));
        categoryDocumentListWithBoutiqueTrueAndDriveToSubCatContextProp.add(getCategoryDocumentThatWasSelectedByUserWithBoutiqueTrueAndContextPropDriveToSubCat());
        return categoryDocumentListWithBoutiqueTrueAndDriveToSubCatContextProp;
    }

    public static List<CategoryDocument> getCategoryDocumentListWithDesignerCatAndBoutiqueCategoryAndDriveToSubCat() {
        List<CategoryDocument> categoryDocumentListWithDesignerCatAndBoutiqueCategoryAndDriveToSubCat = new ArrayList<>();
        categoryDocumentListWithDesignerCatAndBoutiqueCategoryAndDriveToSubCat.add(getDesignerCategoryDocument());
        categoryDocumentListWithDesignerCatAndBoutiqueCategoryAndDriveToSubCat.add(getCategoryDocumentWithBoutiqueTrue());
        categoryDocumentListWithDesignerCatAndBoutiqueCategoryAndDriveToSubCat.add(getTestCategoryDocument(someCat));
        categoryDocumentListWithDesignerCatAndBoutiqueCategoryAndDriveToSubCat.add(getCategoryDocumentThatWasSelectedByUserWithContextPropDriveToSubCat());
        categoryDocumentListWithDesignerCatAndBoutiqueCategoryAndDriveToSubCat.add(getCategoryDocumentDriveTo());
        categoryDocumentListWithDesignerCatAndBoutiqueCategoryAndDriveToSubCat.add(getCategoryDocumentDriveToSecond());
        return categoryDocumentListWithDesignerCatAndBoutiqueCategoryAndDriveToSubCat;
    }

    public static List<CategoryDocument> getCategoryDocumentListWithDesignerCatAndBoutiqueCategoryInHierarchyAndDriveToSubCat() {
        List<CategoryDocument> categoryDocumentListWithDesignerCatAndBoutiqueCategoryAndDriveToSubCat = new ArrayList<>();
        categoryDocumentListWithDesignerCatAndBoutiqueCategoryAndDriveToSubCat.add(getDesignerCategoryDocument());
        categoryDocumentListWithDesignerCatAndBoutiqueCategoryAndDriveToSubCat.add(getCategoryDocumentThatWasSelectedByUserWithBoutiqueTrueAndContextPropDriveToSubCat());
        categoryDocumentListWithDesignerCatAndBoutiqueCategoryAndDriveToSubCat.add(getCategoryDocumentDriveTo());
        categoryDocumentListWithDesignerCatAndBoutiqueCategoryAndDriveToSubCat.add(getCategoryDocumentDriveToSecond());
        return categoryDocumentListWithDesignerCatAndBoutiqueCategoryAndDriveToSubCat;
    }

    public static List<CategoryDocument> getCategoryDocumentListWithBoutiqueCategoryInHierarchy() {
        List<CategoryDocument> categoryDocumentListWithBoutiqueCategoryInHierarchy = new ArrayList<>();
        categoryDocumentListWithBoutiqueCategoryInHierarchy.add(getCategoryDocumentTopNav());
        categoryDocumentListWithBoutiqueCategoryInHierarchy.add(getCategoryDocumentThatCouldBeReplaced());
        categoryDocumentListWithBoutiqueCategoryInHierarchy.add(getCategoryDocumentWithBoutiqueTrue());
        categoryDocumentListWithBoutiqueCategoryInHierarchy.add(getTestCategoryDocument(someCat));
        categoryDocumentListWithBoutiqueCategoryInHierarchy.add(getCategoryDocumentThatWasSelectedByUser());
        return categoryDocumentListWithBoutiqueCategoryInHierarchy;
    }

    public static CategoryDocument getCategoryDocumentWithBoutiqueTrue() {
        CategoryDocument catThatIsBoutique = new CategoryDocumentBuilder()
                .withId(catBoutique)
                .withDisplayName("Gucci")
                .withBoutique(true)
                .withCanonicalUrl("designerCat/Gucci/c.cat")
                .withContextualProperties(Collections.emptyList())
                .build();

        return catThatIsBoutique;
    }

    public static CategoryDocument getCategoryDocumentThatWasSelectedByUserWithBoutiqueTrue() {
        CategoryDocument catThatWasSelectedByUser = new CategoryDocumentBuilder()
                .withId(catSelected)
                .withDisplayName("Gucci")
                .withBoutique(true)
                .withCanonicalUrl("designerCat/Gucci/c.cat")
                .withContextualProperties(Collections.emptyList())
                .build();

        return catThatWasSelectedByUser;
    }

    public static CategoryDocument getCategoryDocumentThatWasSelectedByUser() {
        CategoryDocument catThatWasSelectedByUser = new CategoryDocumentBuilder()
                .withId(catSelected)
                .withDisplayName("Gucci")
                .withBoutique(false)
                .withCanonicalUrl(CATEGORY_DESIGNER + "/Gucci/c.cat")
                .withContextualProperties(Collections.emptyList())
                .build();

        return catThatWasSelectedByUser;
    }

    public static CategoryDocument getCategoryDocumentThatWasSelectedByUserWithContextPropDriveToSubCat() {
        List<String> childCategoryOrder = new ArrayList<>();
        String driveToSubcategoryId = catToDriveTo + ":" + catToDriveToSecond;
        ContextualProperty driveToSubCat = new ContextualProperty(false, false, someCat, "", "", driveToSubcategoryId, null, null, null, childCategoryOrder);
        CategoryDocument catThatWasSelectedByUser = new CategoryDocumentBuilder()
                .withId(catSelected)
                .withDisplayName("Gucci")
                .withBoutique(false)
                .withCanonicalUrl(CATEGORY_DESIGNER + "/Gucci/c.cat")
                .withContextualProperties(asList(driveToSubCat))
                .build();

        return catThatWasSelectedByUser;
    }

    public static CategoryDocument getCategoryDocumentThatWasSelectedByUserWithBoutiqueTrueAndContextPropDriveToSubCat() {
        List<String> childCategoryOrder = new ArrayList<>();
        ContextualProperty driveToSubCat = new ContextualProperty(false, false, someCat, "", "", catToDriveTo + ":" + catToDriveToSecond, null, null, null, childCategoryOrder);
        CategoryDocument catThatWasSelectedByUser = new CategoryDocumentBuilder()
                .withId(catSelected)
                .withDisplayName("Gucci")
                .withBoutique(true)
                .withCanonicalUrl(CATEGORY_DESIGNER + "/Gucci/c.cat")
                .withContextualProperties(asList(driveToSubCat))
                .build();

        return catThatWasSelectedByUser;
    }

    public static CategoryDocument getCategoryDocumentThatIsReplacing() {
        CategoryDocument catThatIsReplacing = new CategoryDocumentBuilder()
                .withId(CATEGORY_DESIGNER)
                .withDisplayName("Designers")
                .withBoutique(false)
                .withCanonicalUrl(CATEGORY_DESIGNER + "/c.cat")
                .withContextualProperties(Collections.emptyList())
                .build();

        return catThatIsReplacing;
    }

    public static CategoryDocument getCategoryDocumentThatCouldBeReplaced() {
        CategoryDocument catThatCouldBeReplaced = new CategoryDocumentBuilder()
                .withId(catCouldBeReplaced)
                .withDisplayName("Could Replace This Cat")
                .withBoutique(false)
                .withCanonicalUrl("featuredDesignerCat/c.cat")
                .withContextualProperties(Collections.emptyList())
                .build();

        return catThatCouldBeReplaced;
    }

    public static CategoryDocument getCategoryDocumentTopNav() {
        CategoryDocument categoryTopNav = new CategoryDocumentBuilder()
                .withId(topNavCat)
                .withDisplayName("Women's Apparel")
                .withBoutique(false)
                .withCanonicalUrl("topNav/c.cat")
                .withContextualProperties(Collections.emptyList())
                .build();

        return categoryTopNav;
    }

    public static CategoryDocument getCategoryDocumentDriveTo() {
        CategoryDocument catDocToDriveTo = new CategoryDocumentBuilder()
                .withId(catToDriveTo)
                .withDisplayName("Driving To Cat")
                .withBoutique(false)
                .withCanonicalUrl(catToDriveTo + "/c.cat")
                .build();

        return catDocToDriveTo;
    }

    public static CategoryDocument getCategoryDocumentDriveToSecond() {
        CategoryDocument catDocToDriveToSecond = new CategoryDocumentBuilder()
                .withId(catToDriveToSecond)
                .withDisplayName("Driving To Cat Second")
                .withBoutique(false)
                .withCanonicalUrl(catToDriveToSecond + "/c.cat")
                .build();

        return catDocToDriveToSecond;
    }

    public static List<CategoryDocument> getTestFeaturedDesignerDocuments() {
        List<CategoryDocument> categoryDocuments = new ArrayList<>();
        ContextualProperty contextualProperty = new ContextualProperty(false, false, "cat03", "desktopAlternateName", "mobileAlternateName", null, null, null, null, null);
        CategoryDocument designerNameDocument = new CategoryDocumentBuilder()
                .withId("cat01")
                .withBoutique(true)
                .withDisplayName("Designer Name")
                .withContextualProperties(Arrays.asList(contextualProperty))
                .withCanonicalUrl("/url1")
                .build();

        CategoryDocument subCategoryDocument = new CategoryDocumentBuilder()
                .withId("cat02")
                .withDisplayName("shoes")
                .withCanonicalUrl("/url2")
                .withBoutique(false)
                .build();

        CategoryDocument excludedCategory = new CategoryDocumentBuilder()
                .withId("cat03")
                .withDisplayName("Excludable")
                .withName("Excludable")
                .withBoutique(false)
                .withCanonicalUrl("/url3")
                .build();
        categoryDocuments.add(subCategoryDocument);
        categoryDocuments.add(excludedCategory);
        categoryDocuments.add(designerNameDocument);
        categoryDocuments.add(subCategoryDocument);

        return categoryDocuments;
    }

    public static List<Breadcrumb> getTestFeaturedBreadcrumbs() {
        List<Breadcrumb> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(new Breadcrumb(CATEGORY_DESIGNER, "Designer", "", "/Designer/cat000730/c.cat"));
        breadcrumbs.add(new Breadcrumb("cat01", "Designer Name", "", "/Designer/url1/cat01_cat000730/c.cat"));
        breadcrumbs.add(new Breadcrumb("cat02", "shoes", "", "/Designer/url1/url2/cat02_cat01_cat000730/c.cat"));
        return breadcrumbs;
    }

    public static CategoryDocument getCategoryDocumentWithEmptySearchCriteria() {
        CategoryDocument categoryWithoutAttributesAndDimensions = new CategoryDocumentBuilder()
                .withId(catBoutique)
                .withDisplayName("Gucci")
                .withSearchCriteria(new SearchCriteria())
                .build();

        return categoryWithoutAttributesAndDimensions;
    }

}