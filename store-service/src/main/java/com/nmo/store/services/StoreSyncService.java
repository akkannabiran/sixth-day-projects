package com.sixthday.store.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sixthday.store.models.storeindex.StoreDocument;
import com.sixthday.store.repository.StoreSyncRepository;
import com.sixthday.store.exceptions.StoreUpsertFailedException;
import com.sixthday.store.models.storeindex.StoreMessage;

@Component
public class StoreSyncService {

	private StoreSyncRepository storeSyncRepository;

	@Autowired
	public StoreSyncService(final StoreSyncRepository storeSyncRepository) {
		this.storeSyncRepository = storeSyncRepository;
	}

	public void upsertStore(final StoreDocument storeDocument, StoreMessage.EventType eventType)
			throws StoreUpsertFailedException {
		storeSyncRepository.createOrUpdateStore(storeDocument, eventType);
	}
}
