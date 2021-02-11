package com.sixthday.navigation;

import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;

import java.util.Arrays;
import java.util.List;

public class CategoryTestDataFactory {

    public static CategoryDocument seoCategoryDocument() {
        CategoryDocument categoryDocument = new CategoryDocument();
        categoryDocument.setId("ftr000000");
        categoryDocument.setChildren(Arrays.asList("cat1"));
        return categoryDocument;
    }

    public static CategoryDocument sisterLinksDocument() {
        CategoryDocument categoryDocument = new CategoryDocument();
        categoryDocument.setId("cat1");
        categoryDocument.setName("sisterSite 1");
        categoryDocument.setLongDescription("/url");
        categoryDocument.setChildren(Arrays.asList("topcat"));
        return categoryDocument;
    }

    public static List<CategoryDocument> topCategories() {
        CategoryDocument topCategory = new CategoryDocument();
        topCategory.setId("topcat");
        topCategory.setName("top cat");
        topCategory.setLongDescription("/top-cat-url");
        return Arrays.asList(topCategory);
    }

    public static CategoryDocument sisterLinksDocumentWithNoChildCategories() {
        CategoryDocument categoryDocument = new CategoryDocument();
        categoryDocument.setId("cat1");
        categoryDocument.setName("sisterSite 1");
        categoryDocument.setLongDescription("/url");
        return categoryDocument;
    }

    public static List<CategoryDocument> emptyCategoryList() {
        return Arrays.asList(new CategoryDocument());
    }

    public static CategoryDocument seoCategoryDocumentWithNoChildcategories() {
        CategoryDocument categoryDocument = new CategoryDocument();
        categoryDocument.setId("ftr000000");
        return categoryDocument;
    }

}
