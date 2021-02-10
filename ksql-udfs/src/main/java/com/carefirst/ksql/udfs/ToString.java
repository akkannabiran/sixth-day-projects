package com.sixthday.ksql.udfs;

import io.confluent.ksql.function.udf.Udf;
import io.confluent.ksql.function.udf.UdfDescription;
import io.confluent.ksql.function.udf.UdfParameter;

import org.apache.kafka.common.Configurable;

import java.lang.StringBuilder;
import java.util.Map;

@UdfDescription(name = "TOSTR",
        author = "Arunkumar Kannabiran",
        version = "1.0.0",
        description = "The given string will be converted into double quoted string for valid values")
public class ToString implements Configurable {

    @Override
    public void configure(final Map<String, ?> map) {
    }

    @Udf(description = "The given string will be converted into double quoted string for valid values")
    public String toString(@UdfParameter String input) {
        if (input != null) {
            return new StringBuilder().append("\"").append(input).append("\"").toString();
        }
        return input;
    }
}