package com.sixthday.store.data;

import com.sixthday.store.models.sixthdayStore;
import com.sixthday.store.models.sixthdayStore.*;

import java.util.ArrayList;
import java.util.List;

public class sixthdayStoreBuilder {
    private String storeNumber = "storeNumber";
    private String name = "storeName";
    private String storeId = "storeId";
    private String image = "image";
    private String addressLine1 = "addressLine1";
    private String addressLine2 = "addressLine2";
    private String phoneNumber = "phoneNumber";
    private String storeHours = "storeHours";
    private SkuAvailability skuAvailability = new SkuAvailability("Not Available Today", false, "ADD TO CART FOR PICKUP IN 2-3 DAYS");
    private List<sixthdayStoreEvent> events = new ArrayList<>();

    public sixthdayStore build() {
        sixthdayStore store = new sixthdayStore();
        store.setStoreNumber(storeNumber);
        store.setName(name);
        store.setStoreId(storeId);
        store.setImage(image);
        store.setAddressLine1(addressLine1);
        store.setAddressLine2(addressLine2);
        store.setPhoneNumber(phoneNumber);
        store.setStoreHours(storeHours);
        store.setSkuAvailability(skuAvailability);
        store.setEvents(events);
        return store;
    }

    public sixthdayStoreBuilder withStoreNumber(String storeNumber) {
        this.storeNumber = storeNumber;
        return this;
    }
    public sixthdayStoreBuilder withName(String name) {
        this.name = name;
        return this;
    }
    public sixthdayStoreBuilder withStoreId(String storeId) {
        this.storeId = storeId;
        return this;
    }
    public sixthdayStoreBuilder withImage(String image) {
        this.image = image;
        return this;
    }
    public sixthdayStoreBuilder withAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
        return this;
    }
    public sixthdayStoreBuilder withAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
        return this;
    }
    public sixthdayStoreBuilder withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }
    public sixthdayStoreBuilder withStoreHours(String storeHours) {
        this.storeHours = storeHours;
        return this;
    }
    public sixthdayStoreBuilder withSkuAvailability(SkuAvailability skuAvailability) {
        this.skuAvailability = skuAvailability;
        return this;
    }
    public sixthdayStoreBuilder addEvent(sixthdayStoreEvent event) {
        this.events.add(event);
        return this;
    }
}
