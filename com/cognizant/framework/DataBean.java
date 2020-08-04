package com.cognizant.framework;

public class DataBean {

	private String currentUtterance;
	private String currentUtteranceResponse;
	private String lastUtterance;
	private String lastUtteranceResponse;

	private int countHistory;

	public String getCurrentUtterance() {
		return currentUtterance;
	}

	public void setCurrentUtterance(String currentUtterance) {
		this.currentUtterance = currentUtterance;
	}

	public String getCurrentUtteranceResponse() {
		return currentUtteranceResponse;
	}

	public void setCurrentUtteranceResponse(String currentUtteranceResponse) {
		this.currentUtteranceResponse = currentUtteranceResponse;
	}

	public String getLastUtterance() {
		return lastUtterance;
	}

	public void setLastUtterance(String lastUtterance) {
		this.lastUtterance = lastUtterance;
	}

	public String getLastUtteranceResponse() {
		return lastUtteranceResponse;
	}

	public void setLastUtteranceResponse(String lastUtteranceResponse) {
		this.lastUtteranceResponse = lastUtteranceResponse;
	}

	public int getCountHistory() {
		return countHistory;
	}

	public void setCountHistory(int countHistory) {
		this.countHistory = countHistory;
	}

}
