package com.marlin.android.sdk;

public class Memory {

	private String total;
	private String free;

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public String getFree() {
		return free;
	}

	public void setFree(String free) {
		this.free = free;
	}

	@Override
	public String toString() {
		return "Memory [free=" + free + ", total=" + total + "]";
	}

}
