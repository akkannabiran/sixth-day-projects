package com.sixthday.store.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class VaultSecrets {
    @Value("${elasticsearch6.user}")
    String elasticSearch6User;

    @Value("${elasticsearch6.password}")
    String elasticSearch6Password;

    @Value("${rabbitmq.username}")
    String rabbitMqUsername;
    @Value("${rabbitmq.password}")
    String rabbitMqPassword;

}
