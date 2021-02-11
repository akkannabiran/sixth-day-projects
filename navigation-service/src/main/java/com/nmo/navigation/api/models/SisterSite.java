package com.sixthday.navigation.api.models;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SisterSite {
    private String name;
    private String url;
    private List<TopCategory> topCategories;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopCategory {
        private String name;
        private String url;
    }
}
