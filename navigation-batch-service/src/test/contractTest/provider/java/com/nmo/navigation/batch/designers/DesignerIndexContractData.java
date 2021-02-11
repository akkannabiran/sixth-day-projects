package com.sixthday.navigation.batch.designers;

import com.sixthday.model.serializable.designerindex.*;

import java.util.Arrays;
import java.util.Collections;

public class DesignerIndexContractData {

    public static DesignerIndex getDesignerIndex() {
        return DesignerIndex
                .builder()
                .id("cat4870731")
                .name("Women's Clothing")
                .designersByCategory(
                        Arrays.asList(
                                DesignerByCategory
                                        .builder()
                                        .id("cat000730")
                                        .name("View All A-Z")
                                        .url("/c/view-all-a-z")
                                        .build(),
                                DesignerByCategory
                                        .builder()
                                        .id("cat000009")
                                        .name("Women's Clothing")
                                        .url("/c/women")
                                        .build()
                        )
                )
                .designersByIndex(
                        Arrays.asList(
                                DesignersByIndex.builder()
                                        .name("A")
                                        .categories(
                                                Arrays.asList(
                                                        DesignerCategory
                                                                .builder()
                                                                .id("cat46620733")
                                                                .name("A.L.C.")
                                                                .url("/c/a-l-c")
                                                                .adornmentTag("NEW_OR_ANY-TEXT")
                                                                .excludedCountries(Collections.singletonList("US"))
                                                                .build(),
                                                        DesignerCategory
                                                                .builder()
                                                                .id("cat46620733")
                                                                .name("Adidas")
                                                                .url("/c/adidas")
                                                                .excludedCountries(Collections.emptyList())
                                                                .build()
                                                )
                                        )
                                        .build(),
                                DesignersByIndex.builder()
                                        .name("B")
                                        .categories(
                                                Collections.emptyList()
                                        )
                                        .build()
                        )
                )
                .build();
    }
}
