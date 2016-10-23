package br.inpe.cap.evolution.domain;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("dependencyManagement")
public class MavenDependencyManagement {
	
	@XStreamAlias("dependencies")
	private List<MavenDependency> dependencies;
	
	public List<MavenDependency> getDependencies() {
		// Initialization here because class is constructed by XStream by reflection 
		if(dependencies == null) {
			dependencies = new ArrayList<>();
		}
		return dependencies;
	}
	
}
