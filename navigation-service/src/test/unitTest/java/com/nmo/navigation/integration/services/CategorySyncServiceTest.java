package com.sixthday.navigation.integration.services;

import com.sixthday.navigation.api.config.NavigationServiceConfig;
import com.sixthday.navigation.api.elasticsearch.documents.CategoryDocument;
import com.sixthday.navigation.integration.messages.CategoryMessage;
import com.sixthday.navigation.integration.repository.CategorySyncRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class CategorySyncServiceTest {

    @Mock
    private CategorySyncRepository categorySyncRepository;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private NavigationServiceConfig navigationServiceConfig;

    @InjectMocks
    private CategorySyncService categorySyncService;

    @Test
    public void shouldCallUpsertCategory() {
        CategoryDocument categoryDocument = new CategoryDocument();

        categorySyncService.upsertOrDeleteCategory(categoryDocument, CategoryMessage.EventType.CATEGORY_UPDATED);

        verify(categorySyncRepository).upsertOrDeleteCategoryDocument(categoryDocument, CategoryMessage.EventType.CATEGORY_UPDATED);
    }

    @Test
    public void shouldCallUpsertCategory_whenECIntegrationIsDisabled() {
        CategoryDocument categoryDocument = new CategoryDocument();

        categorySyncService.upsertOrDeleteCategory(categoryDocument, CategoryMessage.EventType.CATEGORY_UPDATED);

        verify(categorySyncRepository).upsertOrDeleteCategoryDocument(categoryDocument, CategoryMessage.EventType.CATEGORY_UPDATED);
    }

    @Test
    public void shouldCatchExceptionWhenUpsertOrDeleteCategoryFails() {
        doThrow(Exception.class).when(categorySyncRepository).upsertOrDeleteCategoryDocument(
                any(CategoryDocument.class), any(CategoryMessage.EventType.class));

        CategoryDocument categoryDocument = new CategoryDocument();
        categorySyncService.upsertOrDeleteCategory(categoryDocument, CategoryMessage.EventType.CATEGORY_UPDATED);

        verify(categorySyncRepository).upsertOrDeleteCategoryDocument(categoryDocument, CategoryMessage.EventType.CATEGORY_UPDATED);
    }

    @Test
    public void shouldCallDeleteCategory() {
        CategoryDocument categoryDocument = new CategoryDocument();
        categoryDocument.setDeleted(true);
        categorySyncService.upsertOrDeleteCategory(categoryDocument, CategoryMessage.EventType.CATEGORY_UPDATED);

        verify(categorySyncRepository, times(1)).upsertOrDeleteCategoryDocument(categoryDocument, CategoryMessage.EventType.CATEGORY_UPDATED);
    }
}
