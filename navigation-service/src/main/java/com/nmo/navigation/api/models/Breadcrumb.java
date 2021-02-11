package com.sixthday.navigation.api.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@AllArgsConstructor
public class Breadcrumb {
    private String id;
    private String name;
    private String nameForMobile;
    private String url;
}
