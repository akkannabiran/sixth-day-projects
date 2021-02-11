package com.sixthday.navigation.api.controllers;

import com.sixthday.logger.logging.LoggableEvent;
import com.sixthday.navigation.api.exceptions.CategoryNotFoundException;
import com.sixthday.navigation.api.models.NavigationErrorResponse;
import com.sixthday.navigation.api.models.response.BrandLinks;
import com.sixthday.navigation.api.services.BrandLinksService;
import com.sixthday.navigation.config.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class BrandLinksController {
    private BrandLinksService brandLinksService;

    @Autowired
    public BrandLinksController(final BrandLinksService brandLinksService) {
        this.brandLinksService = brandLinksService;
    }

    @GetMapping("/brandlinks")
    @LoggableEvent(eventType = Constants.API, action = Constants.GET_BRAND_LINKS)
    public BrandLinks getBrandLinks() {
        return brandLinksService.getBrandLinks();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(CategoryNotFoundException.class)
    public NavigationErrorResponse handleCategoryNotFoundException(CategoryNotFoundException categoryNotFoundException) {
        log.debug(", event_type=\"API\", action=\"GET_BRAND_LINKS\", ExceptionTrace=", categoryNotFoundException);
        return new NavigationErrorResponse(categoryNotFoundException.getMessage(), HttpStatus.NOT_FOUND.value());
    }
}
