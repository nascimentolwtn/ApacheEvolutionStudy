package br.inpe.cap.apache.domain;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

@XStreamAlias("project")
public class MavenProject {
	
	private static final String MAVEN_VARIABLE_MARK = "${";

	@XStreamAlias("dependencies")
	private List<MavenDependency> dependencies = new ArrayList<>();

	@XStreamAlias("properties")
	@XStreamConverter(MavenProjectPropertyConverter.class)
	private List<MavenProjectProperty> properties;
	
	public void setDependencies(List<MavenDependency> dependencies) {
		this.dependencies = dependencies;
	}
	
	public List<MavenDependency> getDependencies() {
		return dependencies;
	}
	
	public void setProperties(List<MavenProjectProperty> properties) {
		this.properties = properties;
	}

	/**
	 * @return Returns an alphabetic sorted {@link ArrayList} of project properties.
	 */
	public List<MavenProjectProperty> getProperties() {
		return properties;
	}

	public void replaceDependencyVariableVersions() {
		dependencies.stream()
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

	private String lookupVersionValue(String version) {
		String versionVariable = version.substring(version.indexOf(MAVEN_VARIABLE_MARK)+2, version.length()-1);
		String versionValue = null;
		for (MavenProjectProperty mavenProjectProperty : properties) {
			if(mavenProjectProperty.getName().equals(versionVariable)) {
				versionValue = mavenProjectProperty.getValue();
				break;
			}
		}
		return versionValue;
	}
	
}
