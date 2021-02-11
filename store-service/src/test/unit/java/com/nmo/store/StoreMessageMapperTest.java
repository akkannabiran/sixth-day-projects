package com.sixthday.store;

import com.sixthday.store.data.StoreMessageBuilder;
import com.sixthday.store.models.storeindex.StoreDocument;
import com.sixthday.store.models.storeindex.StoreEventDocument;
import com.sixthday.store.models.storeindex.StoreMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class StoreMessageMapperTest {
    private StoreMessageMapper storeMessageMapper = new StoreMessageMapper();

    @Test
    public void shouldMapStoreMessageAttributesToStoreDocument() {
        StoreMessage storeMessage = new StoreMessageBuilder()
                    .withDisplayable(true)
                    .withEligibleForBOPS(true)
        			.withStoreEvent(new StoreEventDocument(	"128700337", "CHANEL Facial Event", null,
        					"Pamper yourself with a luxurious facial.", LocalDate.now(), LocalDate.now(), null, null))
        			.withStoreEvent(new StoreEventDocument(	"128700339", "Lafayette 148 New York Special Presentation.", null,
        					"View the latest collection and enjoy expert style consultations", LocalDate.now(), LocalDate.now(), null, null))
        			.build();

        StoreDocument storeDocument = storeMessageMapper.map(storeMessage);
        
        assertThat(storeDocument.getId(), is("106/BL"));
        assertThat(storeDocument.isDisplayable(), is(true));
        assertThat(storeDocument.isEligibleForBOPS(), is(true));
        assertThat(storeDocument.getAddressLine1(), is("11111 NE 8th Street"));
        assertThat(storeDocument.getEvents().get(0).getEventId(), is("128700337"));
        assertThat(storeDocument.getEvents().get(0).getEventName(), is("CHANEL Facial Event"));
        assertThat(storeDocument.getEvents().get(1).getEventId(), is("128700339"));
        assertThat(storeDocument.getEvents().get(1).getEventName(), is("Lafayette 148 New York Special Presentation."));
    }
}
