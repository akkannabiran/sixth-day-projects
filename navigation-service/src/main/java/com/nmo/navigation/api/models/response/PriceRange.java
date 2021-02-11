package com.sixthday.navigation.api.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PriceRange {
    private BigDecimal min;
    private BigDecimal max;
    private PriceRangeType option;

    public enum PriceRangeType {
        OFF,
        PROMO_PRICE,
        TYPE113_PERCENT_OFF
    }
}
