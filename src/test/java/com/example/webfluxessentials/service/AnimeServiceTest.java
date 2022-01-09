package com.example.webfluxessentials.service;

import com.example.webfluxessentials.domain.Anime;
import com.example.webfluxessentials.repository.AnimeRepository;
import com.example.webfluxessentials.util.AnimeCreator;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class AnimeServiceTest {

    @InjectMocks
    private AnimeService animeService;

    @Mock
    private AnimeRepository animeRepository;

    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    public static void blockHoundSetup() {
        BlockHound.install();
    }

    @BeforeEach
    public void setup() {
        BDDMockito.when(animeRepository.findAll())
                .thenReturn(Flux.just(anime));

        BDDMockito.when(animeRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(animeRepository.save(AnimeCreator.createAnimeToBeSaved()))
            .thenReturn(Mono.just(anime));

        BDDMockito.when(animeRepository.delete(ArgumentMatchers.any(Anime.class)))
                .thenReturn(Mono.empty());

        BDDMockito.when(animeRepository.save(AnimeCreator.createValidAnime()))
                .thenReturn(Mono.empty());

        List<Anime> list = new ArrayList<>();
        list.add(AnimeCreator.createAnimeToBeSaved());
        list.add(AnimeCreator.createAnimeToBeSaved());
        BDDMockito.when(animeRepository.saveAll(list))
                .thenReturn(Flux.just(anime, anime));

    }

    @Test
    public void blockhoundWorks() throws Exception {
        try {
            FutureTask<?> task = new FutureTask<>(() -> {
                Thread.sleep(0);
                return "";
            });

            Schedulers.parallel().schedule(task);

            task.get(10, TimeUnit.SECONDS);

            Assertions.fail("should fail");
        }catch (Exception e) {
            Assertions.assertTrue(e.getCause() instanceof BlockingOperationError);
        }

    }

    @Test
    @DisplayName("findAll returns a flux of anmie")
    public void 전체조회_성공() throws Exception {
        StepVerifier.create(animeService.findAll())
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();

    }

    @Test
    @DisplayName("findById returns a Mono of anmie")
    public void 한건조회_성공() throws Exception {
        StepVerifier.create(animeService.findById(1L))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();

    }

    @Test
    @DisplayName("findById returns Mono Error when anime dose not exits")
    public void 한건조회_실패_mono_error() throws Exception {
        BDDMockito.when(animeRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());

        StepVerifier.create(animeService.findById(1L))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();

    }

    @Test
    @DisplayName("save creates an anime when successful")
    public void 한건저장_성공() throws Exception {
        Anime saved = AnimeCreator.createAnimeToBeSaved();
        StepVerifier.create(animeService.save(saved))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();

    }

    @Test
    @DisplayName("update returns empty mono when successful")
    public void 한건수정_성공() throws Exception {

        StepVerifier.create(animeService.update(AnimeCreator.createValidAnime()))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("update returns mono error")
    public void 한건수정_실패() throws Exception {
        BDDMockito.when(animeRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());

        StepVerifier.create(animeService.update(AnimeCreator.createValidAnime()))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }



    @Test
    @DisplayName("delete removes the anime when successful")
    public void 한건삭제_성공() throws Exception {
        BDDMockito.when(animeRepository.save(AnimeCreator.createAnimeToBeSaved()))
                .thenReturn(Mono.empty());

        StepVerifier.create(animeService.delete(1L))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("delete returns mono error when anime dose not exist")
    public void 한건삭제_실패() throws Exception {

        BDDMockito.when(animeRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());

        StepVerifier.create(animeService.delete(1L))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("saveAll creates an anime when successful")
    public void 여러건저장_성공() throws Exception {
        List<Anime> list = new ArrayList<>();
        list.add(AnimeCreator.createAnimeToBeSaved());
        list.add(AnimeCreator.createAnimeToBeSaved());
        StepVerifier.create(animeService.saveAll(list))
                .expectSubscription()
                .expectNext(anime, anime)
                .verifyComplete();

    }

    @Test
    @DisplayName("saveAll returns Mono error when one of the objects in the list contains empty or null name")
    public void 여러건저장_실패() throws Exception {
        List<Anime> list = new ArrayList<>();
        list.add(AnimeCreator.createAnimeToBeSaved());
        list.add(AnimeCreator.createAnimeToBeSaved().withName(""));

        BDDMockito.when(animeRepository.saveAll(ArgumentMatchers.anyIterable()))
                .thenReturn(Flux.just(anime, anime.withName("")));


        StepVerifier.create(animeService.saveAll(list))
                .expectSubscription()
                .expectNext(anime)
                .expectError(ResponseStatusException.class)
                .verify();

    }

}