package com.sixthday.kafka.connect.jdbc.converter;

import com.sixthday.kafka.connect.jdbc.database.DBColumnDef;

public class BooleanValueConverter implements ValueConverter<Boolean> {

    @Override
    public Boolean validateAndConvert(Object value, DBColumnDef dbColumnDef) {
        if ("bool".equals(dbColumnDef.getTypeName())) {
            return Boolean.valueOf(value.toString());
        }
        return null;
    }
}
