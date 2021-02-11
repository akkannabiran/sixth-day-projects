package com.sixthday.navigation.api.data;

import com.sixthday.navigation.api.models.Silos;
import com.sixthday.navigation.domain.CategoryNode;

import java.util.ArrayList;
import java.util.List;

public class SiloNavTreeTestDataFactory {
    public static final String DEVICE_TYPE_DESKTOP = "desktop";
    public static final String DEVICE_TYPE_MOBILE = "mobile";
    public static final String DEVICE_TYPE_DEFAULT = DEVICE_TYPE_DESKTOP;
    public static final String EXCEPTION_MSG = "Unknown Exception";
    public static final String UNITED_STATES_COUNTRY_CODE = "US";
    public static final String NAV_KEY_GROUP_CONTROL = "A";

    public static Silos getTestSilosForDesktop() {
        Silos desktopSilos = new Silos();
        getSilosTree(desktopSilos, 2);
        return desktopSilos;
    }

    public static Silos getTestSilosForMobile() {
        Silos mobileSilos = new Silos();
        getSilosTree(mobileSilos, 4);
        return mobileSilos;
    }

    public static Silos getInitialMobileTestSilos() {
        Silos initialMobileSilos = new Silos();
        getSilosTree(initialMobileSilos, 3);
        return initialMobileSilos;
    }

    public static void getSilosTree(Silos silos, int numberOfSilos) {
        CategoryNode node = new CategoryNode();
        List<CategoryNode> silosTree = new ArrayList<>();
        for (int siloIndex = 1; siloIndex <= numberOfSilos; siloIndex++) {
            node.setCatmanId("catman" + siloIndex);
            node.setId("cat" + siloIndex);
            node.setName("Designers" + siloIndex);
            node.setUrl("Url" + siloIndex);
            silosTree.add(node);
        }
        silos.setSilosTree(silosTree);
    }
}