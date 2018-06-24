package com.skc.scout24;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApplicationExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String exception(Exception e){
        e.printStackTrace();
        return "ops/land";
    }
}
