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

	@XStreamAlias("modules")
	private MavenModule modulesList;

	@XStreamAlias("properties")
	@XStreamConverter(MavenProjectPropertyConverter.class)
	private List<MavenProjectProperty> properties;
	
	@XStreamAlias("dependencyManagement")
	private MavenDependencyManagement dependencyManagement;
	
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
	
	public List<String> getModules() {
		// Initialization here because class is constructed by XStream by reflection 
		if(modulesList == null) {
			modulesList = new MavenModule();
		}
		return modulesList.getModules();
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
	
	public MavenDependencyManagement getDependencyManagement() {
		// Initialization here because class is constructed by XStream by reflection 
		if(dependencyManagement == null) {
			dependencyManagement = new MavenDependencyManagement();
		}
		return dependencyManagement;
	}

	public void replaceVariables() {
		this.replaceVariables(getDependencies(), false);
	}

	private void replaceVariables(List<MavenDependency> dependencies, boolean isDependencyManaged) {
		dependencies
			.forEach(
				(dependency) -> {
					replaceGroupIdVariables(dependency);
					replaceArtifactIdVariables(dependency);
					replaceVersionVariables(dependency);
					dependency.setDependencyManaged(isDependencyManaged);
				}
			);
	}

	private void replaceGroupIdVariables(MavenDependency dependency) {
		String oldGroupId = dependency.getGroupId();
		if(isntMavenVariable(oldGroupId)) {
			return;
		}
		if(oldGroupId.startsWith(MAVEN_PROJECT_GROUPID_VARIABLE)) {
			if(this.groupId != null) {
				dependency.setGroupId(this.groupId);
			} else if(this.parent != null) {
				dependency.setGroupId(this.parent.getGroupId());
			}
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
			if(this.artifactId != null) {
				dependency.setArtifactId(this.artifactId);
			} else if(this.parent != null) {
				dependency.setArtifactId(this.parent.getArtifactId());
			}
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
			if(this.version != null) {
				dependency.setVersion(this.version);
			} else if(this.parent != null) {
				dependency.setVersion(this.parent.getVersion());
			}
		} else {
			String variableVersionValue = lookupVariableValue(oldVersion);
			dependency.setVersion(variableVersionValue);
		}
	}

	private boolean isntMavenVariable(String variable) {
		return variable == null || !variable.startsWith(MAVEN_VARIABLE_MARK);
	}

	/**
	 * @param variable Reference of dependency version variable
	 * @return Variable value. If not defined, will return original reference
	 */
	private String lookupVariableValue(String variable) {
		String variableValue = variable.substring(variable.indexOf(MAVEN_VARIABLE_MARK)+2, variable.length()-1);
		for (MavenProjectProperty mavenProjectProperty : getProperties()) {
			if(mavenProjectProperty.getName().equals(variableValue)) {
				return mavenProjectProperty.getValue();
			}
		}
		return variable;
	}

	public void replaceDependencyLineFeedCarriageReturn() {
		super.replaceLineFeedCarriageReturnAndTrim();
		getDependencies().forEach((dependency) -> dependency.replaceLineFeedCarriageReturnAndTrim());
	}

	public void setupDependencyManagedDependencies() {
		this.replaceVariables(getDependencyManagement().getDependencies(), true);
	}

	public List<MavenDependency> getAllDependencies() {
		return null;
	}

}
