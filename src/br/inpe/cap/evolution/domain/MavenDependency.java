package br.inpe.cap.evolution.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("dependency")
public class MavenDependency extends MavenVersionedEntity {
	
	public MavenDependency(String groupId, String artifactId, String version) {
		super(groupId, artifactId, version);
	}

	@Override
	public String toString() {
		return "MavenDependency [groupId=" + groupId + ", artifactId=" + artifactId + ", version=" + version + "]";
	}

}
