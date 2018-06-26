package com.skc.scout24;

import java.io.Serializable;

import org.springframework.stereotype.Component;

/**
 * A model class for heading tag like h1, h2 etc on HTML DOM
 * 
 * @author sitakanta
 *
 */
@Component
public class Heading implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String type;
    private String value;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
