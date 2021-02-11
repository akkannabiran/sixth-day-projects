package com.sixthday.store.models.storeindex;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sixthday.store.util.LocalDateDeserializer;
import com.sixthday.store.util.LocalDateSerializer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter @Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class StoreEventDocument {
	public static final String EVENT_DATE_FORMAT = "yyyy-MM-dd";

	private String eventId;
	private String eventName;
	private String eventTypeId;
	private String eventDescription;

	@JsonDeserialize(using = LocalDateDeserializer.class)
	@JsonSerialize(using = LocalDateSerializer.class)
	private LocalDate eventStartDate;

	@JsonDeserialize(using = LocalDateDeserializer.class)
	@JsonSerialize(using = LocalDateSerializer.class)
	private LocalDate eventEndDate;
	private String eventSchedule;
	private String eventDuration;

  public Boolean isEventWithinNext31Days() {
		LocalDate today = LocalDate.now();
		LocalDate oneMonthFromToday = today.plusMonths(1);
		// LocalDate comparisons are non-inclusive, so subtract a day
		LocalDate yesterday = today.minusDays(1);

		return this.getEventStartDate().isBefore(oneMonthFromToday) && this.getEventEndDate().isAfter(yesterday);
	}
}
