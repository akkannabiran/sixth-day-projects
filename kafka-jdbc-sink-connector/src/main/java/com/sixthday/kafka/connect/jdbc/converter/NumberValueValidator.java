package com.sixthday.kafka.connect.jdbc.converter;

import com.sixthday.kafka.connect.jdbc.database.DBColumnDef;
import com.sixthday.kafka.connect.jdbc.exception.ValidationException;
import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.commons.validator.routines.IntegerValidator;
import org.apache.commons.validator.routines.LongValidator;
import org.apache.commons.validator.routines.ShortValidator;

import java.math.BigDecimal;

public class NumberValueValidator {

    public static class IntegerValueConverter implements ValueConverter<Integer> {

        @Override
        public Integer validateAndConvert(Object value, DBColumnDef dbColumnDef) throws ValidationException {
            String errorMsg = "Datatype '" +
                    dbColumnDef.getTypeName() + "' limit crossed for columnName=" +
                    dbColumnDef.getColumnName() +
                    ", columnLimit=" +
                    dbColumnDef.getMaxLength() +
                    ", value=" + value;

            if ("int2".equals(dbColumnDef.getTypeName()) || "smallserial".equals(dbColumnDef.getTypeName())) {
                if (ShortValidator.getInstance().validate(value.toString()) == null)
                    throw new ValidationException(errorMsg);
            } else if ("int4".equals(dbColumnDef.getTypeName()) || "serial".equals(dbColumnDef.getTypeName())) {
                if (IntegerValidator.getInstance().validate(value.toString()) == null)
                    throw new ValidationException(errorMsg);
            }
            try {
                return Integer.parseInt(value.toString());
            } catch (Exception e) {
                throw new ValidationException(errorMsg, e);
            }
        }
    }

    public static class LongValueConverter implements ValueConverter<Long> {

        @Override
        public Long validateAndConvert(Object value, DBColumnDef dbColumnDef) throws ValidationException {
            String errorMsg = "Datatype '" +
                    dbColumnDef.getTypeName() + "' limit crossed for columnName=" +
                    dbColumnDef.getColumnName() +
                    ", columnLimit=" +
                    dbColumnDef.getMaxLength() +
                    ", value=" + value;
            if ("bigserial".equals(dbColumnDef.getTypeName())) {
                if (LongValidator.getInstance().validate(value.toString()) == null)
                    throw new ValidationException(errorMsg);
            }
            try {
                return Long.parseLong(value.toString());
            } catch (Exception e) {
                throw new ValidationException(errorMsg, e);
            }
        }
    }

    public static class DoubleValueConverter implements ValueConverter<Double> {

        @Override
        public Double validateAndConvert(Object value, DBColumnDef dbColumnDef) throws ValidationException {
            String errorMsg = "Datatype '" +
                    dbColumnDef.getTypeName() + "' limit crossed for columnName=" +
                    dbColumnDef.getColumnName() +
                    ", columnLimit=" +
                    dbColumnDef.getMaxLength() +
                    ", value=" + value;
            if ("float8".equals(dbColumnDef.getTypeName())) {
                if (DoubleValidator.getInstance().validate(value.toString()) == null)
                    throw new ValidationException(errorMsg);
            }
            try {
                return Double.parseDouble(value.toString());
            } catch (Exception e) {
                throw new ValidationException(errorMsg, e);
            }
        }
    }

    public static class BigDecimalValueConverter implements ValueConverter<BigDecimal> {

        @Override
        public BigDecimal validateAndConvert(Object value, DBColumnDef dbColumnDef) throws ValidationException {
            String errorMsg = "Datatype '" +
                    dbColumnDef.getTypeName() + "' limit crossed for columnName=" +
                    dbColumnDef.getColumnName() +
                    ", columnLimit=" +
                    dbColumnDef.getMaxLength() +
                    ", value=" + value;
            if ("numeric".equals(dbColumnDef.getTypeName())) {
                if (DoubleValidator.getInstance().validate(value.toString()) == null)
                    throw new ValidationException(errorMsg);
            }
            try {
                return new BigDecimal(value.toString());
            } catch (Exception e) {
                throw new ValidationException(errorMsg, e);
            }
        }
    }

    public static class FloatValueConverter implements ValueConverter<Float> {

        @Override
        public Float validateAndConvert(Object value, DBColumnDef dbColumnDef) throws ValidationException {
            String errorMsg = "Datatype '" +
                    dbColumnDef.getTypeName() + "' limit crossed for columnName=" +
                    dbColumnDef.getColumnName() +
                    ", columnLimit=" +
                    dbColumnDef.getMaxLength() +
                    ", value=" + value;
            if ("float4".equals(dbColumnDef.getTypeName())) {
                if (DoubleValidator.getInstance().validate(value.toString()) == null)
                    throw new ValidationException(errorMsg);
            }
            try {
                return Float.valueOf(value.toString());
            } catch (Exception e) {
                throw new ValidationException(errorMsg, e);
            }
        }
    }
}
