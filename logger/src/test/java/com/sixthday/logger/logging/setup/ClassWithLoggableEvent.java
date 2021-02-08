package com.sixthday.logger.logging.setup;

import com.sixthday.logger.logging.Loggable;
import com.sixthday.logger.logging.LoggableEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Loggable
public class ClassWithLoggableEvent {

    @LoggableEvent(eventType = "EVENT_TYPE", action = "ACTION")
    public String logSuccessFulEvent(String data) {
        return data;
    }

    @LoggableEvent(eventType = "EVENT_TYPE", action = "ACTION")
    public String logFailedEvent(String data) throws Exception {
        throw new Exception("Failed event");
    }

    @LoggableEvent(eventType = "EVENT_TYPE", action = "ACTION")
    public ResponseEntity successStatusCode(String data) throws Exception {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @LoggableEvent(eventType = "EVENT_TYPE", action = "ACTION")
    public ResponseEntity serverErrorStatusCode(String data) throws Exception {
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @LoggableEvent(eventType = "EVENT_TYPE", action = "ACTION")
    public ResponseEntity clientErrorStatusCode(String data) throws Exception {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
