package com.sixthday.navigation.api.models.response;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class CategoryTemplateDetails {
    private String id;
    private String templateType;
    private Map<String, Object> templateAttributes = new HashMap<>();
}
