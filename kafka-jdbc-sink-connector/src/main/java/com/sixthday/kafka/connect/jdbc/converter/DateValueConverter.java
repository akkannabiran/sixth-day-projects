package com.sixthday.kafka.connect.jdbc.converter;

import com.sixthday.kafka.connect.jdbc.database.DBColumnDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;

public class DateValueConverter implements ValueConverter<Date> {

    private final Logger LOGGER = LoggerFactory.getLogger(DateValueConverter.class);

    @Override
    public Date validateAndConvert(Object value, DBColumnDef dbColumnDef) {
        if ("date".equals(dbColumnDef.getTypeName())) {
            try {
                return Date.valueOf(value.toString());
            } catch (Exception e) {
                LOGGER.warn("Unable to parse the date '{}'. Returning 'null' value.", value.toString());
            }
        }
        return null;
    }
}

