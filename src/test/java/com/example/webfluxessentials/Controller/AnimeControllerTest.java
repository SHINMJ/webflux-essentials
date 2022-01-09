package com.example.webfluxessentials.Controller;

import com.example.webfluxessentials.domain.Anime;
import com.example.webfluxessentials.repository.AnimeRepository;
import com.example.webfluxessentials.service.AnimeService;
import com.example.webfluxessentials.util.AnimeCreator;
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
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class AnimeControllerTest {

    @InjectMocks
    private AnimeController animeController;

    @Mock
    private AnimeService animeService;

    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    public static void blockHoundSetup() {
        BlockHound.install();
    }

    @BeforeEach
    public void setup() {
        BDDMockito.when(animeService.findAll())
                .thenReturn(Flux.just(anime));

        BDDMockito.when(animeService.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(animeService.save(AnimeCreator.createAnimeToBeSaved()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(animeService.delete(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());

        BDDMockito.when(animeService.update(AnimeCreator.createValidAnime()))
                .thenReturn(Mono.empty());

        List<Anime> list = new ArrayList<>();
        list.add(AnimeCreator.createAnimeToBeSaved());
        list.add(AnimeCreator.createAnimeToBeSaved());
        BDDMockito.when(animeService.saveAll(list))
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
        StepVerifier.create(animeController.listAll())
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();

    }

    @Test
    @DisplayName("findById returns a Mono of anmie")
    public void 한건조회_성공() throws Exception {
        StepVerifier.create(animeController.findById(1L))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();

    }

    @Test
    @DisplayName("save creates an anime when successful")
    public void 한건저장_성공() throws Exception {
        Anime saved = AnimeCreator.createAnimeToBeSaved();
        StepVerifier.create(animeController.save(saved))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();

    }

    @Test
    @DisplayName("delete removes the anime when successful")
    public void 한건삭제_성공() throws Exception {
        BDDMockito.when(animeService.save(AnimeCreator.createAnimeToBeSaved()))
                .thenReturn(Mono.empty());

        StepVerifier.create(animeController.delete(1L))
                .expectSubscription()
                .verifyComplete();
    }


    @Test
    @DisplayName("update returns empty mono when successful")
    public void 한건수정_성공() throws Exception {

        StepVerifier.create(animeController.update(1L, AnimeCreator.createValidAnime()))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("saveAll creates an anime when successful")
    public void 여러건저장_성공() throws Exception {
        List<Anime> list = new ArrayList<>();
        list.add(AnimeCreator.createAnimeToBeSaved());
        list.add(AnimeCreator.createAnimeToBeSaved());
        StepVerifier.create(animeController.batchSave(list))
                .expectSubscription()
                .expectNext(anime, anime)
                .verifyComplete();

    }


}