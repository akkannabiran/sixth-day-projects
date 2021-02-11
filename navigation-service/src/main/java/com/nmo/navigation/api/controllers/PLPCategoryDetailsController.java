package com.sixthday.navigation.api.controllers;

import com.sixthday.logger.logging.LoggableEvent;
import com.sixthday.navigation.api.exceptions.CategoryNotFoundException;
import com.sixthday.navigation.api.mappers.PLPCategoryDetailsMapper;
import com.sixthday.navigation.api.models.NavigationErrorResponse;
import com.sixthday.navigation.api.models.response.PLPCategoryDetails;
import com.sixthday.navigation.api.services.CategoryService;
import com.sixthday.navigation.config.Constants;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@Slf4j
public class PLPCategoryDetailsController {

    private CategoryService categoryService;
    private PLPCategoryDetailsMapper plpCategoryDetailsMapper;

    @Autowired
    public PLPCategoryDetailsController(final CategoryService categoryService, final PLPCategoryDetailsMapper plpCategoryDetailsMapper) {
        this.categoryService = categoryService;
        this.plpCategoryDetailsMapper = plpCategoryDetailsMapper;
    }

    @GetMapping(value = "/categories/{categoryId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @LoggableEvent(eventType = Constants.API, action = Constants.GET_PLP_CATEGORY)
    public PLPCategoryDetails getCategoryDetailsForPLP(
            @PathVariable("categoryId") @NonNull final String categoryId,
            @RequestParam Optional<String> parentCategoryId,
            @RequestParam Optional<String> siloCategoryId) {
        return plpCategoryDetailsMapper.map(categoryService.getCategoryDocument(categoryId), parentCategoryId, siloCategoryId);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(CategoryNotFoundException.class)
    public NavigationErrorResponse handleCategoryNotFoundException(CategoryNotFoundException categoryNotFoundException) {
        log.debug(", event_type=\"API\", action=\"GET_PLP_CATEGORY\", ExceptionTrace=", categoryNotFoundException);
        return new NavigationErrorResponse(categoryNotFoundException.getMessage(), HttpStatus.NOT_FOUND.value());
    }
}
