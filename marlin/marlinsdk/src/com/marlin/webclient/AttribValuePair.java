package com.marlin.webclient;

import java.net.URLEncoder;

/**
 * A simple class to store attribute value pairs
 */
public class AttribValuePair {

	public void setIgnoreAttribCase(boolean ignore) {
		this.ignoreAttribCase = ignore;
	}

	public boolean getIgnoreAttribCase() {
		return ignoreAttribCase;
	}

	/**
	 * empty constructor that does nothing
	 */
	public AttribValuePair() {
	}

	/**
	 * initializes object using an attribute and its values
	 */
	public AttribValuePair(String attrib, String value) {
		this.attrib = attrib;
		this.value = value;
	}

	/**
	 * inializes object using attrib=value string
	 */
	public AttribValuePair(String attribAndValue) {
		setAttribAndValue(attribAndValue);
	}

	/**
	 * set the attrib and value using an attrib=value string
	 */
	protected void setAttribAndValue(String attribAndValue) {
		int pos = 0;
		pos = attribAndValue.indexOf("=");
		if (pos == -1) {
			attrib = attribAndValue;
		} else {
			attrib = attribAndValue.substring(0, pos).trim();
			value = attribAndValue.substring(pos + 1).trim();
			if (value.startsWith("\"") || value.startsWith("'")) {
				value = value.substring(1);
			}
			if (value.endsWith("\"") || value.endsWith("'")) {
				value = value.substring(0, value.length() - 1);
			}
		}
	}

	public String getAttrib() {
		if (ignoreAttribCase) {
			return attrib.toLowerCase();
		} else {
			return attrib;
		}
	}

	public String getValue() {
		return value;
	}

	public String toEncodedString() {
		return URLEncoder.encode(attrib) + "=" + URLEncoder.encode(value);
	}

	public String toString() {
		return attrib + "=\"" + value + "\"";
	}

	private String attrib;
	private String value;
	private boolean ignoreAttribCase = false;
}
