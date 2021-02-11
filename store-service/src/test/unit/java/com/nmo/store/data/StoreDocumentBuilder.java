package com.sixthday.store.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixthday.store.models.storeindex.StoreDocument;
import com.sixthday.store.models.storeindex.StoreEventDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class StoreDocumentBuilder {
    private String id = UUID.randomUUID().toString();
    private String storeNumber = "STORE_NUMBER";
    private String storeName = "STORE_NAME";
    private String addressLine1 = "ADDRESS_LINE1";
    private String addressLine2 = "ADDRESS_LINE2";
    private String city = "CITY";
    private String state = "STATE";
    private String zipCode = "ZIPCODE";
    private String phoneNumber = "PHONE_NUMBER";
    private String storeHours = "STORE_HOURS";
    private String storeDescription = "STORE_DESCRIPTION";
    private boolean displayable = true;
    private boolean eligibleForBOPS = true;
    private List<StoreEventDocument> storeEventDocuments = new ArrayList<>();
    private ObjectMapper mapper = new ObjectMapper();

    public StoreDocument build() {
        return new StoreDocument(id, storeNumber, storeName, addressLine1, addressLine2, city, state, zipCode,
                phoneNumber, storeHours, storeDescription, displayable, eligibleForBOPS, storeEventDocuments);
    }

    public String buildAsJsonString() throws JsonProcessingException {
        return mapper.writeValueAsString(build());
    }

    public StoreDocumentBuilder withStoreNumber(String storeNumber) {
        this.storeNumber = storeNumber;
        return this;
    }

    public StoreDocumentBuilder withStoreName(String storeName) {
        this.storeName = storeName;
        return this;
    }

    public StoreDocumentBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public StoreDocumentBuilder withDisplayable(boolean displayable) {
        this.displayable = displayable;
        return this;
    }

    public StoreDocumentBuilder withEligibleForBOPS(boolean eligibleForBOPS) {
        this.eligibleForBOPS = eligibleForBOPS;
        return this;
    }

    public StoreDocumentBuilder withAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
        return this;
    }

    public StoreDocumentBuilder withAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
        return this;
    }

    public StoreDocumentBuilder withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public StoreDocumentBuilder withStoreHours(String storeHours) {
        this.storeHours = storeHours;
        return this;
    }

    public StoreEventDocumentBuilder withStoreEventDocument() {
        StoreEventDocumentBuilder storeEventDocumentBuilder = new StoreEventDocumentBuilder(this);
        return storeEventDocumentBuilder;
    }

    public StoreDocumentBuilder addStoreEventDocument(StoreEventDocument storeEventDocument) {
        this.storeEventDocuments.add(storeEventDocument);
        return this;
    }

    public static String randomStoreNumber() {
        return String.valueOf(new Random().nextInt(1000000));
    }
}
