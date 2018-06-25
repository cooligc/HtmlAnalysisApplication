package com.skc.scout24;

import org.apache.logging.log4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
public class ApplicationExceptionHandler {

    private static final Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger();

    @ExceptionHandler(Exception.class)
    public String exception(Exception e, Model model){
        model.addAttribute("error","Something went wrong . Please try again after sometime");
        LOGGER.error("Error trace --> ",e);
        return "ops/land";
    }
}
