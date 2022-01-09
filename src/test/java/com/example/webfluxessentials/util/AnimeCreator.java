package com.example.webfluxessentials.util;

import com.example.webfluxessentials.domain.Anime;

public class AnimeCreator {

    public static Anime createAnimeToBeSaved() {
        return Anime.builder()
                .name("test shinmj")
                .build();
    }
    public static Anime createValidAnime() {
        return Anime.builder()
                .id(1L)
                .name("test shinmj")
                .build();
    }
    public static Anime createValidUpdateAnime() {
        return Anime.builder()
                .id(1L)
                .name("test shinmj 2")
                .build();
    }
}

