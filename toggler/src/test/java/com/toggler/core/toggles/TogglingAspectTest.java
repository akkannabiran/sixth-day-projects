package com.toggler.core.toggles;


import com.toggler.core.setup.GreetingProvider;
import com.toggler.core.utils.FeatureToggleRepository;
import com.toggler.core.setup.Hello;
import com.toggler.core.setup.MyFeature;
import com.toggler.core.setup.TestConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfig.class)
public class TogglingAspectTest {
    private static final String NAME = "Tim";
    private static final String ENGLISH_GREETING = "Hello " + NAME;
    private static final String GERMAN_GREETING = "Hallo " + NAME;

    @Autowired
    private Hello greeting;

    @Autowired
    private GreetingProvider greetingProvider;

    @Rule
    public FeatureToggleRepository featureToggleRepository = new FeatureToggleRepository();

    @Before
    public void setUp() throws Exception {
        featureToggleRepository.enable(MyFeature.GREET_IN_ENGLISH);
    }

    @Test
    public void shouldReturnEnglishGreetingWhenToggleIsTurnedOn() throws Exception {
        String englishGreeting = greeting.sayHello(NAME);

        assertThat(englishGreeting, is(ENGLISH_GREETING));
    }

    @Test
    public void shouldReturnGermanGreetingWhenToggleIsTurnedOff() throws Exception {
        featureToggleRepository.disable(MyFeature.GREET_IN_ENGLISH);

        String englishGreeting = greeting.sayHello(NAME);

        assertThat(englishGreeting, is(GERMAN_GREETING));
    }

    @Test
    public void shouldCallEnglishGreetingMethodWhenToggleIsTurnedOn() throws Exception {
        String englishGreeting = greetingProvider.sayHello(NAME);

        assertThat(englishGreeting, is(ENGLISH_GREETING));
    }

    @Test
    public void shouldCallGermanGreetingMethodWhenToggleIsTurnedOff() throws Exception {
        featureToggleRepository.disable(MyFeature.GREET_IN_ENGLISH);

        String englishGreeting = greetingProvider.sayHello(NAME);

        assertThat(englishGreeting, is(GERMAN_GREETING));
    }
}