package com.redbee.chainbeeapp.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class ErrorHandler {

    private static final String CODE_INTERNAL_ERROR = "INTERNAL_ERROR";
    private static final String CODE_INVALID_REQUEST = "INVALID_REQUEST";

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> handle(Throwable ex) {
        log.error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), ex);
        return buildResponseError(HttpStatus.INTERNAL_SERVER_ERROR, List.of(ex.getMessage()), CODE_INTERNAL_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handle(MethodArgumentNotValidException ex) {
        log.error("Se dispara exception por request body inválido: {}", ex.getMessage());
        return buildResponseError(HttpStatus.BAD_REQUEST, getBindingResultMessages(ex), CODE_INVALID_REQUEST);
    }

    private List<String> getBindingResultMessages(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());
    }

    private ResponseEntity<ErrorResponse> buildResponseError(HttpStatus httpStatus, List<String> messages, String code) {
        final var response = ErrorResponse.builder()
            .messages(messages)
            .code(code)
            .status(httpStatus.value())
            .build();

        return new ResponseEntity<>(response, httpStatus);
    }

    @Builder
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    private static class ErrorResponse {
        @JsonProperty
        List<String> messages;
        @JsonProperty
        String code;
        @JsonProperty
        int status;
    }
}
