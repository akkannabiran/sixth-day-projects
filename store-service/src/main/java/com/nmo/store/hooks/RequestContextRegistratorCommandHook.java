package com.sixthday.store.hooks;

import com.netflix.hystrix.HystrixInvokable;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import org.springframework.web.context.request.RequestContextHolder;

public class RequestContextRegistratorCommandHook extends HystrixCommandExecutionHook {

    @Override
    public <T> void onExecutionStart(HystrixInvokable<T> commandInstance) {
        RequestContextHolder.setRequestAttributes(RequestContextHystrixRequestVariable.getInstance().get());
    }

    @Override
    public <T> void onExecutionSuccess(HystrixInvokable<T> commandInstance) {
        RequestContextHolder.resetRequestAttributes();
    }

    @Override
    public <T> Exception onExecutionError(HystrixInvokable<T> commandInstance, Exception e) {
        RequestContextHolder.resetRequestAttributes();
        return e;
    }
}
