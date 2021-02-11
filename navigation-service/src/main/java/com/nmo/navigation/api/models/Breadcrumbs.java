package com.sixthday.navigation.api.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Breadcrumbs {
    @JsonProperty("breadcrumbs")
    List<Breadcrumb> breadcrumbList;
}
