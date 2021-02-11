package com.sixthday.navigation.api.controllers;

import com.sixthday.logger.logging.LoggableEvent;
import com.sixthday.navigation.api.exceptions.LeftNavTreeNotFoundException;
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
public class LeftNavTreeController {

    private LeftNavTreeService leftNavTreeService;

    @Autowired
    public LeftNavTreeController(LeftNavTreeService leftNavTreeService) {
        this.leftNavTreeService = leftNavTreeService;
    }

    @GetMapping(value = "/leftnav/{navPath}", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    @ResponseBody
    @LoggableEvent(eventType = Constants.API, action = Constants.GET_LEFT_NAV)
    public LeftNavTree getLeftNavTreeForThisNavPath(@PathVariable String navPath,
                                                    @RequestParam(name = "countryCode", defaultValue = "US") String countryCode,
                                                    @RequestParam(name = "source", defaultValue = "topNav") String source,
                                                    @RequestParam(name = "navKeyGroup", required=false) String navKeyGroup) {
        return leftNavTreeService.getLeftNavTreeByNavPath(navPath, countryCode, source, navKeyGroup);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(LeftNavTreeNotFoundException.class)
    public NavigationErrorResponse handleLeftNavTreeNotFoundException(LeftNavTreeNotFoundException leftNavTreeNotFoundException) {
        log.debug(", event_type=\"API\", action=\"GET_LEFT_NAV\", ExceptionTrace=", leftNavTreeNotFoundException);
        return new NavigationErrorResponse(leftNavTreeNotFoundException.getMessage(), HttpStatus.NOT_FOUND.value());
    }
}
