package com.whitelabel.gateway.config;

import com.whitelabel.gateway.enums.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(spec -> spec
                        .pathMatchers("/eureka/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/stocks/**").permitAll()
                        .pathMatchers("/api/v1/products/**").hasRole(Role.ADMIN.name())
                        .pathMatchers("/api/v1/stocks/**").hasRole(Role.ADMIN.name())

                        .pathMatchers(HttpMethod.POST, "/api/v1/orders/**").hasRole(Role.USER.name())
                        .pathMatchers(HttpMethod.GET, "/api/v1/orders/**").hasAnyRole(Role.ADMIN.name(), Role.USER.name())
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/orders/**").hasRole(Role.ADMIN.name())
                        .pathMatchers(HttpMethod.PUT, "/api/v1/orders/**").hasRole(Role.ADMIN.name())
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(reactiveJwt())));

        return http.build();
    }

    private ReactiveJwtAuthenticationConverterAdapter reactiveJwt() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> relmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");

            if (relmAccess == null || relmAccess.isEmpty()) {
                return Collections.emptyList();
            }

            Collection<String> roles = (Collection<String>) relmAccess.get("roles");

            return roles
                    .stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        });

        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }
}
