package com.marlin.android.sdk;

import java.util.List;

public class Event {

	private String eventId;
	private String description;
	private int redirectCount;
	private int resultCode;
	private String resultDesc;
	private boolean availability;
	private String throughput;
	private String powerConsumption;
	private String signalStrength;
	private Connection connection;
	private String url;
	private List<PageElement> pageElements;

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getRedirectCount() {
		return redirectCount;
	}

	public void setRedirectCount(int redirectCount) {
		this.redirectCount = redirectCount;
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

	public boolean getAvailability() {
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

	public String getPowerConsumption() {
		return powerConsumption;
	}

	public void setPowerConsumption(String powerConsumption) {
		this.powerConsumption = powerConsumption;
	}

	public String getSignalStrength() {
		return signalStrength;
	}

	public void setSignalStrength(String signalStrength) {
		this.signalStrength = signalStrength;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<PageElement> getPageElements() {
		return pageElements;
	}

	public void setPageElements(List<PageElement> pageElements) {
		this.pageElements = pageElements;
	}

	@Override
	public String toString() {
		return "Event [availability=" + availability + ", connection="
				+ connection + ", description=" + description + ", eventId="
				+ eventId + ", pageElements=" + pageElements
				+ ", powerConsumption=" + powerConsumption + ", redirectCount="
				+ redirectCount + ", resultCode=" + resultCode
				+ ", resultDesc=" + resultDesc + ", signalStrength="
				+ signalStrength + ", throughput=" + throughput + ", url="
				+ url + "]";
	}
}
