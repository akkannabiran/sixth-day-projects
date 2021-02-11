package com.sixthday.store.services;

import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import com.sixthday.store.models.storeindex.StoreDocument;
import com.sixthday.store.repository.StoreSyncRepository;
import com.sixthday.store.models.storeindex.StoreMessage;


@RunWith(SpringRunner.class)
public class StoreSyncServiceTest {
    @Mock
    private StoreSyncRepository storeSyncRepository;
    @InjectMocks
    private StoreSyncService storeSyncService;

    @Test
    public void shouldCallUpsertStore() throws Exception {
        StoreDocument storeDocument = new StoreDocument();
        StoreMessage.EventType eventType = StoreMessage.EventType.STORE_UPSERT;

        storeSyncService.upsertStore(storeDocument, eventType);

        verify(storeSyncRepository).createOrUpdateStore(storeDocument, eventType);
    }
}
