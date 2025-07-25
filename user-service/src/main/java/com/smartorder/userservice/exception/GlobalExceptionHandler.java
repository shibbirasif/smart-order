package com.smartorder.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Validation Error");
        problemDetail.setDetail(ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ":" + err.getDefaultMessage())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Invalid input"));
        problemDetail.setType(URI.create("https://smartorder.com/errors/validation"));
        return problemDetail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleJsonParse(HttpMessageNotReadableException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Malformed JSON request");
        problemDetail.setDetail("Your request body was unreadable or invalid.");
        return problemDetail;
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ProblemDetail handleKnownErrors(ErrorResponseException ex) {
        return ex.getBody();
    }

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ProblemDetail handleEmailAlreadyUsed(EmailAlreadyUsedException ex) {
        ProblemDetail problemDetails = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetails.setTitle("Email already used");
        problemDetails.setDetail(ex.getMessage());
        problemDetails.setType(URI.create("https://smartorder.com/errors/email-already-used"));
        return problemDetails;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleUnsupportedMethodException(HttpRequestMethodNotSupportedException ex) {
        ProblemDetail problemDetails = ProblemDetail.forStatus(HttpStatus.METHOD_NOT_ALLOWED);
        problemDetails.setTitle("Method Not Allowed");
        problemDetails.setDetail("The requested method is not supported for this endpoint.");
        return problemDetails;
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<String> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body("Unsupported content type. Please use application/json.");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnknown(Exception ex) {
        ProblemDetail problemDetails = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetails.setTitle("Internal Server Error");
        problemDetails.setDetail("Something went wrong.");
        return problemDetails;
    }
}
