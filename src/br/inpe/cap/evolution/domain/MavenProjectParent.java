package br.inpe.cap.evolution.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("parent")
public class MavenProjectParent extends MavenVersionedEntity {

	@XStreamAlias("relativePath")
	private String relativePath;
	
	public String getRelativePath() {
		return relativePath;
	}
	
}
