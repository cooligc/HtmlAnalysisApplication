package com.skc.scout24;

import java.io.Serializable;

/***
 * Model class for html Link
 * @author sitakanta
 *
 */
public class Link implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String href;
    private String type;
    private String absoluteURL;
    private Boolean isAccessable;
    private String errorString;

    public Link() {
    }

    public Link(String href, String type,String absoluteURL) {
        this.href = href;
        this.type = type;
        this.absoluteURL = absoluteURL;
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

	public String getAbsoluteURL() {
		return absoluteURL;
	}

	public void setAbsoluteURL(String absoluteURL) {
		this.absoluteURL = absoluteURL;
	}

	public Boolean getIsAccessable() {
		return isAccessable;
	}

	public void setIsAccessable(Boolean isAccessable) {
		this.isAccessable = isAccessable;
	}

	public String getErrorString() {
		return errorString;
	}

	public void setErrorString(String errorString) {
		this.errorString = errorString;
	}
}
