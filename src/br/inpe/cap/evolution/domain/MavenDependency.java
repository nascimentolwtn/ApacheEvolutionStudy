package br.inpe.cap.evolution.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("dependency")
public class MavenDependency extends MavenVersionedEntity {
	
	private boolean dependencyManaged;
	private boolean versionEverChanged;
	
	public boolean isDependencyManaged() {
		return dependencyManaged;
	}

	public void setDependencyManaged(boolean dependencyManaged) {
		this.dependencyManaged = dependencyManaged;
	}

	@Override
	public String toString() {
		return "MavenDependency [groupId=" + groupId + ", artifactId=" + artifactId + ", version=" + version + "]";
	}

	public void setVersionEverChanged(boolean versionEverChanged) {
		this.versionEverChanged = versionEverChanged;
	}
	
	public boolean versionHasEverChanged() {
		return versionEverChanged;
	}

}
