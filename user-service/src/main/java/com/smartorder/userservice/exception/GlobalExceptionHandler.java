package com.smartorder.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponseException;
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
                .reduce((a,b) -> a + ", " + b)
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

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnknown(Exception ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Internal Server Error");
        pd.setDetail("Something went wrong.");
        return pd;
    }

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ProblemDetail handleEmailAlreadyUsed(EmailAlreadyUsedException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Email already used");
        pd.setDetail(ex.getMessage());
        pd.setType(URI.create("https://smartorder.com/errors/email-already-used"));
        return pd;
    }
}
