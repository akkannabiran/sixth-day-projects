package com.sixthday.navigation.integration.services;

import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.integration.publisher.CategoryMessagePublisher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CategoryPublisherServiceTest {

    @Mock
    CategoryMessagePublisher categoryMessagePublisher;

    @InjectMocks
    CategoryPublisherService categoryPublisherService;

    @Test
    public void shouldPublishCategoryDocument() {
        CategoryDocument categoryDocument = new CategoryDocument();
        categoryPublisherService.buildAndSend(categoryDocument);

        verify(categoryMessagePublisher).sendMessage(categoryDocument);
    }

}