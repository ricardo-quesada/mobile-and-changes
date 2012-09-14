package com.marlin.android.sdk;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Stats {

	private String deviceId;
	private String deviceTime;
	private DeviceDetails deviceDetails;
	private ScriptResults[] scriptResults;

	public Stats(String id) {
		deviceId = id;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		deviceTime = sdf.format(new Date());
	}
	
	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getDeviceTime() {
		return deviceTime;
	}

	public void setDeviceTime(String deviceTime) {
		this.deviceTime = deviceTime;
	}

	public DeviceDetails getDeviceDetails() {
		return deviceDetails;
	}

	public void setDeviceDetails(DeviceDetails deviceDetails) {
		this.deviceDetails = deviceDetails;
	}

	public ScriptResults[] getScriptResults() {
		return scriptResults;
	}

	public void setScriptResults(ScriptResults[] scriptResults) {
		this.scriptResults = scriptResults;
	}

	@Override
	public String toString() {
		return "Stats [deviceDetails=" + deviceDetails + ", deviceId="
				+ deviceId + ", deviceTime=" + deviceTime + ", scriptResults="
				+ Arrays.toString(scriptResults) + "]";
	}

}
