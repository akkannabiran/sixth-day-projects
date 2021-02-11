package com.sixthday.store.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Getter
@Setter
public class Coordinates {
    @NotNull
    private Float latitude;
    @NotNull
    private Float longitude;

    @Override
    public String toString() {
        return "(" + latitude.toString() + ", " + longitude.toString() + ")";
    }
}
