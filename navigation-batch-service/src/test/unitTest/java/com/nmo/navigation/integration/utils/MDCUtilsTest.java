package com.sixthday.navigation.integration.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.MDC;

import static com.sixthday.sixthdayLogging.*;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MDC.class)
public class MDCUtilsTest {

    @Test
    public void shouldPutAllContentSyncMDCPropertiesToSetMDCIsCalledWithNonNullValues() {
        PowerMockito.mockStatic(MDC.class);

        MDCUtils.setMDC("MessageId", "MessageType", "ContextId", "OriginTimestamp", "Source", "Destination", "Resource");

        verifyStatic(times(1));
        MDC.put(MDC_MESSAGE_ID, "MessageId");

        PowerMockito.verifyStatic(times(1));
        MDC.put(MDC_MESSAGE_TYPE, "MessageType");

        PowerMockito.verifyStatic(times(1));
        MDC.put(MDC_CONTEXT_ID, "ContextId");

        PowerMockito.verifyStatic(times(1));
        MDC.put(MDC_ORIGIN_TIMESTAMP, "OriginTimestamp");

        PowerMockito.verifyStatic(times(1));
        MDC.put(MDC_SOURCE_PROPERTY, "Source");

        PowerMockito.verifyStatic(times(1));
        MDC.put(MDC_DESTINATION_PROPERTY, "Destination");

        PowerMockito.verifyStatic(times(1));
        MDC.put(MDC_RESOURCE_PROPERTY, "Resource");
    }

    @Test
    public void shouldPutAllContentSyncMDCPropertiesToSetMDCIsCalledWithNullValues() {
        PowerMockito.mockStatic(MDC.class);

        MDCUtils.setMDC(null, null, null, null, null, null, null);

        verifyStatic(times(1));
        MDC.put(MDC_MESSAGE_ID, "-");

        PowerMockito.verifyStatic(times(1));
        MDC.put(MDC_MESSAGE_TYPE, null);

        PowerMockito.verifyStatic(times(1));
        MDC.put(MDC_CONTEXT_ID, "-");

        PowerMockito.verifyStatic(times(1));
        MDC.put(MDC_ORIGIN_TIMESTAMP, "NA");

        PowerMockito.verifyStatic(times(1));
        MDC.put(MDC_SOURCE_PROPERTY, "-");

        PowerMockito.verifyStatic(times(1));
        MDC.put(MDC_DESTINATION_PROPERTY, "-");

        PowerMockito.verifyStatic(times(1));
        MDC.put(MDC_RESOURCE_PROPERTY, "-");
    }

    @Test
    public void shouldPutAllContentSyncMDCPropertiesToSetMDCOnMessageParsingFailureIsCalledWithNonValues() {
        PowerMockito.mockStatic(MDC.class);

        MDCUtils.setMDCOnMessageParsingFailure("Source", "Destination", "Resource");

        verifyStatic(times(1));
        MDC.put(MDC_MESSAGE_ID, "-");

        PowerMockito.verifyStatic(times(1));
        MDC.put(MDC_MESSAGE_TYPE, "DeserializationFailed");

        PowerMockito.verifyStatic(times(1));
        MDC.put(MDC_CONTEXT_ID, "-");

        PowerMockito.verifyStatic(times(1));
        MDC.put(MDC_ORIGIN_TIMESTAMP, "NA");

        PowerMockito.verifyStatic(times(1));
        MDC.put(MDC_SOURCE_PROPERTY, "Source");

        PowerMockito.verifyStatic(times(1));
        MDC.put(MDC_DESTINATION_PROPERTY, "Destination");

        PowerMockito.verifyStatic(times(1));
        MDC.put(MDC_RESOURCE_PROPERTY, "Resource");
    }

    @Test
    public void shouldPutAllContentSyncMDCPropertiesToSetMDCOnMessageParsingFailureIsCalledWithNullValues() {
        PowerMockito.mockStatic(MDC.class);

        MDCUtils.setMDCOnMessageParsingFailure(null, null, null);

        verifyStatic(times(1));
        MDC.put(MDC_MESSAGE_ID, "-");

        PowerMockito.verifyStatic(times(1));
        MDC.put(MDC_MESSAGE_TYPE, "DeserializationFailed");

        PowerMockito.verifyStatic(times(1));
        MDC.put(MDC_CONTEXT_ID, "-");

        PowerMockito.verifyStatic(times(1));
        MDC.put(MDC_ORIGIN_TIMESTAMP, "NA");

        PowerMockito.verifyStatic(times(1));
        MDC.put(MDC_SOURCE_PROPERTY, "-");

        PowerMockito.verifyStatic(times(1));
        MDC.put(MDC_DESTINATION_PROPERTY, "-");

        PowerMockito.verifyStatic(times(1));
        MDC.put(MDC_RESOURCE_PROPERTY, "-");
    }

}
