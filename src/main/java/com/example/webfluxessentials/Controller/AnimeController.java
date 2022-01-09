package com.example.webfluxessentials.Controller;

import com.example.webfluxessentials.domain.Anime;
import com.example.webfluxessentials.repository.AnimeRepository;
import com.example.webfluxessentials.service.AnimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("animes")
@Slf4j
@RequiredArgsConstructor
@SecurityScheme(
        name = "Basic Authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "basic"
)
public class AnimeController {

    private final AnimeService animeService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "List all animes",
            tags = {"anime"},
            security = @SecurityRequirement(name = "Basic Authentication"))
    public Flux<Anime> listAll() {
        return animeService.findAll();
    }

    @GetMapping(path = "{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "List all animes",
            tags = {"anime"},
            security = @SecurityRequirement(name = "Basic Authentication"))
    public Mono<Anime> findById (@PathVariable long id) {
        return animeService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "List all animes",
            tags = {"anime"},
            security = @SecurityRequirement(name = "Basic Authentication"))
    public Mono<Anime> save(@Valid @RequestBody Anime anime) {
        return animeService.save(anime);
    }


    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "List all animes",
            tags = {"anime"},
            security = @SecurityRequirement(name = "Basic Authentication"))
    public Flux<Anime> batchSave(@Valid @RequestBody List<Anime> animes) {
        return animeService.saveAll(animes);
    }


    @PutMapping(path = "{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "List all animes",
            tags = {"anime"},
            security = @SecurityRequirement(name = "Basic Authentication"))
    public Mono<Void> update(@PathVariable long id, @Valid @RequestBody Anime anime) {
        return animeService.update(anime.withId(id));
    }

    @DeleteMapping(path = "{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "List all animes",
            tags = {"anime"},
            security = @SecurityRequirement(name = "Basic Authentication"))
    public Mono<Void> delete(@PathVariable long id) {
        return animeService.delete(id);
    }



}
