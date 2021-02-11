package com.toggler.core.setup;

import com.toggler.core.toggles.Toggle;
import org.springframework.stereotype.Component;

@Component
public class GreetingProvider {

    @Toggle(name = MyFeature.GREET_IN_ENGLISH, fallback = "sayHallo")
    public String sayHello(String name) {
        return "Hello " + name;
    }

    @SuppressWarnings("unused")
    private String sayHallo(String name) {
        return "Hallo " + name;
    }
}
