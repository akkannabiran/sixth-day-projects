package com.sixthday.store;

import com.sixthday.store.models.storeindex.StoreDocument;
import com.sixthday.store.models.storeindex.StoreMessage;

public class StoreMessageMapper {
	public StoreDocument map(StoreMessage storeMessage) {
		StoreDocument storeDocument = new StoreDocument();
		storeDocument.setId(storeMessage.getId());
		storeDocument.setStoreNumber(storeMessage.getStoreNumber());
		storeDocument.setStoreName(storeMessage.getStoreName());
		storeDocument.setAddressLine1(storeMessage.getAddressLine1());
		storeDocument.setAddressLine2(storeMessage.getAddressLine2());
		storeDocument.setCity(storeMessage.getCity());
		storeDocument.setState(storeMessage.getState());
		storeDocument.setZipCode(storeMessage.getZipCode());
		storeDocument.setPhoneNumber(storeMessage.getPhoneNumber());
		storeDocument.setStoreHours(storeMessage.getStoreHours());
		storeDocument.setStoreDescription(storeMessage.getStoreDescription());
		storeDocument.setEvents(storeMessage.getEvents());
		storeDocument.setDisplayable(storeMessage.isDisplayable());
		storeDocument.setEligibleForBOPS(storeMessage.isEligibleForBOPS());
		return storeDocument;
	}
}
