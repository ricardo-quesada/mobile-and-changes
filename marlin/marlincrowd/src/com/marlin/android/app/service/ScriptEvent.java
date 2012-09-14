package com.marlin.android.app.service;

public class ScriptEvent {
	private String eventId;
	private String url;
	private String description;

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "ScriptEvent [description=" + description + ", eventId="
				+ eventId + ", url=" + url + "]";
	}
}
