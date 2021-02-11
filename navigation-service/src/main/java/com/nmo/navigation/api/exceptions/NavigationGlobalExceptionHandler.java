package com.sixthday.navigation.api.exceptions;

import com.sixthday.navigation.api.models.NavigationErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
@RestController
@Slf4j
public class NavigationGlobalExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = {Exception.class})
    public NavigationErrorResponse handleException(Exception exception) {
        log.error(", event_type=\"API\", action=\"GLOBAL_EXCEPTION_HANDLER\", ExceptionTrace=", exception);
        if (exception.getCause() != null)
            return new NavigationErrorResponse(exception.getCause().getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        else
            return new NavigationErrorResponse(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
