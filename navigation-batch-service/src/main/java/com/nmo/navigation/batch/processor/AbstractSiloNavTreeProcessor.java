package com.sixthday.navigation.batch.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.navigation.batch.vo.SiloNavTreeProcessorResponse;
import com.sixthday.navigation.batch.vo.SiloNavTreeReaderResponse;
import com.sixthday.navigation.config.NavigationBatchServiceConfig;
import com.sixthday.navigation.domain.ContextualProperty;
import com.sixthday.navigation.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.elasticsearch.repository.CategoryRepository;
import com.sixthday.navigation.util.UrlUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Slf4j
@Component
public abstract class AbstractSiloNavTreeProcessor {
    private final NavigationBatchServiceConfig navigationBatchServiceConfig;
    private CategoryRepository categoryRepository;

    @Autowired
    public AbstractSiloNavTreeProcessor(NavigationBatchServiceConfig navigationBatchServiceConfig, CategoryRepository categoryRepository) {
        this.navigationBatchServiceConfig = navigationBatchServiceConfig;
        this.categoryRepository = categoryRepository;
    }

    public abstract SiloNavTreeProcessorResponse process(SiloNavTreeReaderResponse item, ObjectMapper objectMapper);

    protected NavigationBatchServiceConfig getNavigationBatchServiceConfig() {
        return navigationBatchServiceConfig;
    }

    protected CategoryRepository getCategoryRepository() {
        return categoryRepository;
    }

    protected String updateNavPathWithDriveTo(final String categoryId, final String parentNavPath, final String currentNavPath, final String url) {
        String navPath = currentNavPath;

        if (url != null && !url.contains(categoryId)) {
            CategoryDocument categoryDocument = getCategoryRepository().getCategoryDocument(categoryId);
            if (categoryDocument != null) {
                String parentId = UrlUtil.getLastCategoryId(parentNavPath).orElse(null);
                ContextualProperty contextualProperty = categoryDocument.getApplicablePropertiesForCategory(parentId);
                Optional<String> driveToSubCategoryIds = categoryDocument.getDriveToSubCategoryIds(contextualProperty);
                if (driveToSubCategoryIds.isPresent()) {
                    navPath = navPath + "_" + driveToSubCategoryIds.get().replace(":", "_");
                } else if (!currentNavPath.equals(parentNavPath)) { //suppress error log for strange mobile nav case
                    log.error("Missing driveTo info for categoryId: " + categoryId + " parentId: " + parentId +
                            " url: " + url + " currentNavPath: " + currentNavPath + " parentNavPath: " + parentNavPath);
                }
            } else {
                log.error("Unable to find category document for categoryID: " + categoryId);
            }
        }

        return navPath;
    }

    protected String buildNavPath(String pathSoFar, String categoryId) {
        String navPath = pathSoFar;
        if (StringUtils.isEmpty(pathSoFar)) {
            navPath = categoryId;
        } else if (!pathSoFar.contains(categoryId)) {
            navPath = pathSoFar + "_" + categoryId;
        }

        return navPath;
    }
}
