package com.sixthday.category.api.controllers;

import com.sixthday.category.api.models.Category;
import com.sixthday.category.api.models.CategoryErrorResponse;
import com.sixthday.category.api.services.CategoryService;
import com.sixthday.logger.logging.LoggableEvent;
import com.sixthday.logger.logging.LoggingAction;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.sixthday.logger.logging.LoggingEvent.API;

@RestController
@Slf4j
public class CategoryController {

    private CategoryService categoryService;

    @Autowired
    public CategoryController(final CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @ResponseBody
    @LoggableEvent(eventType = API, action = LoggingAction.GET_CATEGORIES)
    @RequestMapping(value = "{countryCode}/categories", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.POST)
    public List<Category> getCategories(@PathVariable final String countryCode, @RequestBody Map<String, String> categoryIds,
                                        @RequestParam Optional<String> siloCategoryId, @RequestParam Optional<String> navPath) {
        MDC.put("CountryCode", "\"" + countryCode + "\"");
        return categoryService.getCategories(categoryIds, siloCategoryId, navPath);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(Exception.class)
    public CategoryErrorResponse handleCategoryControllerException(Exception e) {
        log.debug(", event_type=\"API\", action=\"GET_CATEGORIES\", ExceptionTrace=", e);
        return new CategoryErrorResponse("Unable to process the request, internal error occurred " + e.getMessage(), HttpStatus.NOT_FOUND.value());
    }
}