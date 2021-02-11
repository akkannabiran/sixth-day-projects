package com.sixthday.store.data;

import com.sixthday.store.models.sixthdayStore;
import com.sixthday.store.models.sixthdayStore.sixthdayStoreEvent;
import com.sixthday.store.models.SkuAvailabilityInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class sixthdayStoresBuilder {

    public static List<sixthdayStore> build(Integer noOfStores) {
        List<sixthdayStore> stores = new ArrayList<>();
        SkuAvailabilityInfo info = new SkuAvailabilityInfo(new StoreSkuInventoryDocumentBuilder().build(), 1);
        for (Integer storeNumber = 1; storeNumber <= noOfStores; storeNumber++) {
            sixthdayStore store = new sixthdayStore(storeNumber.toString(), "storeName", "storeId", "image", "addressLine1",
                    "addressLine2", "city", "phoneNumber", "storeHours",
                    new sixthdayStore.SkuAvailability(info.getAvailabilityStatus(),info.isInventoryAvailable(),info.getAddToCartMessage()),
                    Collections.singletonList(new sixthdayStoreEvent("name", "description", "eventTypeDuration")));
            stores.add(store);
        }
        return stores;
    }
}
