package com.restaurant.ordering.adapter.in.web;

import com.restaurant.ordering.adapter.in.web.dto.ErrorResponse;
import com.restaurant.ordering.domain.exception.BusinessRuleViolationException;
import com.restaurant.ordering.domain.exception.InvalidOrderStateException;
import com.restaurant.ordering.domain.exception.OrderNotFoundException;
import com.restaurant.ordering.domain.exception.PaymentFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(OrderNotFoundException.class)
    ResponseEntity<ErrorResponse> handleNotFound(OrderNotFoundException ex) {
        return rejected(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidOrderStateException.class)
    ResponseEntity<ErrorResponse> handleInvalidState(InvalidOrderStateException ex) {
        return rejected(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleViolationException ex) {
        return rejected(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(PaymentFailedException.class)
    ResponseEntity<ErrorResponse> handlePaymentFailed(PaymentFailedException ex) {
        // Not logged here: OrderService already logs the decline at WARN.
        return build(HttpStatus.PAYMENT_REQUIRED, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        log.info("Request rejected with 400: validation failed on {}", details);
        ErrorResponse body = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(), "Validation failed", details);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        return rejected(HttpStatus.BAD_REQUEST, "Malformed or invalid request body");
    }

    // INFO, not WARN: these are expected client errors, not faults of this service.
    private ResponseEntity<ErrorResponse> rejected(HttpStatus status, String message) {
        log.info("Request rejected with {}: {}", status.value(), message);
        return build(status, message);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(status.value(), status.getReasonPhrase(), message));
    }
}
