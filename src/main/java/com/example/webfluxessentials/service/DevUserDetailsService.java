package com.example.webfluxessentials.service;

import com.example.webfluxessentials.repository.DevUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class DevUserDetailsService implements ReactiveUserDetailsService {

    private final DevUserRepository devUserRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return devUserRepository.findByUsername(username)
                .cast(UserDetails.class);
    }
}
