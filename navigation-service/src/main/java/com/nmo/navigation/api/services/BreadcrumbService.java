package com.sixthday.navigation.api.services;

import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.api.elasticsearch.models.ContextualProperty;
import com.sixthday.navigation.api.elasticsearch.repository.CategoryRepository;
import com.sixthday.navigation.api.exceptions.BreadcrumbNotFoundException;
import com.sixthday.navigation.api.exceptions.CategoryNotFoundException;
import com.sixthday.navigation.api.mappers.BreadcrumbMapper;
import com.sixthday.navigation.api.models.Breadcrumb;
import com.sixthday.navigation.api.utils.BreadcrumbUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.sixthday.navigation.config.Constants.SEPARATOR;
import static com.sixthday.navigation.config.Constants.SOURCE_LEFT_NAV;
import static com.sixthday.navigation.config.Constants.TEST_CATEGORY_GROUP;

@Service
public class BreadcrumbService {
    private CategoryRepository categoryRepository;
    private BreadcrumbMapper breadcrumbMapper;
    private BreadcrumbUtil breadcrumbUtil;
    private NavigationServiceConfig navigationServiceConfig;

    @Autowired
    public BreadcrumbService(final CategoryRepository categoryRepository, final BreadcrumbMapper breadcrumbMapper, final BreadcrumbUtil breadcrumbUtil, NavigationServiceConfig navigationServiceConfig) {
        this.categoryRepository = categoryRepository;
        this.breadcrumbMapper = breadcrumbMapper;
        this.breadcrumbUtil = breadcrumbUtil;
        this.navigationServiceConfig = navigationServiceConfig;
    }

    public List<Breadcrumb> getBreadcrumbs(String navPath, String source, String navKeyGroup) {

        String backwardCompatiblePath = buildBackwardCompatiblePath(navPath);
        String breadcrumbCategoryIds = backwardCompatiblePath.contains(SEPARATOR) ? backwardCompatiblePath : getDefaultPath(backwardCompatiblePath, navKeyGroup);
        final List<String> categoryIdList = breadcrumbUtil.convertToListAndRemoveRootCategory(breadcrumbCategoryIds, navKeyGroup);

        final List<CategoryDocument> originalCategoryDocuments;
        try {
            originalCategoryDocuments = categoryRepository.getCategoryDocuments(categoryIdList);
        } catch (CategoryNotFoundException e) {
            throw new BreadcrumbNotFoundException(String.join(",", categoryIdList), e);
        }

        List<String> originalCategoryIds = originalCategoryDocuments.stream().map(CategoryDocument::getId).collect(Collectors.toList());

        List<CategoryDocument> categoryDocumentsToBeMappedToBreadcrumbs = new ArrayList<>();

        List<CategoryDocument> driveToSubCatDocuments = getDriveToSubCategoryDocumentList(categoryIdList);

        List<CategoryDocument> boutiqueCategoryDocuments = originalCategoryDocuments.stream().filter(CategoryDocument::isBoutique).collect(Collectors.toList());
        List<String> boutiqueCategoryIds = boutiqueCategoryDocuments.stream().map(CategoryDocument::getId).collect(Collectors.toList());

        if (!boutiqueCategoryDocuments.isEmpty()) {
            List<CategoryDocument> categoryDocumentsWithReplacedCategory = getBoutiqueCategoryDocumentListWithReplacedCategory(boutiqueCategoryDocuments);
            categoryDocumentsWithReplacedCategory.addAll(getRemainingCategoriesInOriginalCategoryDocumentList(originalCategoryDocuments, boutiqueCategoryDocuments));
            if (!driveToSubCatDocuments.isEmpty() && !SOURCE_LEFT_NAV.equals(source)) {
                categoryDocumentsWithReplacedCategory.addAll(driveToSubCatDocuments);
            }
            categoryDocumentsToBeMappedToBreadcrumbs.addAll(categoryDocumentsWithReplacedCategory);
        } else {
            categoryDocumentsToBeMappedToBreadcrumbs.addAll(originalCategoryDocuments);
            if (!SOURCE_LEFT_NAV.equals(source)) {
                categoryDocumentsToBeMappedToBreadcrumbs.addAll(driveToSubCatDocuments);
            }
        }

        List<Breadcrumb> breadcrumbs = breadcrumbMapper.mapElasticSearchCategoriesToBreadcrumbs(categoryDocumentsToBeMappedToBreadcrumbs, hasLiveRootCatIdInNavPath(backwardCompatiblePath));
        applyAlternateNameForBoutiqueCategoriesOnBreadcrumbs(originalCategoryDocuments, originalCategoryIds, boutiqueCategoryIds, breadcrumbs);
        return breadcrumbs;
    }

    protected String buildBackwardCompatiblePath(String navPath) {
        String newNavPath = navPath;
        if (navPath.contains(",")) {
            newNavPath = navPath.replaceAll(",", SEPARATOR);
        }

        if (navPath.endsWith(navigationServiceConfig.getCategoryConfig().getIdConfig().getLive())
                || navPath.endsWith(navigationServiceConfig.getCategoryConfig().getIdConfig().getStage())
                || navPath.endsWith(navigationServiceConfig.getCategoryConfig().getIdConfig().getMarketing())) {
            List<String> categoryIds = Arrays.asList(newNavPath.split(SEPARATOR));
            Collections.reverse(categoryIds);
            return String.join(SEPARATOR, categoryIds);
        }
        return newNavPath;
    }

    private void processAlternateNameForBoutiqueCategoryOnBreadcrumb(Breadcrumb breadcrumb, List<CategoryDocument> originalCategoryDocuments, List<String> originalCategoryIds) {
        int indexInOriginalIds = originalCategoryIds.indexOf(breadcrumb.getId());
        int parentIndex = originalCategoryIds.indexOf(breadcrumb.getId()) - 1;
        String parentCategoryId = parentIndex >= 0 ? originalCategoryDocuments.get(parentIndex).getId() : breadcrumbUtil.getRootCategoryId();
        breadcrumb.setName(originalCategoryDocuments.get(indexInOriginalIds).getDesktopAlternateName(parentCategoryId));
        breadcrumb.setNameForMobile(originalCategoryDocuments.get(indexInOriginalIds).getMobileAlternateName(parentCategoryId));
    }

    private void applyAlternateNameForBoutiqueCategoriesOnBreadcrumbs(List<CategoryDocument> originalCategoryDocuments, List<String> originalCategoryIds, List<String> boutiqueCategoryIds, List<Breadcrumb> breadcrumbs) {
        for (Breadcrumb breadcrumb : breadcrumbs) {
            if (boutiqueCategoryIds.contains(breadcrumb.getId())) {
                processAlternateNameForBoutiqueCategoryOnBreadcrumb(breadcrumb, originalCategoryDocuments, originalCategoryIds);
            }
        }
    }

    private List<CategoryDocument> getBoutiqueCategoryDocumentListWithReplacedCategory(List<CategoryDocument> boutiqueCategoryDocuments) {
        CategoryDocument designerCategoryDocument;
        try {
            designerCategoryDocument = categoryRepository.getCategoryDocument(navigationServiceConfig.getCategoryConfig().getIdConfig().getDesigner());
        } catch (CategoryNotFoundException e) {
            throw new BreadcrumbNotFoundException(navigationServiceConfig.getCategoryConfig().getIdConfig().getDesigner(), e);
        }
        boutiqueCategoryDocuments.add(0, designerCategoryDocument);
        return boutiqueCategoryDocuments;
    }

    private List<CategoryDocument> getRemainingCategoriesInOriginalCategoryDocumentList(List<CategoryDocument> originalCategoryDocuments, List<CategoryDocument> boutiqueCategoryDocuments) {
        int indexOfLastBoutiqueCategoryDocument = originalCategoryDocuments.indexOf(boutiqueCategoryDocuments.get(boutiqueCategoryDocuments.size() - 1));
        return originalCategoryDocuments.subList(indexOfLastBoutiqueCategoryDocument + 1, originalCategoryDocuments.size());
    }

    private List<CategoryDocument> getDriveToSubCategoryDocumentList(final List<String> categoryIds) {
        List<CategoryDocument> driveToSubCategoryDocuments = new ArrayList<>();

        String categorySelectedByUser = categoryIds.get(categoryIds.size() - 1);
        CategoryDocument categoryDocumentOfLastCategoryInList;
        try {
            categoryDocumentOfLastCategoryInList = categoryRepository.getCategoryDocument(categorySelectedByUser);
        } catch (CategoryNotFoundException e) {
            throw new BreadcrumbNotFoundException(categorySelectedByUser, e);
        }
        Optional<String> parentCategoryId = findParentFromPath(categorySelectedByUser, categoryIds);

        if (parentCategoryId.isPresent()) {
            Optional<List<ContextualProperty>> contextualProperties = Optional.ofNullable(categoryDocumentOfLastCategoryInList.getContextualProperties());
            if (contextualProperties.isPresent()) {
                Optional<ContextualProperty> contextPropWithMatchingParentIdAndHasDriveToSubCat = contextualProperties.get().stream()
                        .filter(contextualProperty -> contextualProperty.getParentId().equals(parentCategoryId.get()))
                        .filter(contextualProperty -> StringUtils.isNotBlank(contextualProperty.getDriveToSubcategoryId()))
                        .findAny();
                contextPropWithMatchingParentIdAndHasDriveToSubCat.ifPresent(contextualProperty -> getDriveToSubCategoryDocuments(driveToSubCategoryDocuments, contextualProperty));
            }
        }

        return driveToSubCategoryDocuments;
    }

    private void getDriveToSubCategoryDocuments(List<CategoryDocument> driveToSubCategoryDocuments, ContextualProperty contextPropWithMatchingParentIdAndHasDriveToSubCat) {
        List<String> driveToSubCatIds = Arrays.asList(contextPropWithMatchingParentIdAndHasDriveToSubCat.getDriveToSubcategoryId().split(":"));
        driveToSubCatIds.forEach(driveToSubCatId -> {
                    try {
                        driveToSubCategoryDocuments.add(categoryRepository.getCategoryDocument(driveToSubCatId));
                    } catch (CategoryNotFoundException e) {
                        throw new BreadcrumbNotFoundException(driveToSubCatId, e);
                    }
                }
        );
    }

    private Optional<String> findParentFromPath(String currentId, final List<String> categoryIds) {
        Optional<String> parentId = Optional.empty();
        for (String categoryId : categoryIds) {
            if (StringUtils.equals(currentId, categoryId)) {
                return parentId;
            }
            parentId = Optional.of(categoryId);
        }
        return Optional.empty();
    }

    private String getDefaultPath(String categoryId, String navKeyGroup) {
        CategoryDocument categoryDocument;
        try {
          if (TEST_CATEGORY_GROUP.equals(navKeyGroup)) {
            categoryDocument = categoryRepository.getCategoryDocument(breadcrumbUtil.getAlternateForDefault(categoryId));
          } else {
            categoryDocument = categoryRepository.getCategoryDocument(categoryId);
          }
        } catch (CategoryNotFoundException e) {
            throw new BreadcrumbNotFoundException(categoryId, e);
        }

        if (StringUtils.isBlank(categoryDocument.getDefaultPath())) {
            throw new BreadcrumbNotFoundException(categoryId);
        }
        return categoryDocument.getDefaultPath();
    }

    private Boolean hasLiveRootCatIdInNavPath(String navPath) {
        return navPath.contains(navigationServiceConfig.getCategoryConfig().getIdConfig().getLive());
    }
    
}
