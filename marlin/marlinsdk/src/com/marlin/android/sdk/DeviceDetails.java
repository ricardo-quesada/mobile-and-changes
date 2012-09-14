package com.marlin.android.sdk;

import java.util.Arrays;

public class DeviceDetails {

	private OperatingSystem operatingSystem;
	private Network[] network;
	private Memory memory;
	private Battery battery;
	private Location location;

	public Network[] getNetwork() {
		return network;
	}

	public void setNetwork(Network[] network) {
		this.network = network;
	}

	public Memory getMemory() {
		return memory;
	}

	public void setMemory(Memory memory) {
		this.memory = memory;
	}

	public Battery getBattery() {
		return battery;
	}

	public void setBattery(Battery battery) {
		this.battery = battery;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public DeviceDetails() {
		operatingSystem = OperatingSystem.getInstance();
	}

	public OperatingSystem getOperatingSystem() {
		return operatingSystem;
	}

	public void setOperatingSystem(OperatingSystem operatingSystem) {
		this.operatingSystem = operatingSystem;
	}

	@Override
	public String toString() {
		return "DeviceDetails [battery=" + battery + ", location=" + location
				+ ", memory=" + memory + ", network="
				+ Arrays.toString(network) + ", operatingSystem="
				+ operatingSystem + "]";
	}

}
