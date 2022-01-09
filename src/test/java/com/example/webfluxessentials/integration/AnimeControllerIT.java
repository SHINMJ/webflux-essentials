package com.example.webfluxessentials.integration;

import com.example.webfluxessentials.domain.Anime;
import com.example.webfluxessentials.exception.CustomAttribute;
import com.example.webfluxessentials.repository.AnimeRepository;
import com.example.webfluxessentials.service.AnimeService;
import com.example.webfluxessentials.util.AnimeCreator;
import com.example.webfluxessentials.util.WebTestClientUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
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


@ExtendWith(SpringExtension.class)
//only on Spring WebFlux components.
//@WebFluxTest
////@WebFluxTest 는 webflux component만 포함하므로 그외 필요한 클래스는 import 해주어야 한다.
//@Import({AnimeService.class, CustomAttribute.class})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class AnimeControllerIT {

    private static final String ADMIN_USER = "admin";   //userdetailservice를 호출하므로 DB에 입력되어 있는 username
    private static final String REGULAR_USER = "user";

    @MockBean
    private AnimeRepository animeRepository;

    @Autowired
    private WebTestClient client;

    /**
     * 여러 사용자(DB에 입력되어 있는..)를 사용할 경우 아래와 같이 사용하기도 함.
     */
//    @Autowired
//    private WebTestClientUtil webTestClientUtil;
//    private WebTestClient webTestClientUser;        //has ROLE_USER
//    private WebTestClient webTestClientAdmin;       //has ROLE_ADMIN
//    private WebTestClient webTestClientInvalid;     //none role


    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeEach
    public void setup() {
//        webTestClientUser = webTestClientUtil.authenticateClient("user", "test");
//        webTestClientAdmin = webTestClientUtil.authenticateClient("admin", "test");
//        webTestClientInvalid = webTestClientUtil.authenticateClient("invalid", "invalid");

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
    @DisplayName("findAll returns a flux of anmie when user has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    public void 전체조회_성공() throws Exception {
        client.get()
                .uri("/animes")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody().jsonPath("$.[0].id").isEqualTo(anime.getId())
                .jsonPath("$.[0].name").isEqualTo(anime.getName());
    }

    @Test
    @DisplayName("findAll returns forbidden when user has role USER")
    @WithUserDetails(REGULAR_USER)
//    @WithMockUser(roles = "USER")  //role만 지정해서 mock user를 사용할 수도 있다.
    public void 전체조회_ROLE_USER_forbidden_error() throws Exception {
        client.get()
                .uri("/animes")
                .exchange()
                .expectStatus().isForbidden();

    }

    @Test
    @DisplayName("findAll returns unauthorized when is not authenticated")
    public void 전체조회_unahtorized_error() throws Exception {
        client.get()
                .uri("/animes")
                .exchange()
                .expectStatus().isUnauthorized();

    }

    @Test
    @DisplayName("findAll flavor returns a flux of anmie when user has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    public void 전체조회2_성공() throws Exception {
        client.get()
                .uri("/animes")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Anime.class)
                .hasSize(1)
                .contains(anime);
    }

    @Test
    @DisplayName("findById returns a Mono of anmie when user has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    public void 한건조회_성공() throws Exception {
        client.get()
                .uri("/animes/{id}", 1L)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }

    @Test
    @DisplayName("findById returns a Mono of anmie when user has role USER")
    @WithUserDetails(ADMIN_USER)
    public void 한건조회_ROLE_USER_성공() throws Exception {
        client.get()
                .uri("/animes/{id}", 1L)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }

    @Test
    @DisplayName("findById returns Mono Error when anime dose not exits when user has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    public void 한건조회_실패_mono_error() throws Exception {
        BDDMockito.when(animeRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());

        client.get()
                .uri("/animes/{id}", 1L)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened");
    }

    @Test
    @DisplayName("save creates an anime when successful when user has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    public void 한건저장_성공() throws Exception {
        Anime saved = AnimeCreator.createAnimeToBeSaved();

        client.post()
                .uri("/animes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(saved))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }

    @Test
    @DisplayName("save returns mono error with bas request when name is empty when user has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    public void 한건저장_valid_실패() throws Exception {
        Anime saved = AnimeCreator.createAnimeToBeSaved().withName("");

        client.post()
                .uri("/animes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(saved))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    @DisplayName("update returns empty mono when successful when user has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    public void 한건수정_성공() throws Exception {

        client
                .put()
                .uri("/animes/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(anime))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("update returns mono error when user has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    public void 한건수정_실패() throws Exception {
        BDDMockito.when(animeRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());

        client
                .put()
                .uri("animes/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(anime))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened");
    }



    @Test
    @DisplayName("delete removes the anime when successful when user has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    public void 한건삭제_성공() throws Exception {
        BDDMockito.when(animeRepository.save(AnimeCreator.createAnimeToBeSaved()))
                .thenReturn(Mono.empty());

        client.delete()
                .uri("/animes/{id}", 1L)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("delete returns mono error when anime dose not exist when user has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    public void 한건삭제_실패() throws Exception {
        BDDMockito.when(animeRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());

        client.delete()
                .uri("/animes/{id}", 1L)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened");
    }

    @Test
    @DisplayName("saveAll creates an anime when successful when user has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    public void 여러건저장_성공() throws Exception {
        List<Anime> list = new ArrayList<>();
        list.add(AnimeCreator.createAnimeToBeSaved());
        list.add(AnimeCreator.createAnimeToBeSaved());

        client
                .post()
                .uri("/animes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(list))
                .exchange()
                .expectStatus().isCreated()
                .expectBodyList(Anime.class)
                .hasSize(2)
                .contains(anime);


    }

    @Test
    @DisplayName("saveAll returns Mono error when one of the objects in the list contains empty or null name when user has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    public void 여러건저장_실패() throws Exception {
        List<Anime> list = new ArrayList<>();
        list.add(AnimeCreator.createAnimeToBeSaved());
        list.add(AnimeCreator.createAnimeToBeSaved().withName(""));

        BDDMockito.when(animeRepository.saveAll(ArgumentMatchers.anyIterable()))
                .thenReturn(Flux.just(anime, anime.withName("")));

        client
                .post()
                .uri("/animes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(list))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                ;


    }



}
