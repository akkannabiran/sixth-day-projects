package com.toggler.core.utils;

import com.toggler.core.toggles.FeatureToggleConfig;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springframework.web.context.request.RequestContextHolder;

public class FeatureToggleRepository implements TestRule {

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                RequestContextHolder.resetRequestAttributes();
                FeatureToggleConfig.reset();
                base.evaluate();
            }
        };
    }

    public void enable(String toggleName) {
        FeatureToggleConfig.enable(toggleName);
    }

    public void disable(String toggleName) {
        FeatureToggleConfig.disable(toggleName);
    }
}
