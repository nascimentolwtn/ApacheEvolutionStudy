package br.inpe.cap.evolution.maven;

import org.apache.commons.lang3.StringUtils;

public class CommitLine {

	public static final String HEADER = "HASH,DATE,REPOSITORY,FILE,COMMIT_POSISTION,TOTAL_COMMITS,%_PROJECT,GROUP_ID,ARTIFACT_ID,VERSION,PREVIOUS_VERSION,VERSION_CHANGED,MESSAGE";
	public static final String INITIAL_VERSION = "initial_version";

	private String hash;
	private String date;
	private String repository;
	private String file;
	private int commitPosition;
	private int totalCommits;
	private float percent;
	private boolean dependencyManaged;
	private String groupId;
	private String artifactId;
	private String version;
	private String previousVersion = INITIAL_VERSION;
	private boolean versionChanged;
	private String message;
	
	public static enum CommitLineType {INPUT, OUTPUT};
	
	/**
	 *"Use Static Factory Method parseCommitLine(String line)!
	 */
	private CommitLine() {}

	public static CommitLine parseCommitLine(String line, CommitLineType type) {
		String[] tokens = validateAndTokenizeLine(line);
		CommitLine commitLine = new CommitLine();
		int tokenIndex = 0;
		commitLine.setHash(tokens[tokenIndex++]);
		commitLine.setDate(tokens[tokenIndex++]);
		commitLine.setRepository(tokens[tokenIndex++]);
		commitLine.setFile(tokens[tokenIndex++]);
		commitLine.setCommitPosition(Integer.parseInt(tokens[tokenIndex++]));
		commitLine.setTotalCommits(Integer.parseInt(tokens[tokenIndex++]));
		commitLine.setPercent(Float.parseFloat(tokens[tokenIndex++]));
		if(type == CommitLineType.INPUT) commitLine.setDependencyManaged(Boolean.parseBoolean(tokens[tokenIndex++]));
		commitLine.setGroupId(tokens[tokenIndex++]);
		commitLine.setArtifactId(tokens[tokenIndex++]);
		commitLine.setVersion(tokens[tokenIndex++]);
		if(type == CommitLineType.OUTPUT) commitLine.setPreviousVersion(tokens[tokenIndex++]);
		if(type == CommitLineType.OUTPUT) commitLine.setVersionChanged(Boolean.parseBoolean(tokens[tokenIndex++]));
		commitLine.setMessage(tokens[tokenIndex++]);
		return commitLine;
	}

	private static String[] validateAndTokenizeLine(String line) {
		if(HEADER.equals(line)) {
			throw new RuntimeException("Should not parse the CSV header.");
		}
		if(StringUtils.isEmpty(line)) {
			throw new RuntimeException("CSV line is empty or null.");
		}
		String[] tokens = line.split(",");
		boolean isInputTokens = tokens.length == 12;
		boolean isOutputTokens = tokens.length == 13;
		if(!(isInputTokens || isOutputTokens)) {
			throw new RuntimeException("Line cannot be parsed.");
		}
		return tokens;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public int getCommitPosition() {
		return commitPosition;
	}

	public void setCommitPosition(int commitPosition) {
		this.commitPosition = commitPosition;
	}

	public int getTotalCommits() {
		return totalCommits;
	}

	public void setTotalCommits(int totalCommits) {
		this.totalCommits = totalCommits;
	}

	public float getPercent() {
		return percent;
	}

	public void setPercent(float porcent) {
		this.percent = porcent;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getPreviousVersion() {
		return previousVersion;
	}

	public void setPreviousVersion(String previousVersion) {
		this.previousVersion = previousVersion;
	}

	public boolean versionHasChanged() {
		return versionChanged;
	}

	public void setVersionChanged(boolean versionChanged) {
		this.versionChanged = versionChanged;
	}

	public boolean isDependencyManaged() {
		return dependencyManaged;
	}

	public void setDependencyManaged(boolean dependencyManaged) {
		this.dependencyManaged = dependencyManaged;
	}

}
