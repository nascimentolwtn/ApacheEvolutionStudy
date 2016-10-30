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
	
	/**
	 *"Use Static Factory Method parseCommitLine(String line)!
	 */
	private CommitLine() {}

	public static CommitLine parseInputCommitLine(String line) {
		String[] tokens = validateAndTokenizeLine(line);
		CommitLine commitLine = new CommitLine();
		commitLine.setHash(tokens[0]);
		commitLine.setDate(tokens[1]);
		commitLine.setRepository(tokens[2]);
		commitLine.setFile(tokens[3]);
		commitLine.setCommitPosition(Integer.parseInt(tokens[4]));
		commitLine.setTotalCommits(Integer.parseInt(tokens[5]));
		commitLine.setPercent(Float.parseFloat(tokens[6]));
		commitLine.setDependencyManaged(Boolean.parseBoolean(tokens[7]));
		commitLine.setGroupId(tokens[8]);
		commitLine.setArtifactId(tokens[9]);
		commitLine.setVersion(tokens[10]);
		commitLine.setMessage(tokens[11]);
		return commitLine;
	}

	public static CommitLine parseOutputCommitLine(String line) {
		String[] tokens = validateAndTokenizeLine(line);
		CommitLine commitLine = new CommitLine();
		commitLine.setHash(tokens[0]);
		commitLine.setDate(tokens[1]);
		commitLine.setRepository(tokens[2]);
		commitLine.setFile(tokens[3]);
		commitLine.setCommitPosition(Integer.parseInt(tokens[4]));
		commitLine.setTotalCommits(Integer.parseInt(tokens[5]));
		commitLine.setPercent(Float.parseFloat(tokens[6]));
		commitLine.setGroupId(tokens[7]);
		commitLine.setArtifactId(tokens[8]);
		commitLine.setVersion(tokens[9]);
		commitLine.setPreviousVersion(tokens[10]);
		commitLine.setVersionChanged(Boolean.parseBoolean(tokens[11]));
		commitLine.setMessage(tokens[12]);
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
			throw new RuntimeException("Line cannot be parsed. Tokens="+tokens.length);
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
