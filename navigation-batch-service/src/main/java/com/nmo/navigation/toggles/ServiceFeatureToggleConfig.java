package com.sixthday.navigation.toggles;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "featuretoggles")
@Component
public class ServiceFeatureToggleConfig {
    protected static Map<String, Boolean> toggles = new HashMap<String, Boolean>();

    public static void enable(String toggleName) {
        toggles.put(toggleName, true);
    }

    public static void disable(String toggleName) {
        toggles.put(toggleName, false);
    }
    
    public Map<String, Boolean> getToggles() {
        return toggles;
    }
    
    public void setToggles(Map<String, Boolean> togglesParam) {
        toggles = togglesParam;
    }
}
