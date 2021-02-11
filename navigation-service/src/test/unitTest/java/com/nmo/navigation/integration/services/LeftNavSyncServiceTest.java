package com.sixthday.navigation.integration.services;

import com.sixthday.navigation.integration.repository.LeftNavSyncRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
public class LeftNavSyncServiceTest {

    @Mock
    private LeftNavSyncRepository leftNavSyncRepository;

    @InjectMocks
    private LeftNavSyncService leftNavSyncService;

    @Test
    public void shouldCallUpsertOrDeleteCategory() {
        String categoryToBeDeleted = "cat123";

        leftNavSyncService.deleteLeftNavTreeForThatCategory(categoryToBeDeleted);

        verify(leftNavSyncRepository).fetchAndDeleteLeftNavDocument(categoryToBeDeleted);
    }
}
