package com.toggler.core.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toggler.core.toggles.Feature;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Slf4j
public class RequestUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Map<String, Boolean> getToggles(HttpServletRequest request) {
        String togglesHeader = request.getHeader(Feature.X_FEATURE_TOGGLES);
        if (togglesHeader == null) {
            return Collections.emptyMap();
        }

        try {
            return objectMapper.readValue(togglesHeader, new TypeReference<Map<String, Boolean>>(){});
        } catch (IOException e) {
            log.error("Failed to parse feature toggles: '{}' from {}", togglesHeader, Feature.X_FEATURE_TOGGLES, e);
            return Collections.emptyMap();
        }
    }
}
