package com.sixthday.navigation.api.controllers;

import com.sixthday.logger.logging.LoggableEvent;
import com.sixthday.navigation.api.exceptions.BreadcrumbNotFoundException;
import com.sixthday.navigation.api.models.Breadcrumbs;
import com.sixthday.navigation.api.models.NavigationErrorResponse;
import com.sixthday.navigation.api.services.BreadcrumbService;
import com.sixthday.navigation.config.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import static com.sixthday.navigation.config.Constants.API;

@RestController
@Slf4j
public class BreadcrumbController {

    private BreadcrumbService breadcrumbService;

    @Autowired
    public BreadcrumbController(final BreadcrumbService breadcrumbService) {
        this.breadcrumbService = breadcrumbService;
    }

    @GetMapping(value = "/breadcrumbs", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @LoggableEvent(eventType = API, action = Constants.GET_BREADCRUMBS)
    public Breadcrumbs getBreadcrumbs(@RequestParam final String categoryIds,
                                      @RequestParam(name = "source", defaultValue = "topNav") String source,
                                      @RequestParam(name = "navKeyGroup", required=false) String navKeyGroup) {
        return new Breadcrumbs(breadcrumbService.getBreadcrumbs(categoryIds, source, navKeyGroup));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(BreadcrumbNotFoundException.class)
    public NavigationErrorResponse handleBreadcrumbNotFoundException(BreadcrumbNotFoundException breadcrumbNotFoundException) {
        log.debug(", event_type=\"API\", action=\"GET_BREADCRUMBS\", ExceptionTrace=", breadcrumbNotFoundException);
        return new NavigationErrorResponse(breadcrumbNotFoundException.getMessage(), HttpStatus.NOT_FOUND.value());
    }
}
