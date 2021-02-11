package com.sixthday.navigation.api.elasticsearch.repository;

import com.sixthday.logger.logging.LoggableEvent;
import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.config.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@Slf4j
public class CategoryRepository {

    private NavigationServiceConfig navigationServiceConfig;
    private ESCategoryDocumentRepository esCategoryDocumentRepository;

    @Autowired
    public CategoryRepository(ESCategoryDocumentRepository esCategoryDocumentRepository,
                              NavigationServiceConfig navigationServiceConfig) {
        this.esCategoryDocumentRepository = esCategoryDocumentRepository;
        this.navigationServiceConfig = navigationServiceConfig;
    }

    public CategoryDocument getCategoryDocument(final String categoryId) {
        return esCategoryDocumentRepository.getCategoryDocument(categoryId);
    }

    public List<CategoryDocument> getCategoryDocuments(final List<String> categoryIds) {
        return esCategoryDocumentRepository.getCategoryDocuments(categoryIds);
    }

    @LoggableEvent(eventType = Constants.REPOSITORY, action = Constants.GET_BRAND_LINK_CATEGORY_DOCUMENTS)
    public Map<CategoryDocument, List<CategoryDocument>> getBrandLinks() {
        CategoryDocument seoFooterCategory = getCategoryDocument(navigationServiceConfig.getCategoryConfig().getIdConfig().getSeoFooter());
        if (seoFooterCategory == null) {
            return Collections.emptyMap();
        }
        Map<CategoryDocument, List<CategoryDocument>> brandLinks = new HashMap<>();
        List<String> seoFooterCategories = seoFooterCategory.getChildren();
        if (!CollectionUtils.isEmpty(seoFooterCategories)) {
            seoFooterCategories.forEach(categoryId -> {
                CategoryDocument sisterSiteCategory = getCategoryDocument(categoryId);
                if (sisterSiteCategory != null) {
                    List<String> sisterSiteCategories = sisterSiteCategory.getChildren();
                    if (!CollectionUtils.isEmpty(sisterSiteCategories)) {
                        brandLinks.put(sisterSiteCategory, getCategoryDocuments(sisterSiteCategories));
                    }
                }
            });
        }
        return brandLinks;
    }
}
