package com.example.webfluxessentials.repository;

import com.example.webfluxessentials.domain.Anime;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface AnimeRepository extends R2dbcRepository<Anime, Long> {
    Mono<Anime> findById(long id);
}
