package com.marlin.android.sdk;

import java.util.ArrayList;
import java.util.List;

public class TraceRouteData {

	private String hostName;
	private List<TraceRouteHopDetails> hopDetails;

	public TraceRouteData(String hostName) {
		this.hostName = hostName;
		this.hopDetails = new ArrayList<TraceRouteHopDetails>();
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public List<TraceRouteHopDetails> getHopDetails() {
		return hopDetails;
	}

	public void setHopDetails(List<TraceRouteHopDetails> hopDetails) {
		this.hopDetails = hopDetails;
	}

	public void addHopDetail(TraceRouteHopDetails hopDetails) {
		this.hopDetails.add(hopDetails);
	}

	@Override
	public String toString() {
		return "TraceRouteData [hopDetails=" + hopDetails + ", hostName="
				+ hostName + "]";
	}

}
