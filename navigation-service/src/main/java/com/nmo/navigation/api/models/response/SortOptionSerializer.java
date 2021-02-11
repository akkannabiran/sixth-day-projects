package com.sixthday.navigation.api.models.response;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.SneakyThrows;

public class SortOptionSerializer extends StdSerializer<SortOption> {
    public SortOptionSerializer() {
        this(null);
    }

    public SortOptionSerializer(Class<SortOption> t) {
        super(t);
    }

    @Override
    @SneakyThrows
    public void serialize(SortOption sortOption,
                          JsonGenerator generator,
                          SerializerProvider provider) {
        generator.writeStartObject();
        generator.writeStringField(sortOption.getValue().toString(), sortOption.getValue().getName());
        generator.writeBooleanField("isDefault", sortOption.getIsDefault());
        generator.writeEndObject();
    }
}
