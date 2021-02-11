package com.sixthday.navigation.api.mappers;

import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.models.Breadcrumb;
import com.sixthday.navigation.api.utils.BreadcrumbUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BreadcrumbMapper {

    private BreadcrumbUtil breadcrumbUtil;

    @Autowired
    public BreadcrumbMapper(final BreadcrumbUtil breadcrumbUtil) {
        this.breadcrumbUtil = breadcrumbUtil;
    }

    private Breadcrumb mapElasticSearchCategoryToWebBreadcrumb(final CategoryDocument categoryDocument, List<CategoryDocument> categoryDocuments, Boolean hasLiveRootCatIdInNavpath) {
        int categoryDocumentIndex = categoryDocuments.indexOf(categoryDocument);

        StringBuilder urlToBuild = new StringBuilder().append(breadcrumbUtil.buildBreadcrumbUrl(categoryDocuments, categoryDocumentIndex + 1, hasLiveRootCatIdInNavpath));
        final String url = breadcrumbUtil.appendSourceParam(urlToBuild.toString());

        Breadcrumb breadcrumb;
        if (categoryDocumentIndex - 1 >= 0) {
            String parentCategoryId = categoryDocuments.get(categoryDocumentIndex - 1).getId();
            breadcrumb = new Breadcrumb(categoryDocument.getId(), categoryDocument.getDesktopAlternateName(parentCategoryId), categoryDocument.getMobileAlternateName(parentCategoryId), url);
        } else {
            breadcrumb = new Breadcrumb(categoryDocument.getId(), categoryDocument.getDesktopAlternateName(breadcrumbUtil.getRootCategoryId()), categoryDocument.getMobileAlternateName(breadcrumbUtil.getRootCategoryId()), url);
        }
        return breadcrumb;
    }

    public List<Breadcrumb> mapElasticSearchCategoriesToBreadcrumbs(List<CategoryDocument> categoryDocuments, Boolean hasLiveRootCatIdInNavpath) {
        return categoryDocuments.stream().map(categoryDocument -> mapElasticSearchCategoryToWebBreadcrumb(categoryDocument, categoryDocuments, hasLiveRootCatIdInNavpath)).collect(Collectors.toList());
    }

}
