package br.inpe.cap.evolution.processor.date;

import java.util.Date;
import java.util.Set;

import br.inpe.cap.evolution.processor.date.exception.VersionNotFoundException;

public class Library {

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
	
	public String getVersionLibraryOnDate(long timestamp) {
		Version newerVersion = null;
		for (Version version : versions) {
			if(version.getTimestamp() <= timestamp){
				newerVersion = version;
			}
		}
		
		if (newerVersion == null)
			return "No version found";
		
		return newerVersion.getVersionNumber();
	}

	public Date getReleaseDateOfLibraryVersionNumber(final String versionNumber) throws VersionNotFoundException {
		for (Version version : versions) {
			if(version.getVersionNumber().equals(versionNumber)){
				return new Date(version.getTimestamp());
			}			
		}

		throw new VersionNotFoundException("There isn't a g:" + groupId + "/a:" + artifactId +
										   " @ version " + versionNumber + " available in maven central.");
	}

}
