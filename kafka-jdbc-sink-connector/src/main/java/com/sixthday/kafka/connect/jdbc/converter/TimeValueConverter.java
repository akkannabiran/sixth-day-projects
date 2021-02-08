package com.sixthday.kafka.connect.jdbc.converter;

import com.sixthday.kafka.connect.jdbc.database.DBColumnDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Time;

public class TimeValueConverter implements ValueConverter<Time> {

    private final Logger LOGGER = LoggerFactory.getLogger(TimeValueConverter.class);

    @Override
    public Time validateAndConvert(Object value, DBColumnDef dbColumnDef) {
        if ("timetz".equals(dbColumnDef.getTypeName())) {
            try {
                return Time.valueOf(value.toString());
            } catch (Exception e) {
                LOGGER.warn("Unable to parse the time with timezone '{}'. Returning 'null' value.", value.toString());
            }
        } else if ("time".equals(dbColumnDef.getTypeName())) {
            try {
                return Time.valueOf(value.toString());
            } catch (Exception e) {
                LOGGER.warn("Unable to parse the time '{}'. Returning 'null' value.", value.toString());
            }
        }
        return null;
    }
}

