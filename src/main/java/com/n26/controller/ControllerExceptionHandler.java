package com.n26.controller;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(InvalidFormatException.class)
    @ResponseBody
    ErrorResult handleMethodArgumentNotValidException(InvalidFormatException e) {
        ErrorResult errorResult = new ErrorResult();
        errorResult.getFieldErrors()
                .add(new FieldValidationError(
                        e.getPath().get(0).toString(),
                        String.format("Value '%s' must be serializble to: %s", e.getValue(), e.getTargetType())
                ));
        return errorResult;
    }

//    @ResponseStatus(HttpStatus.F)
//    @ExceptionHandler(InvalidFormatException.class)
//    @ResponseBody
//    ErrorResult handleMethodArgumentNotValidException(InvalidFormatException e) {
//        ErrorResult errorResult = new ErrorResult();
//        errorResult.getFieldErrors()
//                .add(new FieldValidationError(
//                        e.getPath().get(0).toString(),
//                        String.format("Value '%s' must be serializble to: %s", e.getValue(), e.getTargetType())
//                ));
//        return errorResult;
//    }

    @Getter
    @NoArgsConstructor
    private static class ErrorResult {
        private final List<FieldValidationError> fieldErrors = new ArrayList<>();

        private ErrorResult(String field, String message) {
            this.fieldErrors.add(new FieldValidationError(field, message));
        }
    }

    @Getter
    @AllArgsConstructor
    private static class FieldValidationError {
        private final String field;
        private final String message;
    }
}
