package br.inpe.cap.evolution.processor.date.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import br.inpe.cap.evolution.processor.date.exception.VersionNotFoundException;

public class Library implements Serializable {

	private static final long serialVersionUID = -3970241141577418122L;
	
	private String groupId;
	private String artifactId;
	private Set<Version> versions;
	
	public Library(String groupId, String artifactId, Set<Version> versions) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.versions = versions;
	}
	
	public Set<Version> getVersions() {
		return versions;
	}
	
	public synchronized String getVersionLibraryOnDate(long timestamp) throws VersionNotFoundException {
		Version newerVersion = null;
		for (Version version : versions) {
			if(version.getTimestamp() <= timestamp){
				newerVersion = version;
			}
		}
		
		if (newerVersion == null)
			throw new VersionNotFoundException("Any g:" + groupId + "/a:" + artifactId + " were found in maven central.");
		
		return newerVersion.getVersionNumber();
	}

	public synchronized Date getReleaseDateOfLibraryVersionNumber(final String versionNumber) throws VersionNotFoundException {
		for (Version version : versions) {
			if(version.getVersionNumber().equals(versionNumber)){
				return new Date(version.getTimestamp());
			}			
		}

		throw new VersionNotFoundException("There isn't a g:" + groupId + "/a:" + artifactId +
										   " @ version " + versionNumber + " available in maven central.");
	}
	
	@Override
	public String toString() {
		return this.groupId + this.artifactId + this.versions;
	}

}
