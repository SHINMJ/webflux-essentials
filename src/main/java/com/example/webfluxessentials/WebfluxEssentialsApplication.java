package com.example.webfluxessentials;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import reactor.blockhound.BlockHound;

@SpringBootApplication
public class WebfluxEssentialsApplication {

//    static {
//        BlockHound.install(
//            builder ->
//                    builder.allowBlockingCallsInside("java.util.UUID", "randomUUOD")
//                .allowBlockingCallsInside("java.io.FilterInpustream", "read")
//        );
//    }
    public static void main(String[] args) {
        SpringApplication.run(WebfluxEssentialsApplication.class, args);
    }

}
