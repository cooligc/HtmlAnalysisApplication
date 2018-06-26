package com.skc.scout24;

import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


/***
 * This is Global Exception Handler . As of now , this class is Generic to handle Exception
 * @author sitakanta
 *
 */
@ControllerAdvice
public class ApplicationExceptionHandler {

    private static final Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger();
    
    /***
     * Method to handle {@link Exception} type of exception
     * @param e
     * @param model
     * @return
     */
    @ExceptionHandler(Exception.class)
    public String exception(Exception e, Model model){
        model.addAttribute("error","Something went wrong . Please try again after sometime");
        LOGGER.error("Error trace --> ",e);
        return "ops/land";
    }
    
    /***
     * Method to handle {@link IOException} type of exception
     * @param e
     * @param model
     * @return
     */
    @ExceptionHandler(IOException.class)
    public String exception(IOException e, Model model){
        model.addAttribute("error","Something went wrong with respect to Link. Please try again after sometime");
        LOGGER.error("Error trace --> ",e);
        return "ops/land";
    }
}
