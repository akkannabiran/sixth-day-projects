package com.toggler.core.setup;

import com.toggler.core.toggles.Toggle;
import org.springframework.stereotype.Component;

@Component("hello.english")
@Toggle(name = MyFeature.GREET_IN_ENGLISH, fallback = "hello.german")
public class Hello implements Greeting {
    @Override
    public String sayHello(String name) {
        return "Hello " + name;
    }
}
