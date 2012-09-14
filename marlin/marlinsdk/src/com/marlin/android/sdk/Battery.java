package com.marlin.android.sdk;

public class Battery {

	private String type;
	private String level;
	private String status;
	private String health;
	private String plugged;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getHealth() {
		return health;
	}

	public void setHealth(String health) {
		this.health = health;
	}

	public String getPlugged() {
		return plugged;
	}

	public void setPlugged(String plugged) {
		this.plugged = plugged;
	}

	@Override
	public String toString() {
		return "Battery [health=" + health + ", level=" + level + ", plugged="
				+ plugged + ", status=" + status + ", type=" + type + "]";
	}
}
