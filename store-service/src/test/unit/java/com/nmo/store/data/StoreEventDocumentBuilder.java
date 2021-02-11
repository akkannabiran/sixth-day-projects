package com.sixthday.store.data;

import com.sixthday.store.models.storeindex.StoreEventDocument;

import java.time.LocalDate;

public class StoreEventDocumentBuilder {
    private String eventId = "EVENT_ID";
    private String eventName = "EVENT_NAME";
    private String eventTypeId = "EVENT_TYPE_ID";
    private String eventDescription = "EVENT_DESCRIPTION";
    private LocalDate eventStartDate = LocalDate.now();
    private LocalDate eventEndDate = eventStartDate.plusDays(1);
    private String eventSchedule = "EVENT_SCHEDULE";
    private String eventDuration = "EVENT_DURATION";
    private StoreDocumentBuilder parentBuilder;

    public StoreEventDocumentBuilder() {}

    public StoreEventDocumentBuilder(StoreDocumentBuilder parentBuilder) {
        this.parentBuilder = parentBuilder;
    }

    public StoreDocumentBuilder done() {
        if (parentBuilder == null) {
            throw new RuntimeException("Do not call 'done()' on StoreDocumentBuilder when using as stand-alone builder, only use when nesting from StoreDocumentBuilder");
        }
        parentBuilder.addStoreEventDocument(this.build());

        return parentBuilder;
    }

    public StoreEventDocument build() {
        return new StoreEventDocument(eventId, eventName, eventTypeId, eventDescription, eventStartDate,
                eventEndDate, eventSchedule, eventDuration);
    }

    public StoreEventDocumentBuilder withEventName(String eventName) {
        this.eventName = eventName;

        return this;
    }

    public StoreEventDocumentBuilder withEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;

        return this;
    }

    public StoreEventDocumentBuilder withEventDuration(String eventDuration) {
        this.eventDuration = eventDuration;

        return this;
    }

    public StoreEventDocumentBuilder withEventStartDate(LocalDate eventStartDate) {
        this.eventStartDate = eventStartDate;

        return this;
    }

    public StoreEventDocumentBuilder withEventEndDate(LocalDate eventEndDate) {
        this.eventEndDate = eventEndDate;

        return this;
    }


}
