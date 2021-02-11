package com.sixthday.store.models;

import lombok.Getter;

@Getter
public class StoreSearchLocation {
    private String freeFormAddress;
    private Coordinates coordinates;

    public StoreSearchLocation(String freeFormAddress) {
        this.freeFormAddress = freeFormAddress;
    }

    public StoreSearchLocation(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public String toString() {
        return (freeFormAddress != null) ? ("Address: " + freeFormAddress) : ("Coordinates: " + coordinates.toString());
    }
}
