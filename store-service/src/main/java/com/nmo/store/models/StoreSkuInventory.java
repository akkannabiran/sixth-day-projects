package com.sixthday.store.models;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class StoreSkuInventory {
    private Store store;
    private List<SkuInventory> skuInventories = new ArrayList<>();
}
