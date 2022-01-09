package com.example.webfluxessentials.exception;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

@Component
@Order(-2)
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    public GlobalExceptionHandler(ErrorAttributes errorAttributes,
                                  WebProperties.Resources resources,
                                  ApplicationContext applicationContext,
                                  ServerCodecConfigurer codecConfigurer) {
        super(errorAttributes, resources, applicationContext);
        this.setMessageWriters(codecConfigurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::formatErrorResponse);
    }

    private Mono<ServerResponse> formatErrorResponse(ServerRequest request) {

//        trace=true 파라미터를 주어야 트레이스를 클라이언트에 보여줌
//        e.g. http://localhost:8080/anmies/2?trace=true
        String query = request.uri().getQuery();
        ErrorAttributeOptions errorAttributeOptions = isTraceEnabled(query) ? ErrorAttributeOptions.of(ErrorAttributeOptions.Include.STACK_TRACE)
                : ErrorAttributeOptions.defaults();

        Map<String, Object> map = getErrorAttributes(request, errorAttributeOptions);
        int status = (int) Optional.ofNullable(map.get("status")).orElse(500);

        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(map));
    }

    private boolean isTraceEnabled(String query) {
        return StringUtils.hasText(query) && query.contains("trace=true");
    }
}
