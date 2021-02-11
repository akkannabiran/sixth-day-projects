package com.sixthday.navigation.api.controllers;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sixthday.logger.logging.LoggableEvent;
import com.sixthday.navigation.api.services.CommonNavService;
import com.sixthday.navigation.config.Constants;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class CommonNavController {

    private CommonNavService commonNavService;

    @Autowired
    public CommonNavController(CommonNavService commonNavService) {
        this.commonNavService = commonNavService;
    }

    @RequestMapping(value = "/commonNav", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @LoggableEvent(eventType = Constants.API, action = Constants.GET_COMMON_NAV_RESPONSE)
    @Cacheable(cacheNames= "commonNavCache")
    public Map<String, Object> getCommonNavResponse(@RequestBody Map<String, String> navRequestMap, @RequestParam(required=false) final String navKeyGroup) {
        Map<String, Object> commonNavResponseMap;
        try {
            addInformationForLoggingToMdc(navRequestMap, navKeyGroup);
            commonNavResponseMap = commonNavService.getNavDetails(navRequestMap, navKeyGroup);
        } catch (final Exception exception) {
            commonNavResponseMap = new HashMap<>();
            log.error("Exception occured while fetching common nav response", exception);
        }
        return commonNavResponseMap;
    }

    private void addInformationForLoggingToMdc(Map<String, String> navRequestMap, final String navKeyGroup) {
        if (!StringUtils.isEmpty(navKeyGroup)) {
          MDC.put("NavKeyGroup", navKeyGroup);
        }
        navRequestMap.forEach((navRequest, navEndPoint) -> MDC.put(navRequest, navEndPoint));
    }
}
