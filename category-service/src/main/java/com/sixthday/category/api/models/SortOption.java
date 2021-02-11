package com.sixthday.category.api.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@JsonSerialize(using = SortOptionSerializer.class)
public class SortOption {
    private Option value;
    private Boolean isDefault;

    @Getter
    public enum Option {
        NEWEST_FIRST("Newest First"),
        FEATURED("Featured"),
        PRICE_HIGH_TO_LOW("Price: high to low"),
        PRICE_LOW_TO_HIGH("Price: low to high"),
        BEST_MATCH("Best Match"),
        DISCOUNT_HIGH_TO_LOW("Discount: high to low"),
        DISCOUNT_LOW_TO_HIGH("Discount: low to high"),
        MY_FAVORITES("My Favorites");

        private String name;

        Option(String name) {
            this.name = name;
        }
    }
}
