package com.sixthday.store.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.store.models.storeindex.StoreEventDocument;
import com.sixthday.store.models.storeindex.StoreMessage;
import com.sixthday.store.models.storeindex.StoreMessage.EventType;

import java.util.ArrayList;

import static com.sixthday.store.models.storeindex.StoreMessage.EventType.STORE_UPSERT;

public class StoreMessageBuilder {
	private String id = "106/BL";
	private String storeNumber = "106";
	private String name = "Bellevue";
	private String addressLine1 = "11111 NE 8th Street";
	private String addressLine2 = "STE 123";
	private String city = "Bellevue";
	private String state = "WA";
	private String zipCode = "98004";
	private String phoneNumber = "425-452-3300";
	private String storeHours = "Mon. 10:00AM - 9:00PM,Tue. 10:00AM - 9:00PM,"
			+ "Wed. 10:00AM - 9:00PM,Thu. 10:00AM - 9:00PM,"
			+ "Fri. 10:00AM - 9:00PM,Sat. 10:00AM - 9:00PM,Sun. 12:00PM - 6:00PM";
	private String storeDescription = "<br>Sixthday is a renowned specialty store dedicated"
			+ "to merchandise leadership and superior customer service."
			+ "We will offer the finest fashion and quality products in a welcoming environment. ";
	private EventType eventType = STORE_UPSERT;
	private boolean displayable = false;
	private boolean eligibleForBOPS = false;
	private ArrayList<StoreEventDocument> storeEventDocuments = new ArrayList<>();

	public StoreMessage build() {
		StoreMessage message = new StoreMessage();
		message.setId(id);
		message.setStoreNumber(storeNumber);
		message.setStoreName(name);
		message.setAddressLine1(addressLine1);
		message.setAddressLine2(addressLine2);
		message.setCity(city);
		message.setState(state);
		message.setZipCode(zipCode);
		message.setPhoneNumber(phoneNumber);
		message.setStoreHours(storeHours);
		message.setStoreDescription(storeDescription);
		message.setEventType(eventType);
		message.setEvents(storeEventDocuments);
		message.setEligibleForBOPS(eligibleForBOPS);
		message.setDisplayable(displayable);
		return message;
	}

	public StoreMessageBuilder withId(String id) {
		this.id = id;
		return this;
	}

	public StoreMessageBuilder withStoreNumber(String storeNumber) {
		this.storeNumber = storeNumber;
		return this;
	}

	public StoreMessageBuilder withName(String name) {
		this.name = name;
		return this;
	}

	public StoreMessageBuilder withAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
		return this;
	}

	public StoreMessageBuilder withAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
		return this;
	}

	public StoreMessageBuilder withCity(String city) {
		this.city = city;
		return this;
	}

	public StoreMessageBuilder withState(String state) {
		this.state = state;
		return this;
	}

	public StoreMessageBuilder withZipCode(String zipCode) {
		this.zipCode = zipCode;
		return this;
	}

	public StoreMessageBuilder withPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
		return this;
	}

	public StoreMessageBuilder withStoreHours(String storeHours) {
		this.storeHours = storeHours;
		return this;
	}

	public StoreMessageBuilder withStoreDescription(String storeDescription) {
		this.storeDescription = storeDescription;
		return this;
	}

	public StoreMessageBuilder withEventType(EventType eventType) {
		this.eventType = eventType;
		return this;
	}

	public StoreMessageBuilder withStoreEvent(StoreEventDocument storeEventDocument) {
		this.storeEventDocuments.add(storeEventDocument);
		return this;
	}

	public StoreMessageBuilder withDisplayable(boolean flag) {
		this.displayable = flag;
		return this;
	}

	public StoreMessageBuilder withEligibleForBOPS(boolean flag) {
		this.eligibleForBOPS = flag;
		return this;
	}
	
	private ObjectMapper mapper = new ObjectMapper();
	public String buildAsJsonString() throws JsonProcessingException {
        return mapper.writeValueAsString(build());
    }
}
