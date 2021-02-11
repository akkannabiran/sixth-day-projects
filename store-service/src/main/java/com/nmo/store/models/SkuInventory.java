package com.sixthday.store.models;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class SkuInventory {
    private String skuId;
    private String inventoryLevel;
    private int quantity;
    private int bopsQuantity;
}
