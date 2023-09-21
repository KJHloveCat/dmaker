package com.fastcampus.programming.dmaker.exception;

import com.fastcampus.programming.dmaker.dto.DmakerErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class DMakerExceptionHandler {

    @ExceptionHandler(DMakerException.class)
    public DmakerErrorResponse handleException(DMakerException e, HttpServletRequest request) {
        log.error("errorCode: {}, url: {}, message: {}", e.getDMakerErrorCode(),
                request.getRequestURI(), e.getDetailMessage());

        return DmakerErrorResponse.builder()
                .errorCode(e.getDMakerErrorCode())
                .errorMessage(e.getDetailMessage())
                .build();
    }

    @ExceptionHandler(value = {
            HttpRequestMethodNotSupportedException.class,
            MethodArgumentNotValidException.class
    })
    public DmakerErrorResponse handleBadRequest(
            Exception e, HttpServletRequest request
    ) {
        log.error(" url: {}, message: {}",
                request.getRequestURI(), e.getMessage());

        return DmakerErrorResponse.builder()
                .errorCode(DMakerErrorCode.INVALID_REQUEST)
                .errorMessage(DMakerErrorCode.INVALID_REQUEST.getMessage())
                .build();
    }

    @ExceptionHandler(Exception.class)
    public DmakerErrorResponse handleException(
            Exception e, HttpServletRequest request
    ) {
        log.error(" url: {}, message: {}",
                request.getRequestURI(), e.getMessage());

        return DmakerErrorResponse.builder()
                .errorCode(DMakerErrorCode.INTERNAL_SERVER_ERROR)
                .errorMessage(DMakerErrorCode.INTERNAL_SERVER_ERROR.getMessage())
                .build();
    }
}
