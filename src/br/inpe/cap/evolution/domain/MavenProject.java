package br.inpe.cap.evolution.domain;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

@XStreamAlias("project")
public class MavenProject extends MavenVersionedEntity {
	
	public static final String MAVEN_VARIABLE_MARK = "${";
	public static final String MAVEN_PROJECT_GROUPID_VARIABLE = "${project.groupId}";
	public static final String MAVEN_PROJECT_ARTIFACTID_VARIABLE = "${project.artifactId}";
	public static final String MAVEN_PROJECT_VERSION_VARIABLE = "${project.version}";
	
	@XStreamAlias("parent")
	private MavenProjectParent parent;

	@XStreamAlias("dependencies")
	private List<MavenDependency> dependencies;

	@XStreamAlias("properties")
	@XStreamConverter(MavenProjectPropertyConverter.class)
	private List<MavenProjectProperty> properties;
	
	public MavenProject(String groupId, String artifactId, String version) {
		super(groupId, artifactId, version);
	}
	
	public void setDependencies(List<MavenDependency> dependencies) {
		this.dependencies = dependencies;
	}
	
	public List<MavenDependency> getDependencies() {
		// Initialization here because class is constructed by XStream by reflection 
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
		// Initialization here because class is constructed by XStream by reflection 
		if(properties == null) {
			properties = new ArrayList<>();
		}
		return properties;
	}

	public void replaceVariables() {
		getDependencies().stream()
			.forEach(
				(dependency) -> {
					replaceGroupIdVariables(dependency);
					replaceArtifactIdVariables(dependency);
					replaceVersionVariables(dependency);
				}
			);
		
	}

	private void replaceGroupIdVariables(MavenDependency dependency) {
		String oldGroupId = dependency.getGroupId();
		if(isntMavenVariable(oldGroupId)) {
			return;
		}
		if(oldGroupId.startsWith(MAVEN_PROJECT_GROUPID_VARIABLE)) {
			dependency.setGroupId(this.groupId);
		} else {
			String variableGroupIdValue = lookupVariableValue(oldGroupId);
			dependency.setGroupId(variableGroupIdValue);
		}
	}

	private void replaceArtifactIdVariables(MavenDependency dependency) {
		String oldArtifactId = dependency.getArtifactId();
		if(isntMavenVariable(oldArtifactId)) {
			return;
		}
		if(oldArtifactId.startsWith(MAVEN_PROJECT_ARTIFACTID_VARIABLE)) {
			dependency.setArtifactId(this.artifactId);
		} else {
			String variableArtifactIdValue = lookupVariableValue(oldArtifactId);
			dependency.setArtifactId(variableArtifactIdValue);
		}
	}

	private void replaceVersionVariables(MavenDependency dependency) {
		String oldVersion = dependency.getVersion();
		if(isntMavenVariable(oldVersion)) {
			return;
		}
		if(oldVersion.startsWith(MAVEN_PROJECT_VERSION_VARIABLE)) {
			dependency.setVersion(this.version);
		} else {
			String variableVersionValue = lookupVariableValue(oldVersion);
			dependency.setVersion(variableVersionValue);
		}
	}

	private boolean isntMavenVariable(String variable) {
		return variable == null || !variable.startsWith(MAVEN_VARIABLE_MARK);
	}

	/**
	 * @param version Reference of dependency version variable
	 * @return Variable value. If not defined, will return original reference
	 */
	private String lookupVariableValue(String version) {
		String versionVariable = version.substring(version.indexOf(MAVEN_VARIABLE_MARK)+2, version.length()-1);
		for (MavenProjectProperty mavenProjectProperty : getProperties()) {
			if(mavenProjectProperty.getName().equals(versionVariable)) {
				return mavenProjectProperty.getValue();
			}
		}
		
		return version;
	}

	public void replaceDependencyLineFeedCarriageReturn() {
		super.replaceLineFeedCarriageReturnAndTrim();
		getDependencies().forEach((dependency) -> dependency.replaceLineFeedCarriageReturnAndTrim());
	}

}
