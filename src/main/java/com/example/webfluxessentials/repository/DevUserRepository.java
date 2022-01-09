package com.example.webfluxessentials.repository;

import com.example.webfluxessentials.domain.DevUser;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface DevUserRepository extends R2dbcRepository<DevUser, Long> {
    Mono<DevUser> findByUsername(String username);
}
