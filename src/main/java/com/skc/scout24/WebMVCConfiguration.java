package com.skc.scout24;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/***
 * {@link WebMvcConfigurer} to configure the Login mapping
 * @author sitakanta
 *
 */
@Configuration
@EnableWebMvc
public class WebMVCConfiguration implements WebMvcConfigurer{

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("user/login");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }
}
