package com.sixthday.store.exceptions;

import com.netflix.hystrix.exception.HystrixRuntimeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FAILED_DEPENDENCY;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ResponseStatus(value = FAILED_DEPENDENCY, reason = "got-www communication error")
    @ExceptionHandler({GotWWWCommunicationException.class, GotWWWResponseParseException.class, HystrixRuntimeException.class})
    public void handleGotWWWExceptions(RuntimeException ex){
        logger.error("Unhandled got-www exception: {}", ex.getMessage(), ex);
    }

    @ResponseStatus(value = BAD_REQUEST, reason = "Invalid address or zip code")
    @ExceptionHandler(InvalidLocationException.class)
    public void handleInvalidAddressException(InvalidLocationException ex){
        logger.error("Unhandled invalid address exception: {}", ex.getMessage(), ex);
    }

    @ResponseStatus(value = BAD_REQUEST, reason = "Invalid latitude or longitude")
    @ExceptionHandler(InvalidLatitudeLongitudeException.class)
    public void handleInvalidAddressException(InvalidLatitudeLongitudeException ex){
        logger.error("Invalid latitude or longitude: {}", ex.getMessage(), ex);
    }

    @ResponseStatus(value = NOT_FOUND, reason = "Store is not found with given storeId")
    @ExceptionHandler(DocumentNotFoundException.class)
    public void handleDocumentNotFoundException(DocumentNotFoundException ex){
        logger.error("Store is not found with given storeId: {}", ex.getMessage(), ex);
    }

    @ResponseStatus(value = INTERNAL_SERVER_ERROR, reason = "Unexpected Null Pointer Exception")
    @ExceptionHandler(NullPointerException.class)
    public void handleNullPointerException(NullPointerException ex){
        logger.error("Unexpected Null pointer Exception: {}", ex.getMessage(), ex);
    }

    @ResponseStatus(value = INTERNAL_SERVER_ERROR, reason = "Document retrieval exception")
    @ExceptionHandler(DocumentRetrievalException.class)
    public void handleDocumentRetrievalException(DocumentRetrievalException ex){
        logger.error("Document retrieval exception: {}", ex.getMessage(), ex);
    }
}
