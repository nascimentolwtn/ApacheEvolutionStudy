package br.inpe.cap.evolution.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("parent")
public class MavenProjectParent extends MavenVersionedEntity {

	public MavenProjectParent(String groupId, String artifactId, String version) {
		super(groupId, artifactId, version);
	}
	
	@XStreamAlias("relativePath")
	private String relativePath;
	
	public String getRelativePath() {
		return relativePath;
	}
	
}
