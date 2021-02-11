package com.sixthday.navigation.api.controllers;

import com.sixthday.logger.logging.LoggableEvent;
import com.sixthday.navigation.api.exceptions.HybridLeftNavTreeNotFoundException;
import com.sixthday.navigation.api.models.LeftNavTree;
import com.sixthday.navigation.api.models.NavigationErrorResponse;
import com.sixthday.navigation.api.services.LeftNavTreeService;
import com.sixthday.navigation.config.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class HybridLeftNavController {

    private LeftNavTreeService leftNavTreeService;

    @Autowired
    public HybridLeftNavController(LeftNavTreeService leftNavTreeService) {
        this.leftNavTreeService = leftNavTreeService;
    }

    @GetMapping(value = "/leftnav/{navPath}/hybrid", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    @ResponseBody
    @LoggableEvent(eventType = Constants.API, action = Constants.GET_HYBRID_LEFT_NAV)
    public LeftNavTree getHybridLeftNavTreeForThisNavPath(@PathVariable String navPath,
                                                    @RequestParam(name = "countryCode", defaultValue = "US") String countryCode,
                                                    @RequestParam(name = "source", defaultValue = "topNav") String source,
                                                    @RequestParam(name = "navKeyGroup", required=false) String navKeyGroup) {
        return leftNavTreeService.getHybridLeftNavByNavPath(navPath, countryCode, source, navKeyGroup);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(HybridLeftNavTreeNotFoundException.class)
    public NavigationErrorResponse handleHybridLeftNavTreeNotFoundException(HybridLeftNavTreeNotFoundException hybridLeftNavNotFoundException) {
        log.debug(", event_type=\"API\", action=\"GET_HYBRID_LEFT_NAV\", ExceptionTrace=", hybridLeftNavNotFoundException);
        return new NavigationErrorResponse(hybridLeftNavNotFoundException.getMessage(), HttpStatus.NOT_FOUND.value());
    }
}
