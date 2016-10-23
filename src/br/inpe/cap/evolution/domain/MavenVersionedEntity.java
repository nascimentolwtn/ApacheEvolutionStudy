package br.inpe.cap.evolution.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import br.inpe.cap.evolution.parser.XmlMavenParser;

public class MavenVersionedEntity {

	@XStreamAlias("groupId")
	protected String groupId;
	
	@XStreamAlias("artifactId")
	protected String artifactId;
	
	@XStreamAlias("version")
	protected String version;

	public String getGroupId() {
		return this.groupId;
	}

	public String getArtifactId() {
		return this.artifactId;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((artifactId == null) ? 0 : artifactId.hashCode());
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MavenVersionedEntity other = (MavenVersionedEntity) obj;
		if (artifactId == null) {
			if (other.artifactId != null)
				return false;
		} else if (!artifactId.equals(other.artifactId))
			return false;
		if (groupId == null) {
			if (other.groupId != null)
				return false;
		} else if (!groupId.equals(other.groupId))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

	public void replaceLineFeedCarriageReturnAndTrim() {
		if(this.groupId != null)
			this.groupId = XmlMavenParser.replaceLineFeedAndComma(this.groupId).trim();
		if(this.artifactId != null)
			this.artifactId = XmlMavenParser.replaceLineFeedAndComma(this.artifactId).trim();
		if(this.version != null)
			this.version = XmlMavenParser.replaceLineFeedAndComma(this.version).trim();
	}

}