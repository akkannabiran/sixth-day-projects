package com.sixthday.category.api.models;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SortOptionSerializerTest {

    @Mock
    JsonGenerator generator;

    @Mock
    SerializerProvider provider;

    @Test
    public void shouldSerializeSortOption() throws IOException {
        StdSerializer<SortOption> serializer = new SortOptionSerializer();
        SortOption option = new SortOption(SortOption.Option.BEST_MATCH, false);

        serializer.serialize(option, generator, provider);

        verify(generator).writeStartObject();
        verify(generator).writeStringField(option.getValue().toString(), option.getValue().getName());
        verify(generator).writeBooleanField("isDefault", option.getIsDefault());
        verify(generator).writeEndObject();
    }

}