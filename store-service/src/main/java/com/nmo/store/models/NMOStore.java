package com.sixthday.store.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sixthday.store.models.storeindex.StoreDocument;
import com.sixthday.store.models.storeindex.StoreEventDocument;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
public class sixthdayStore {

    private String storeNumber;
    private String name;
    private String storeId;
    private String image;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String phoneNumber;
    private String storeHours;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private SkuAvailability skuAvailability;
    private List<sixthdayStoreEvent> events = new ArrayList<>();

    public String getImage() {
        return "/category/stores/" + this.getStoreId().replaceAll("/", "") + "/images/r_main.jpg";
    }

    public static sixthdayStore from(StoreDocument storeDocument) {
        sixthdayStore sixthdayStore = new sixthdayStore();

        sixthdayStore.setStoreNumber(storeDocument.getStoreNumber());
        sixthdayStore.setStoreId(storeDocument.getId());
        sixthdayStore.setName(storeDocument.getStoreName());
        sixthdayStore.setAddressLine1(storeDocument.getAddressLine1());
        sixthdayStore.setAddressLine2(storeDocument.getAddressLine2());
        sixthdayStore.setCity(storeDocument.getCity());
        sixthdayStore.setPhoneNumber(storeDocument.getPhoneNumber());
        sixthdayStore.setStoreHours(storeDocument.getStoreHours());

        List<StoreEventDocument> storeEvents = Optional.ofNullable(storeDocument.getEvents()).orElse(new ArrayList<>());
        List<sixthdayStoreEvent> sixthdayStoreEvents = storeEvents.stream()
                .filter(sixthdayStoreEvent::checkIfStoreEventIsEmpty)
                .filter(StoreEventDocument::isEventWithinNext31Days)
                .sorted(comparing(StoreEventDocument::getEventStartDate))
                .map(sixthdayStoreEvent::from)
                .collect(Collectors.toList());

        sixthdayStore.setEvents(sixthdayStoreEvents);

        return sixthdayStore;
    }

    public static sixthdayStore from(StoreDocument storeDocument, Optional<SkuAvailabilityInfo> skuAvailabilityInfo) {
        sixthdayStore sixthdayStore = sixthdayStore.from(storeDocument);
        sixthdayStore.setSkuAvailability(
                skuAvailabilityInfo
                        .map(info -> new SkuAvailability(info.getAvailabilityStatus(), info.isInventoryAvailable(), info.getAddToCartMessage()))
                        .orElse(SkuAvailability.getDefault())
        );
        return sixthdayStore;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class sixthdayStoreEvent {
        private String name;
        private String description;
        private String eventTimeDuration;

        //TODO : Remove this once the corrupt events"[{}]" issue on store document is fixed.
        static boolean checkIfStoreEventIsEmpty(StoreEventDocument storeEventDocument) {
            return storeEventDocument.getEventId() != null && storeEventDocument.getEventStartDate() != null;
        }

        static sixthdayStoreEvent from(StoreEventDocument storeEventDocument) {
            sixthdayStoreEvent sixthdayStoreEvent = new sixthdayStoreEvent();

            sixthdayStoreEvent.setName(storeEventDocument.getEventName());
            sixthdayStoreEvent.setDescription(storeEventDocument.getEventDescription());
            sixthdayStoreEvent.setEventTimeDuration(storeEventDocument.getEventDuration());

            return sixthdayStoreEvent;
        }
    }

    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class SkuAvailability {
        private String status;
        private boolean inventoryAvailable;
        private String addToCartMessage;

        public static SkuAvailability getDefault() {
            return new SkuAvailability(SkuAvailabilityInfo.NOT_AVAILABLE_TODAY, false, SkuAvailabilityInfo.PICK_UP_IN_TWO_THREE_DAYS);
        }
    }
}
