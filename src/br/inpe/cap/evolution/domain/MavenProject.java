package br.inpe.cap.evolution.domain;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

@XStreamAlias("project")
public class MavenProject {
	
	private static final String MAVEN_VARIABLE_MARK = "${";

	@XStreamAlias("dependencies")
	private List<MavenDependency> dependencies;

	@XStreamAlias("properties")
	@XStreamConverter(MavenProjectPropertyConverter.class)
	private List<MavenProjectProperty> properties;
	
	public void setDependencies(List<MavenDependency> dependencies) {
		this.dependencies = dependencies;
	}
	
	public List<MavenDependency> getDependencies() {
		// Initialization here because class is constructed by XStream by reflaction 
		if(dependencies == null) {
			dependencies = new ArrayList<>();
		}
		return dependencies;
	}
	
	public void setProperties(List<MavenProjectProperty> properties) {
		this.properties = properties;
	}

	/**
	 * @return Returns an alphabetic sorted {@link ArrayList} of project properties.
	 */
	public List<MavenProjectProperty> getProperties() {
		// Initialization here because class is constructed by XStream by reflaction 
		if(properties == null) {
			properties = new ArrayList<>();
		}
		return properties;
	}

	public void replaceDependencyVariableVersions() {
		getDependencies().stream()
			.filter(
				(dependency) -> {
					String version = dependency.getVersion();
					return version != null &&
						   version.startsWith(MAVEN_VARIABLE_MARK);
				})
			.forEach(
				(dependency) -> {
					String variableVersionValue = lookupVersionValue(dependency.getVersion());
					dependency.setVersion(variableVersionValue);
				}
			);
		
	}

	/**
	 * @param version Reference of dependency version variable
	 * @return Variable value. If not defined, will return original reference
	 */
	private String lookupVersionValue(String version) {
		String versionVariable = version.substring(version.indexOf(MAVEN_VARIABLE_MARK)+2, version.length()-1);
		for (MavenProjectProperty mavenProjectProperty : getProperties()) {
			if(mavenProjectProperty.getName().equals(versionVariable)) {
				return mavenProjectProperty.getValue();
			}
		}
		
		return version;
	}
	
}
