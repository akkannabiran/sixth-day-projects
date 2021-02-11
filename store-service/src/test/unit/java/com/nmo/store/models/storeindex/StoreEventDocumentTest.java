package com.sixthday.store.models.storeindex;

import com.sixthday.store.data.StoreEventDocumentBuilder;
import org.junit.Test;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class StoreEventDocumentTest {
  public static final String PAST = "past";
  public static final String TODAY = "today";
  public static final String BEFORE_31_DAYS = "before 31 days";
  public static final String ON_31ST_DAY = "on 31st day";
  public static final String AFTER_31_DAYS = "after 31 days";
  String[] stringsLookup = { PAST, TODAY, BEFORE_31_DAYS, ON_31ST_DAY, AFTER_31_DAYS };
  LocalDate past = LocalDate.now().minusDays(1);
  LocalDate today = LocalDate.now();
  LocalDate before31Days = LocalDate.now().plusDays(1);
  LocalDate on31stDay = LocalDate.now().plusDays(31);
  LocalDate after31Days = LocalDate.now().plusDays(32);

  Boolean[][] tests = {
   /*                  Start:   past   |   today   | before 31 days | on 31st day | after 31 days */
   /* End:              */
   /* ...before         */ {    FALSE                                                               },
   /* ...on             */ {    TRUE,      TRUE                                                     },
   /* ...before 31 days */ {    TRUE,      TRUE,         TRUE                                       },
   /* ...on 31st day    */ {    TRUE,      TRUE,         TRUE,           FALSE                      },
   /* ...after 31 days  */ {    TRUE,      TRUE,         TRUE,           FALSE,         FALSE       },
  };

  @Test
  public void shouldOnlyIncludeEventsWithin1Month() {
    Map dateLookup = dateMap();
    for(int row = 0; row < 5; row++) {
      for (int col = 0; col < tests[row].length; col++) {
        String begin = stringsLookup[col];
        String end = stringsLookup[row];
        Boolean expected = tests[row][col];
        StoreEventDocument doc = new StoreEventDocumentBuilder()
                .withEventStartDate((LocalDate) dateLookup.get(begin))
                .withEventEndDate((LocalDate) dateLookup.get(end))
                .build();
        assertThat(description(begin, end), doc.isEventWithinNext31Days(), is(expected));
      }
    }
  }

  private Map dateMap () {
    Map<String, LocalDate> map = new HashMap<>();
    map.put(PAST, past);
    map.put(TODAY, today);
    map.put(BEFORE_31_DAYS, before31Days);
    map.put(ON_31ST_DAY, on31stDay);
    map.put(AFTER_31_DAYS, after31Days);
    return map;
  }

  private String description(String begin, String end) {
    return String.format("begins: %s, ends: %s", begin, end);
  }
}
