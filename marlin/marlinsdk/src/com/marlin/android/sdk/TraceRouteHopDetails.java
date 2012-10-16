package com.marlin.android.sdk;

public class TraceRouteHopDetails {

	private int hopNumber;
	private String ipAddress;
	private String hostName;
	private double time;

	public int getHopNumber() {
		return hopNumber;
	}

	public void setHopNumber(int hopNumber) {
		this.hopNumber = hopNumber;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	@Override
	public String toString() {
		return "TraceRouteHopDetails [hopNumber=" + hopNumber + ", hostName="
				+ hostName + ", ipAddress=" + ipAddress + ", time=" + time
				+ "]";
	}
}
