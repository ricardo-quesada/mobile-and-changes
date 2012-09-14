package com.marlin.android.sdk;

public class Network {

	private String type;
	private boolean available;
	private String phoneNumber;
	private String phoneTechnology;
	private String dataTechnology;
	private String carrier;
	private String signalStrength;
	private boolean roaming;
	private String state;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getPhoneTechnology() {
		return phoneTechnology;
	}

	public void setPhoneTechnology(String phoneTechnology) {
		this.phoneTechnology = phoneTechnology;
	}

	public String getDataTechnology() {
		return dataTechnology;
	}

	public void setDataTechnology(String dataTechnology) {
		this.dataTechnology = dataTechnology;
	}

	public String getCarrier() {
		return carrier;
	}

	public void setCarrier(String carrier) {
		this.carrier = carrier;
	}

	public String getSignalStrength() {
		return signalStrength;
	}

	public void setSignalStrength(String signalStrength) {
		this.signalStrength = signalStrength;
	}

	public boolean isRoaming() {
		return roaming;
	}

	public void setRoaming(boolean roaming) {
		this.roaming = roaming;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "Network [available=" + available + ", carrier=" + carrier
				+ ", dataTechnology=" + dataTechnology + ", phoneNumber="
				+ phoneNumber + ", phoneTechnology=" + phoneTechnology
				+ ", roaming=" + roaming + ", signalStrength=" + signalStrength
				+ ", state=" + state + ", type=" + type + "]";
	}

}
