package br.inpe.cap.evolution.processor.date.domain;

import java.io.Serializable;

public class Version implements Comparable<Version>, Serializable {
	
	private static final long serialVersionUID = -1573837900880801549L;

	private long timestamp;
	private String versionNumber;
	
	public Version(long timestamp, String number) {
		this.timestamp = timestamp;
		this.versionNumber = number;
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	public String getVersionNumber() {
		return versionNumber;
	}

	public String toString(){
		return versionNumber + " @ " + timestamp;
	}
	
	@Override
	public int compareTo(Version o) {
		return Long.compare(this.timestamp, o.timestamp);
	}
	
}
