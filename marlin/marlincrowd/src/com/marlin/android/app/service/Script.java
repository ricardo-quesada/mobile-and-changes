package com.marlin.android.app.service;

import java.util.List;

public class Script {

	private String scriptId;
	private String name;
	private String runAt;
	private List<ScriptEvent> scriptEvents;

	public String getScriptId() {
		return scriptId;
	}

	public void setScriptId(String scriptId) {
		this.scriptId = scriptId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRunAt() {
		return runAt;
	}

	public void setRunAt(String runAt) {
		this.runAt = runAt;
	}

	public List<ScriptEvent> getScriptEvents() {
		return scriptEvents;
	}

	public void setScriptEvents(List<ScriptEvent> scriptEvents) {
		this.scriptEvents = scriptEvents;
	}

	@Override
	public String toString() {
		return "Script [name=" + name + ", runAt=" + runAt + ", scriptEvents="
				+ scriptEvents + ", scriptId=" + scriptId + "]";
	}
}
