package com.marlin.webclient;

import java.net.URLEncoder;

/**
 * This class represents a form field. It is defined by a name and a value.
 * 
 */
public class FormField {

	public String getFieldname() {
		return fieldname;
	}

	public void setFieldname(String fieldname) {
		this.fieldname = fieldname;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * convert to a String represantation
	 * 
	 * @return a fieldname=value string
	 */
	public String toString() {
		return fieldname + "=" + value;
	}

	/**
	 * convert to an URL encoded string (like toString, but uses URLEncoder for
	 * encoding fieldname and value
	 * 
	 * @return a fieldname=value string
	 */
	@SuppressWarnings("deprecation")
	public String toEncodedString() {
		return URLEncoder.encode(fieldname) + "=" + URLEncoder.encode(value);
	}

	private String fieldname;

	private String value;
}
