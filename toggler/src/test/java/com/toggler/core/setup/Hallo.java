package com.toggler.core.setup;

import org.springframework.stereotype.Component;

@Component("hello.german")
public class Hallo implements Greeting {
    @Override
    public String sayHello(String name) {
        return "Hallo " + name;
    }
}
