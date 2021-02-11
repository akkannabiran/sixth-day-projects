package com.sixthday.category.api.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class RedirectDetails {
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private Integer httpCode;
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private String redirectToCategory;
}
