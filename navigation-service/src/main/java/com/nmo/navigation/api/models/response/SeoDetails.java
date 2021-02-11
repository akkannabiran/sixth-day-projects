package com.sixthday.navigation.api.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SeoDetails {
    private String nameOverride;
    private String canonicalUrl;
    private String title;
    private String metaInformation;
    private String titleOverride;
    private String content;
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private RedirectDetails redirectDetails;
}
