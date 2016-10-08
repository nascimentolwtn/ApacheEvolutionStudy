package br.inpe.cap.apache.domain;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("project")
public class MavenProject {
	
	@XStreamAlias("dependencies")
	private List<MavenDependency> dependencies = new ArrayList<>();
	
	public void setDependencies(List<MavenDependency> dependencies) {
		this.dependencies = dependencies;
	}
	
	public List<MavenDependency> getDependencies() {
		return dependencies;
	}

	public List<MavenProjectProperty> getProperties() {
		return null;
	}
	
}
