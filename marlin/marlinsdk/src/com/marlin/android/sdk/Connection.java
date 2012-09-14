package com.marlin.android.sdk;

public class Connection {

	private boolean ssl;
	private String loadRate;
	private String connectionTime;
	private String bytesDownloaded;
	private String firstByte;
	private String contentType;
	private String endToEndTime;
	private String startTimeStamp;
	private String dnsTime;

	public boolean isSsl() {
		return ssl;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	public String getLoadRate() {
		return loadRate;
	}

	public void setLoadRate(String loadRate) {
		this.loadRate = loadRate;
	}

	public String getConnectionTime() {
		return connectionTime;
	}

	public void setConnectionTime(String connectionTime) {
		this.connectionTime = connectionTime;
	}

	public String getBytesDownloaded() {
		return bytesDownloaded;
	}

	public void setBytesDownloaded(String bytesDownloaded) {
		this.bytesDownloaded = bytesDownloaded;
	}

	public String getFirstByte() {
		return firstByte;
	}

	public void setFirstByte(String firstByte) {
		this.firstByte = firstByte;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getEndToEndTime() {
		return endToEndTime;
	}

	public void setEndToEndTime(String endToEndTime) {
		this.endToEndTime = endToEndTime;
	}

	public String getStartTimeStamp() {
		return startTimeStamp;
	}

	public void setStartTimeStamp(String startTimeStamp) {
		this.startTimeStamp = startTimeStamp;
	}

	public String getDnsTime() {
		return dnsTime;
	}

	public void setDnsTime(String dnsTime) {
		this.dnsTime = dnsTime;
	}

	@Override
	public String toString() {
		return "Connection [bytesDownloaded=" + bytesDownloaded
				+ ", connectionTime=" + connectionTime + ", contentType="
				+ contentType + ", dnsTime=" + dnsTime + ", endToEndTime="
				+ endToEndTime + ", firstByte=" + firstByte + ", loadRate="
				+ loadRate + ", ssl=" + ssl + ", startTimeStamp="
				+ startTimeStamp + "]";
	}
}
