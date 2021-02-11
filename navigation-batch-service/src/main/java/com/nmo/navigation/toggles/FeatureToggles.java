package com.sixthday.navigation.toggles;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FeatureToggles {

    private static Boolean getToggleConfigValue(String toggleName) {
        Optional<Boolean> toggleOption = Optional.ofNullable(ServiceFeatureToggleConfig.toggles.get(toggleName));
        return toggleOption.orElse(false);
    }

    public boolean isEnabled(String featureName) {
        return getToggleConfigValue(featureName);
    }
}