package com.marlin.android.app;

import android.app.Activity;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.TextView;

import com.marlin.android.sdk.DeviceDetails;
import com.marlin.android.sdk.Network;

public class MyPhoneActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.myphonetab);
		((MarlinApplication) getApplication()).setPhoneActivity(this);
	}

	protected void populateValues(DeviceDetails dd) {
		String osVersionVal = "Fetching..";
		String phoneModelVal = "Fetching..";
		if (dd != null && dd.getOperatingSystem() != null) {
			osVersionVal = dd.getOperatingSystem().getVersion();
			phoneModelVal = dd.getOperatingSystem().getModel();
		}

		String memoryTotalVal = "Fetching..";
		String memoryFreeVal = "Fetching..";
		if (dd != null && dd.getMemory() != null) {
			Long totMem = Long.valueOf(dd.getMemory().getTotal());
			Long freeMem = Long.valueOf(dd.getMemory().getFree());
			memoryTotalVal = totMem / 1024 + " KB";
			memoryFreeVal = freeMem / 1024 + " KB";
		}

		String batteryTypeVal = "Fetching..";
		String batteryLevelVal = "Fetching..";
		String batteryStatusVal = "Fetching..";
		String batteryHealthVal = "Fetching..";
		if (dd != null && dd.getBattery() != null
				&& dd.getBattery().getStatus() != null) {
			batteryTypeVal = dd.getBattery().getType();
			batteryLevelVal = dd.getBattery().getLevel() + "%";
			batteryStatusVal = dd.getBattery().getStatus();
			if (BatteryManager.BATTERY_STATUS_CHARGING == Integer
					.valueOf(batteryStatusVal)) {
				batteryStatusVal = "Charging";
			} else if (BatteryManager.BATTERY_STATUS_DISCHARGING == Integer
					.valueOf(batteryStatusVal)) {
				batteryStatusVal = "Discharging";
			} else if (BatteryManager.BATTERY_STATUS_FULL == Integer
					.valueOf(batteryStatusVal)) {
				batteryStatusVal = "Full";
			} else if (BatteryManager.BATTERY_STATUS_NOT_CHARGING == Integer
					.valueOf(batteryStatusVal)) {
				batteryStatusVal = "Not Charging";
			} else if (BatteryManager.BATTERY_STATUS_UNKNOWN == Integer
					.valueOf(batteryStatusVal)) {
				batteryStatusVal = "Unknown";
			}

			batteryHealthVal = dd.getBattery().getHealth();
			if (BatteryManager.BATTERY_HEALTH_DEAD == Integer
					.valueOf(batteryHealthVal)) {
				batteryHealthVal = "Dead";
			} else if (BatteryManager.BATTERY_HEALTH_GOOD == Integer
					.valueOf(batteryHealthVal)) {
				batteryHealthVal = "Good";
			} else if (BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE == Integer
					.valueOf(batteryHealthVal)) {
				batteryHealthVal = "Over Voltage";
			} else if (BatteryManager.BATTERY_HEALTH_OVERHEAT == Integer
					.valueOf(batteryHealthVal)) {
				batteryHealthVal = "Over Heat";
			} else if (BatteryManager.BATTERY_HEALTH_UNKNOWN == Integer
					.valueOf(batteryHealthVal)) {
				batteryHealthVal = "Unknown";
			} else if (BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE == Integer
					.valueOf(batteryHealthVal)) {
				batteryHealthVal = "Failure";
			}
		}

		String signalVal = "Fetching..";
		String carrierVal = "Fetching..";
		String roamingVal = "Fetching..";
		if (dd != null && dd.getNetwork() != null) {
			Network[] networks = dd.getNetwork();
			for (Network net : networks) {
				signalVal = net.getSignalStrength();
				carrierVal = net.getCarrier();
				roamingVal = net.isRoaming() ? "Yes" : "No";
				break;
			}
		}

		((TextView) findViewById(R.id.osVersionVal)).setText(osVersionVal);
		((TextView) findViewById(R.id.phoneModelVal)).setText(phoneModelVal);

		((TextView) findViewById(R.id.memoryTotalVal)).setText(memoryTotalVal);
		((TextView) findViewById(R.id.memoryFreeVal)).setText(memoryFreeVal);

		((TextView) findViewById(R.id.batteryTypeVal)).setText(batteryTypeVal);
		((TextView) findViewById(R.id.batteryLevelVal))
				.setText(batteryLevelVal);
		((TextView) findViewById(R.id.batteryStatusVal))
				.setText(batteryStatusVal);
		((TextView) findViewById(R.id.batteryHealthVal))
				.setText(batteryHealthVal);

		((TextView) findViewById(R.id.signalVal)).setText(signalVal);
		((TextView) findViewById(R.id.carrierVal)).setText(carrierVal);
		((TextView) findViewById(R.id.roamingVal)).setText(roamingVal);
	}

	@Override
	protected void onResume() {
		super.onResume();
		((MarlinApplication) getApplication()).refreshPhoneData();
	}

}
