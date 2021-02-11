package com.sixthday.actuate.info;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ProfilesInfoContributor implements InfoContributor {
    private final Environment environment;

    @Autowired
    public ProfilesInfoContributor(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void contribute(Info.Builder builder) {
        String[] activeProfiles = environment.getActiveProfiles();
        builder.withDetail("spring.profiles.active", activeProfiles);
        builder.withDetail("environment", activeProfiles.length > 0 ? activeProfiles[0] : null);
    }
}