package com.sixthday.navigation.api.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class RedirectDetails {
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private Integer httpCode;
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private String redirectToCategory;
}
