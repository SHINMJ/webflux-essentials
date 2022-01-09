package com.example.webfluxessentials.exception;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * defaultErrorAttributes 를 확장하여 custom error message를 세팅할 수 있다.
 * GlobalExceptionHeandler.formatErrorResponse -> ErrorAttributeOptions 에 default 로 추가됨.
 */

@Component
public class CustomAttribute extends DefaultErrorAttributes {

    /**
     * custom error message
     *
     * @param request
     * @param options
     * @return
     */
    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(request, options);
        Throwable throwable = getError(request);

        if ( throwable instanceof ResponseStatusException) {
            ResponseStatusException ex = (ResponseStatusException) throwable;
            errorAttributes.put("message", ex.getMessage());
            errorAttributes.put("developerMessage", "A ResponseStatusException Happened");

            return errorAttributes;
        }

        return errorAttributes;
    }
}
