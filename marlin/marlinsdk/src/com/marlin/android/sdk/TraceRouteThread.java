package com.marlin.android.sdk;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class TraceRouteThread extends Thread {

	private String hostName;
	private int noHops;
	private TraceRouteData trData;

	public TraceRouteThread(String hostName, int noHops) {
		this.hostName = hostName;
		this.noHops = noHops;
	}

	public TraceRouteData getResult() {
		return trData;
	}

	@Override
	public void run() {
		Log.d("Marlin", getClass().getName() + ": running tracert for "
				+ hostName);
		trData = new TraceRouteData(hostName);
		try {
			InetAddress ipAddr = InetAddress.getByName(hostName);
			for (int i = 1; i < noHops; i++) {
				String pingResult = ping(ipAddr.getHostAddress(), 1, i);
				String intermediateHost = processProbePingResult(pingResult);
				Log.i("MarlinTraceRoute", "intermediateHost="
						+ intermediateHost);
				TraceRouteHopDetails trhd = new TraceRouteHopDetails();
				trhd.setHopNumber(i);
				trhd.setIpAddress(intermediateHost);
				trData.addHopDetail(trhd);
				if (ipAddr.getHostAddress().equals(intermediateHost)) {
					Log.i("MarlinTraceRoute", "breaking.");
					break;
				}
			}
		} catch (Exception e) {
			Log.e("MarlinTraceRoute", "Error while getting ip address", e);
		}

		for (TraceRouteHopDetails hopDetails : trData.getHopDetails()) {
			String pingResult = ping(hopDetails.getIpAddress(), 1, hopDetails
					.getHopNumber());
			String time = processTimePingResult(pingResult);
			if (time != null && time.trim().length() > 0) {
				double timeDouble = Double.parseDouble(time);
				hopDetails.setTime(timeDouble);
			}
			try {
				InetAddress ipAddr = InetAddress.getByName(hopDetails
						.getIpAddress());
				hopDetails.setHostName(ipAddr.getCanonicalHostName());
			} catch (Exception e) {
				Log.e("MarlinTraceRoute", "Error while getting hostname", e);
			}
		}
	}

	private String ping(String ip, int count, int ttl) {
		StringBuilder output = new StringBuilder();
		try {
			Process process = Runtime.getRuntime().exec(
					"ping -c " + count + " -t " + ttl + " " + ip);

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}
			reader.close();

			process.waitFor();

			Log.v("MarlinTraceRoute", output.toString());
		} catch (Exception e) {
			Log.e("MarlinTraceRoute", "Exception", e);
		}

		return output.toString();
	}

	private String processProbePingResult(String rawOut) {
		String intermediateHost = null;
		String[] lines = rawOut.split("\n");
		for (String line : lines) {
			Matcher matcher = Pattern.compile(".*rom ([0-9\\.]+)")
					.matcher(line);
			if (matcher.lookingAt()) {
				intermediateHost = matcher.group(1);
			}
		}
		return intermediateHost;
	}

	private String processTimePingResult(String rawOut) {
		String time = null;
		String[] lines = rawOut.split("\n");
		for (String line : lines) {
			Matcher matcher = Pattern.compile(
					".*from ([0-9\\.]+).*time=([0-9\\.]+) ms").matcher(line);
			if (matcher.lookingAt()) {
				hostName = matcher.group(1);
				time = matcher.group(2);
			}
		}
		return time;
	}
}
