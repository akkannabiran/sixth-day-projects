package com.sixthday.navigation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.actuate.info.Info;
import org.springframework.core.env.Environment;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProfilesInfoContributorTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Environment environment;

    @InjectMocks
    private ProfilesInfoContributor profilesInfoContributor;

    @Test
    public void shouldIncludeActiveProfileAndEnvironmentWhenActiveProfilesPresent() {
        String activeProfiles[] = {"dev", "dev-int"};
        when(environment.getActiveProfiles()).thenReturn(activeProfiles);
        Info.Builder builder = new Info.Builder();
        profilesInfoContributor.contribute(builder);
        String profiles[] = (String[]) builder.build().getDetails().get("spring.profiles.active");
        String environment = builder.build().getDetails().get("environment").toString();
        assertArrayEquals(profiles, activeProfiles);
        assertThat(environment, containsString("dev"));
    }

    @Test
    public void shouldIncludeActiveProfileAndEnvironmentWhenActiveProfilesNotAvailable() {
        String activeProfiles[] = {};
        when(environment.getActiveProfiles()).thenReturn(activeProfiles);
        Info.Builder builder = new Info.Builder();
        profilesInfoContributor.contribute(builder);
        String profiles[] = (String[]) builder.build().getDetails().get("spring.profiles.active");
        assertArrayEquals(profiles, activeProfiles);
        assertNull(builder.build().getDetails().get("environment"));
    }
}
