package com.sixthday.store.util;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.sixthday.store.models.storeindex.StoreEventDocument;

public class LocalDateDeserializer extends StdDeserializer<LocalDate> {
	private static final long serialVersionUID = 5461397815758695791L;

	protected LocalDateDeserializer() {
		super(LocalDate.class);
	}

	@Override
	public LocalDate deserialize(JsonParser parser, DeserializationContext context) throws IOException {
		DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive()
				.appendPattern(StoreEventDocument.EVENT_DATE_FORMAT).toFormatter();

		return LocalDate.parse(parser.readValueAs(String.class), formatter);
	}
}
