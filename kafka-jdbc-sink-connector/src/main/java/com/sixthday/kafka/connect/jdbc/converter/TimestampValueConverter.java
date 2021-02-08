package com.sixthday.kafka.connect.jdbc.converter;

import com.sixthday.kafka.connect.jdbc.database.DBColumnDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;

public class TimestampValueConverter implements ValueConverter<Timestamp> {

    private final Logger LOGGER = LoggerFactory.getLogger(TimestampValueConverter.class);

    @Override
    public Timestamp validateAndConvert(Object value, DBColumnDef dbColumnDef) {
        if ("timestamptz".equals(dbColumnDef.getTypeName())) {
            try {
                return Timestamp.valueOf(value.toString());
            } catch (Exception e) {
                LOGGER.warn("Unable to parse the timestamp with timezone '{}'. Returning 'null' value.", value.toString());
            }
        } else if ("timestamp".equals(dbColumnDef.getTypeName())) {
            try {
                return Timestamp.valueOf(value.toString());
            } catch (Exception e) {
                LOGGER.warn("Unable to parse the timestamp '{}'. Returning 'null' value.", value.toString());
            }
        }
        return null;
    }
}

