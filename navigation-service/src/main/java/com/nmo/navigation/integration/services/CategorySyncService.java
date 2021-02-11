package com.sixthday.navigation.integration.services;

import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.integration.messages.CategoryMessage;
import com.sixthday.navigation.integration.repository.CategorySyncRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CategorySyncService {

    private CategorySyncRepository categorySyncRepository;

    @Autowired
    public CategorySyncService(final CategorySyncRepository categorySyncRepository) {
        this.categorySyncRepository = categorySyncRepository;
    }

    public void upsertOrDeleteCategory(final CategoryDocument categoryDocument, CategoryMessage.EventType eventType) {
        try {
            categorySyncRepository.upsertOrDeleteCategoryDocument(categoryDocument, eventType);
        } catch (Exception e) {
            log.error(", event_type=\"MESSAGE\", action=\"GET_CATEGORIES\", ExceptionTrace=", e);
        }
    }
}
