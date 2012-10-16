package com.marlin.android.sdk;

import android.os.Build;

public class OperatingSystem {

	private String name;
	private String model;
	private String version;

	public static OperatingSystem _instance = new OperatingSystem();

	private OperatingSystem() {
		name = "Android";
		model = Build.MODEL;
		version = Build.VERSION.RELEASE;
	}

	public static OperatingSystem getInstance() {
		return _instance;
	}

	public String getName() {
		return name;
	}

	public String getModel() {
		return model;
	}

	public String getVersion() {
		return version;
	}
}
