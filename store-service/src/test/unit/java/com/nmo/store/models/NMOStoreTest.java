package com.sixthday.store.models;

import com.sixthday.store.models.storeindex.StoreDocument;
import com.sixthday.store.models.storeindex.StoreEventDocument;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class sixthdayStoreTest {
    @Test
    public void shouldSortEventsInChronologicalOrderInStores() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate dayAfter10days = today.plusDays(10);

        List<StoreEventDocument> storeEvents = Arrays.asList(
                new StoreEventDocument("eventId", "eventName1", "eventTypeId", "description", dayAfter10days, LocalDate.now(), "schedule", "timeDuration"),
                new StoreEventDocument("eventId", "eventName3", "eventTypeId", "description", today, LocalDate.now(), "schedule", "timeDuration"),
                new StoreEventDocument("eventId", "eventName2", "eventTypeId", "description", tomorrow, LocalDate.now(), "schedule", "timeDuration")
        );

        StoreDocument storeDocument = new StoreDocument("storeId", "1", "storeName", "addressLine1", "addressLine2", "city", "state",
                "zipCode", "phoneNumber", "storeHours", "description", true, true, storeEvents);

        sixthdayStore sixthdayStore = sixthdayStore.from(storeDocument);

        assertThat(sixthdayStore.getEvents().get(0).getName(), is(storeDocument.getEvents().get(1).getEventName()));
        assertThat(sixthdayStore.getEvents().get(1).getName(), is(storeDocument.getEvents().get(2).getEventName()));
        assertThat(sixthdayStore.getEvents().get(2).getName(), is(storeDocument.getEvents().get(0).getEventName()));
    }

    @Test
    public void shouldFilterOutEventsWithNullProperties() throws Exception {
        List<StoreEventDocument> storeEvents = Arrays.asList(
                new StoreEventDocument(null, "eventName1", "eventTypeId", "description", LocalDate.now(), LocalDate.now(), "schedule", "timeDuration"),
                new StoreEventDocument("eventId", "eventName2", "eventTypeId", "description", null, LocalDate.now(), "schedule", "timeDuration"),
                new StoreEventDocument("eventId", "eventName3", "eventTypeId", "description", LocalDate.now(), LocalDate.now(), "schedule", "timeDuration")
        );

        StoreDocument storeDocument = new StoreDocument("storeId", "1", "storeName", "addressLine1", "addressLine2", "city", "state",
                "zipCode", "phoneNumber", "storeHours", "description", true, true, storeEvents);

        sixthdayStore sixthdayStore = sixthdayStore.from(storeDocument);

        assertThat(sixthdayStore.getEvents().size(), is(1));
        assertThat(sixthdayStore.getEvents().get(0).getName(), is(storeDocument.getEvents().get(2).getEventName()));
    }
}
