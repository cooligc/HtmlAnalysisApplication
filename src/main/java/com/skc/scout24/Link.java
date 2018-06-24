package com.skc.scout24;

import java.io.Serializable;

public class Link implements Serializable{
    private String href;
    private String type;

    public Link() {
    }

    public Link(String href, String type) {
        this.href = href;
        this.type = type;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
