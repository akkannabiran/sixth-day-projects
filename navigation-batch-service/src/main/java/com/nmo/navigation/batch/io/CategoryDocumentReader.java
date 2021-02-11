package com.sixthday.navigation.batch.io;

import com.sixthday.navigation.batch.vo.CategoryDocuments;
import com.sixthday.navigation.config.NavigationBatchServiceConfig;
import com.sixthday.navigation.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.elasticsearch.repository.CategoryRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.sixthday.sixthdayLogging.OperationType.ELASTICSEARCH_GET_CATEGORY_DOCUMENTS;
import static com.sixthday.sixthdayLogging.logDebugOperation;

@Component
@Slf4j
public class CategoryDocumentReader {

    private CategoryRepository categoryRepository;
    private NavigationBatchServiceConfig navigationBatchServiceConfig;

    @Autowired
    CategoryDocumentReader(CategoryRepository categoryRepository, NavigationBatchServiceConfig navigationBatchServiceConfig) {
        this.categoryRepository = categoryRepository;
        this.navigationBatchServiceConfig = navigationBatchServiceConfig;
    }

    @SneakyThrows
    protected CategoryDocuments read(String scrollId) {
        return categoryRepository.getCategoryDocuments(scrollId);
    }

    @SneakyThrows
    protected CategoryDocuments read(int numberOfDocuments) {
        return categoryRepository.getCategoryDocuments(numberOfDocuments);
    }

    public Map<String, CategoryDocument> getAllCategories() {
        return logDebugOperation(log, null, ELASTICSEARCH_GET_CATEGORY_DOCUMENTS, () -> {
            Map<String, CategoryDocument> categoryDocumentMap = new ConcurrentHashMap<>();

            CategoryDocuments categoryDocuments = read(navigationBatchServiceConfig.getLeftNavBatchConfig().getNumberOfDocuments());
            while (!categoryDocuments.getCategoryDocumentList().isEmpty()) {
                for (CategoryDocument document : categoryDocuments.getCategoryDocumentList()) {
                    categoryDocumentMap.put(document.getId(), document);
                }
                categoryDocuments = read(categoryDocuments.getScrollId());
            }
            return categoryDocumentMap;
        });
    }
}
