package com.sixthday.store.hooks;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableDefault;
import org.springframework.web.context.request.RequestAttributes;

public class RequestContextHystrixRequestVariable {
    private static final HystrixRequestVariableDefault<RequestAttributes> requestContextVariable = new HystrixRequestVariableDefault<>();

    private RequestContextHystrixRequestVariable() {}

    public static HystrixRequestVariableDefault<RequestAttributes> getInstance() {
        return requestContextVariable;
    }
}
