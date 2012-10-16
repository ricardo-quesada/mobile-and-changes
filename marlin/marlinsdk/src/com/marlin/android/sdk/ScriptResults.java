package com.marlin.android.sdk;

import java.util.Arrays;

public class ScriptResults {

	private String scriptId;
	private Event[] events;

	public String getScriptId() {
		return scriptId;
	}

	public void setScriptId(String scriptId) {
		this.scriptId = scriptId;
	}

	public Event[] getEvents() {
		return events;
	}

	public void setEvents(Event[] events) {
		this.events = events;
	}

	@Override
	public String toString() {
		return "ScriptResults [events=" + Arrays.toString(events)
				+ ", scriptId=" + scriptId + "]";
	}

}
