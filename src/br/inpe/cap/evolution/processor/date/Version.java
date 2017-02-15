package br.inpe.cap.evolution.processor.date;

public class Version implements Comparable<Version> {
	
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
