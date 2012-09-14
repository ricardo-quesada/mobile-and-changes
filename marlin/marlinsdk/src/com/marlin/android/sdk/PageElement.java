package com.marlin.android.sdk;

public class PageElement {

	private String elementId;
	private String elementUrl;
	private int redirectCount;
	private String contentType;
	private int resultCode;
	private String resultDesc;
	private boolean availability;
	private String throughput;
	private Connection connection;

	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	public String getElementUrl() {
		return elementUrl;
	}

	public void setElementUrl(String elementUrl) {
		this.elementUrl = elementUrl;
	}

	public int getRedirectCount() {
		return redirectCount;
	}

	public void setRedirectCount(int redirectCount) {
		this.redirectCount = redirectCount;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public int getResultCode() {
		return resultCode;
	}

	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}

	public String getResultDesc() {
		return resultDesc;
	}

	public void setResultDesc(String resultDesc) {
		this.resultDesc = resultDesc;
	}

	public boolean isAvailability() {
		return availability;
	}

	public void setAvailability(boolean availability) {
		this.availability = availability;
	}

	public String getThroughput() {
		return throughput;
	}

	public void setThroughput(String throughput) {
		this.throughput = throughput;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	@Override
	public String toString() {
		return "PageElement [availability=" + availability + ", connection="
				+ connection + ", contentType=" + contentType + ", elementId="
				+ elementId + ", elementUrl=" + elementUrl + ", redirectCount="
				+ redirectCount + ", resultCode=" + resultCode
				+ ", resultDesc=" + resultDesc + ", throughput=" + throughput
				+ "]";
	}
}
