package com.example.webfluxessentials.config;

import com.example.webfluxessentials.service.DevUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.Map;

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity       //메서드별 auth
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        //@formatter:off
        return http
                .csrf().disable()
                .authorizeExchange()
                    .pathMatchers(HttpMethod.POST, "/animes/**").hasRole("ADMIN")
                    .pathMatchers(HttpMethod.PUT, "/animes/**").hasRole("ADMIN")
                    .pathMatchers(HttpMethod.DELETE, "/animes/**").hasRole("ADMIN")
                    .pathMatchers(HttpMethod.GET, "/animes/**").hasRole("USER")
                    .pathMatchers("/swagger-ui.html", "/swagger-ui/**",
                            "/v3/api-docs/**", "/webjars/**").permitAll()
                .anyExchange().authenticated()
                .and()
                    .formLogin()
                .and()
                    .httpBasic()
                .and()
                .build();



    }
// 테스트용
//
//    @Bean
//    public MapReactiveUserDetailsService userDetailsService() {
//        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
//        UserDetails user = User.withUsername("user")
//                .password(encoder.encode("test"))
//                .roles("USER")
//                .build();
//
//        UserDetails admin = User.withUsername("admin")
//                .password(encoder.encode("test"))
//                .roles("USER", "ADMIN")
//                .build();
//
//        return new MapReactiveUserDetailsService(user, admin);
//    }

    /**
     * 로그인 후 호출되며 userdetailservice 호출함
     *
     * @param devUserDetailsService
     * @return
     */
    @Bean
    ReactiveAuthenticationManager authenticationManager(DevUserDetailsService devUserDetailsService) {
        return new UserDetailsRepositoryReactiveAuthenticationManager(devUserDetailsService);
    }
}
