package com.sixthday.navigation.integration.services;

import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.integration.publisher.CategoryMessagePublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CategoryPublisherService {

    private CategoryMessagePublisher categoryMessagePublisher;

    @Autowired
    public CategoryPublisherService(final CategoryMessagePublisher categoryMessagePublisher) {
        this.categoryMessagePublisher = categoryMessagePublisher;
    }

    public void buildAndSend(CategoryDocument categoryDocument) {
        categoryMessagePublisher.sendMessage(categoryDocument);
    }
}
