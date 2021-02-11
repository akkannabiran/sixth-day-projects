package com.sixthday.category.api.services;

import com.sixthday.category.api.mappers.CategoryMapper;
import com.sixthday.category.api.models.Category;
import com.sixthday.category.api.models.CategoryDocumentInfo;
import com.sixthday.category.api.utils.CategoryServiceUtil;
import com.sixthday.category.config.CategoryServiceConfig;
import com.sixthday.category.elasticsearch.repository.CategoryRepository;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CategoryService {
    private CategoryRepository categoryRepository;
    private CategoryMapper categoryMapper;
    private CategoryServiceConfig categoryServiceConfig;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper, CategoryServiceConfig categoryServiceConfig) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.categoryServiceConfig = categoryServiceConfig;
    }

    public List<Category> getCategories(Map<String, String> categoryIds, Optional<String> siloCategoryId, Optional<String> navPath) {
        List<Category> categories = new ArrayList<>();

        updateMDC(categoryIds);

        if (CategoryServiceUtil.isValidRequest(categoryIds, categoryServiceConfig.getThresholdLimitValue())) {
            final Set<String> stringSet = CategoryServiceUtil.collectCategoryIdsFromMap(categoryIds);
            if (!CollectionUtils.isEmpty(stringSet)) {
                final List<CategoryDocument> categoryDocuments = categoryRepository.getCategoryDocuments(stringSet);
                List<CategoryDocumentInfo> categoryDocumentInfoList = getCategoryDocumentInfoList(categoryIds, categoryDocuments, navPath);
                final Set<String> parentCategoryIds = categoryDocumentInfoList.stream().filter(inputMapper -> !inputMapper.isUseCategoryDocumentToBuildTheResponse())
                        .map(CategoryDocumentInfo::getParentCategoryId).collect(Collectors.toSet());
                categoryDocuments.addAll(!CollectionUtils.isEmpty(parentCategoryIds) ? categoryRepository.getCategoryDocuments(parentCategoryIds) : Collections.emptyList());
                for (CategoryDocumentInfo categoryDocumentInfo : categoryDocumentInfoList) {
                    categoryDocumentInfo.setParentCategoryDocument(categoryDocuments.stream().filter(categoryDocument -> categoryDocument.getId().equals(categoryDocumentInfo.getParentCategoryId())).findAny().orElse(null));
                    categories.add(categoryMapper.map(categoryDocumentInfo, siloCategoryId));
                }
            } else {
                log.error("Request Body with Category Ids is empty");
            }
        } else {
            log.error("Request Body with Category Ids is empty or Request for more than 25 Categories");
        }
        return categories;
    }

    private void updateMDC(Map<String, String> categoryIds) {
        if (log.isDebugEnabled()) {
            MDC.put("CategoryId", "\"" + categoryIds.toString() + "\"");
        }

    }

    private List<CategoryDocumentInfo> getCategoryDocumentInfoList(Map<String, String> categoryIds, List<CategoryDocument> categoryDocuments, Optional<String> navPath) {
        List<CategoryDocumentInfo> categoryDocumentInfoList = new ArrayList<>();
        for (Map.Entry<String, String> entrySet : categoryIds.entrySet()) {
            if (!StringUtils.isEmpty(entrySet.getKey())) {
                final Optional<CategoryDocumentInfo> categoryDocumentInfo = buildCategoryDocumentInfo(entrySet, categoryDocuments, navPath);
                categoryDocumentInfo.ifPresent(categoryDocumentInfoList::add);
            } else {
                log.warn("Ignoring the request for " + entrySet.toString());
            }
        }
        return categoryDocumentInfoList;
    }

    private Optional<CategoryDocumentInfo> buildCategoryDocumentInfo(final Map.Entry<String, String> entrySet, final List<CategoryDocument> categoryDocuments, final Optional<String> navPath) {
        CategoryDocumentInfo categoryDocumentInfo = new CategoryDocumentInfo();
        categoryDocumentInfo.setCategoryId(entrySet.getKey());
        categoryDocumentInfo.setCategoryDocument(categoryDocuments.parallelStream().filter(categoryDocument -> categoryDocument.getId().equals(entrySet.getKey())).findAny().orElse(null));
        if (categoryDocumentInfo.getCategoryDocument() == null)
            return Optional.empty();
        categoryDocumentInfo.setUseCategoryDocumentToBuildTheResponse(CategoryServiceUtil.useCategoryDocumentToBuildTheResponse(categoryDocumentInfo.getCategoryDocument(), entrySet.getValue()));
        categoryDocumentInfo.setParentCategoryId(CategoryServiceUtil.getParentCategoryId(entrySet, categoryDocumentInfo.getCategoryDocument()));
        categoryDocumentInfo.setNewAspectRatio(isCurrentOrParentCategoryNewAspectRatioConfigured(navPath, entrySet, categoryDocumentInfo));
        return Optional.of(categoryDocumentInfo);
    }

    private boolean isCurrentOrParentCategoryNewAspectRatioConfigured(final Optional<String> navPath, final Map.Entry<String, String> entrySet, CategoryDocumentInfo categoryDocumentInfo) {
        List<String> pathCategoryIds = CategoryServiceUtil.collectPathCategoryIds(navPath, entrySet, categoryDocumentInfo.getCategoryDocument());
        return this.categoryServiceConfig.getNewAspectRatioCategoryIdList().stream().anyMatch(pathCategoryIds::contains);
    }
}
