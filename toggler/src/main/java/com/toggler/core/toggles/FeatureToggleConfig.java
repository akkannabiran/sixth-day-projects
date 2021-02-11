package com.toggler.core.toggles;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "featuretoggles")
public class FeatureToggleConfig {
    public static Map<String,Boolean> toggles = new HashMap<>();

    public Map<String,Boolean> getToggles() {
        return toggles;
    }

    public void setToggles(Map<String,Boolean> toggles) {
        FeatureToggleConfig.toggles = toggles;
    }

    public static void enable(String toggleName) {
        toggles.put(toggleName, true);
    }

    public static void disable(String toggleName) {
        toggles.put(toggleName, false);
    }

    public static void reset() {
        toggles = new HashMap<>();
    }
}


