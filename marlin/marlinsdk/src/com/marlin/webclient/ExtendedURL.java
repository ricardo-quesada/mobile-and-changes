package com.marlin.webclient;

import java.net.URL;

/**
 * This class represents an URL that contains also a argument (e.g. the values
 * for HTTP POST) and a request type (GET/POST)
 */
public class ExtendedURL {

	private URL url = null;
	private int requestMethod = HttpConstants.GET;
	private String params = "";

	/**
	 * Simple constructoir, does nothing special
	 */
	public ExtendedURL() {
	}

	public URL getURL() {
		return this.url;
	}

	public void setURL(URL url) {
		this.url = url;
	}

	public int getRequestMethod() {
		return this.requestMethod;
	}

	public void setRequestMethod(int requestMethod) {
		this.requestMethod = requestMethod;
	}

	public String getParams() {
		return this.params;
	}

	public void setParams(String params) {
		this.params = params;
	}

}
