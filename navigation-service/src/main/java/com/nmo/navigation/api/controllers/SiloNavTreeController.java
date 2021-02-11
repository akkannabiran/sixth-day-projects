package com.sixthday.navigation.api.controllers;

import com.sixthday.logger.logging.LoggableEvent;
import com.sixthday.navigation.api.models.NavigationErrorResponse;
import com.sixthday.navigation.api.models.Silos;
import com.sixthday.navigation.api.services.SiloNavTreeService;
import com.sixthday.navigation.config.Constants;
import com.sixthday.navigation.exceptions.SiloNavTreeNotFoundException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class SiloNavTreeController {

    private static final String DEVICE_TYPE = "device_type";
    private static final String COUNTRY_CODE = "country_code";
    private SiloNavTreeService siloNavTreeService;

    @Autowired
    public SiloNavTreeController(SiloNavTreeService siloNavTreeService) {
        this.siloNavTreeService = siloNavTreeService;
    }

    @GetMapping(value = {"/silos/{countryCode}/{deviceType}"}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    @ResponseBody
    @Cacheable(cacheNames = "siloNavTreesV1")
    @LoggableEvent(eventType = Constants.API, action = Constants.GET_SILOS)
    public Silos getSilos(@PathVariable("countryCode") @NonNull final String countryCode,
                          @PathVariable("deviceType") @NonNull final String deviceType, @RequestParam(required=false) final String navKeyGroup) {
        MDC.put(DEVICE_TYPE, deviceType);
        MDC.put(COUNTRY_CODE, countryCode);
        if (!StringUtils.isEmpty(navKeyGroup)) {
          MDC.put("NavKeyGroup", navKeyGroup);
        }
        return siloNavTreeService.getSilos(countryCode, deviceType, navKeyGroup);
    }

    @GetMapping(value = "/silos/{countryCode}/initial", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    @ResponseBody
    @Cacheable(cacheNames = "siloNavTreesV2")
    @LoggableEvent(eventType = Constants.API, action = Constants.GET_INITIAL_MOBILE_SILOS)
    public Silos getInitialMobileSilos(@PathVariable("countryCode") @NonNull final String countryCode, @RequestParam(required=false) final String navKeyGroup) {
        MDC.put(DEVICE_TYPE, "mobile");
        MDC.put(COUNTRY_CODE, countryCode);
        if (!StringUtils.isEmpty(navKeyGroup)) {
          MDC.put("NavKeyGroup", navKeyGroup);
        }
        return siloNavTreeService.getInitialMobileSilos(countryCode, navKeyGroup);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = {SiloNavTreeNotFoundException.class})
    public NavigationErrorResponse handleSiloNavTreeNotFoundException(SiloNavTreeNotFoundException siloNavTreeNotFoundException) {
        log.debug(", event_type=\"API\", action=\"GET_SILOS\", ExceptionTrace=", siloNavTreeNotFoundException);
        return new NavigationErrorResponse(siloNavTreeNotFoundException.getMessage(), HttpStatus.NOT_FOUND.value());
    }
}
