package com.sixthday.navigation.api.executors;

import com.sixthday.logger.logging.Loggable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Loggable
public class AsyncServiceRequest {

    private Executor asynExecutor;
    private Executor nonContextAwareAsyncExecutor;

    public AsyncServiceRequest(@Qualifier(value = "requestContextAwareExecutor") Executor asyncExecutor, @Qualifier(value = "nonContextAwareExecutor") Executor nonContextAwareAsyncExecutor) {
        this.asynExecutor = asyncExecutor;
        this.nonContextAwareAsyncExecutor = nonContextAwareAsyncExecutor;
    }

    public <T>CompletableFuture<T> createRequest(final Supplier<T> request) {
        return CompletableFuture.supplyAsync(request, asynExecutor);
    }
    
    public <T>CompletableFuture<T> createContextUnawareRequest(final Supplier<T> request) {
        return CompletableFuture.supplyAsync(request, nonContextAwareAsyncExecutor);
    }
}
