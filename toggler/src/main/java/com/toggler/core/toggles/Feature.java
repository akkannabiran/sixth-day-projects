package com.toggler.core.toggles;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static com.toggler.core.utils.RequestUtils.getToggles;


@Slf4j
public class Feature {

    public static final String X_FEATURE_TOGGLES = "X-Feature-Toggles";

    private Feature() {
    }

    public static boolean isEnabled(String toggleName) {
        return getRequest(toggleName)
                .map(request -> getToggles(request).get(toggleName))
                .orElse(getToggleConfigValue(toggleName));
    }

    public static boolean isDisabled(String toggleName) {
        return !isEnabled(toggleName);
    }

    private static Boolean getToggleConfigValue(String toggleName) {
        Optional<Boolean> toggleOption = Optional.ofNullable(FeatureToggleConfig.toggles.get(toggleName));
        return toggleOption.orElse(false);
    }

    private static Optional<HttpServletRequest> getRequest(String toggleName) {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
            if(requestAttributes instanceof ServletRequestAttributes) {
                return Optional.ofNullable(((ServletRequestAttributes) requestAttributes).getRequest());
            }
        } catch (IllegalStateException error) {
            log.error("Could not retrieve HttpServletRequest to check overrides for toggle: {}. Default configuration will be used", toggleName);
            log.debug("Could not retrieve HttpServletRequest", error);
        }
        return Optional.empty();
    }
}