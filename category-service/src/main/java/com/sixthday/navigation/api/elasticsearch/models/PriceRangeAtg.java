package com.sixthday.navigation.api.elasticsearch.models;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PriceRangeAtg implements Serializable {
    private static final long serialVersionUID = 3036823193481847124L;

    private String option;

    private BigDecimal min;
    private BigDecimal max;
}
