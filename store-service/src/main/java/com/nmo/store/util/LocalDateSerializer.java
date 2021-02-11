package com.sixthday.store.util;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.sixthday.store.models.storeindex.StoreEventDocument;

public class LocalDateSerializer extends StdSerializer<LocalDate> {
	private static final long serialVersionUID = 1L;

    public LocalDateSerializer(){
        super(LocalDate.class);
    }

    @Override
    public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider sp) throws IOException, JsonProcessingException {
    	DateTimeFormatter formatters = DateTimeFormatter.ofPattern(StoreEventDocument.EVENT_DATE_FORMAT);
        gen.writeString(value.format(formatters));
    }
}
