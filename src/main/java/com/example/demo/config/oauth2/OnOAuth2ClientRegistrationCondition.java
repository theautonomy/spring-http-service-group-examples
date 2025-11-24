package com.example.demo.config.oauth2;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnOAuth2ClientRegistrationCondition implements Condition {

    private static final String OAUTH2_CLIENT_REGISTRATION_PREFIX =
            "spring.security.oauth2.client.registration";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        try {
            var binder = Binder.get(context.getEnvironment());
            var registrations = binder.bind(OAUTH2_CLIENT_REGISTRATION_PREFIX, java.util.Map.class);
            return registrations.isBound() && !registrations.get().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
