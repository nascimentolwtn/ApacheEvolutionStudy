package br.inpe.cap.evolution.maven;

import static br.inpe.cap.evolution.maven.CommitLine.CommitLineType.DATE_VERSION;
import static br.inpe.cap.evolution.maven.CommitLine.CommitLineType.INPUT;
import static br.inpe.cap.evolution.maven.CommitLine.CommitLineType.OUTPUT;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class CommitLine {

	public static final String INPUT_HEADER = "HASH,DATE,REPOSITORY,FILE,COMMIT_POSISTION,TOTAL_COMMITS,%_PROJECT,IS_MANAGED,GROUP_ID,ARTIFACT_ID,VERSION,MESSAGE";
	public static final String OUTPUT_HEADER = "HASH,DATE,REPOSITORY,FILE,COMMIT_POSITION,TOTAL_COMMITS,%_PROJECT,GROUP_ID,ARTIFACT_ID,VERSION,PREVIOUS_VERSION,VERSION_CHANGED,VERSION_EVER_CHANGED,MESSAGE";
	public static final String OUTPUT_DATESEARCH_HEADER = "HASH,DATE,REPOSITORY,FILE,COMMIT_POSITION,TOTAL_COMMITS,%_PROJECT,GROUP_ID,ARTIFACT_ID,VERSION,PREVIOUS_VERSION,VERSION_CHANGED,VERSION_EVER_CHANGED,VERSION_RELEASE_DATE,NEWER_VERSION_ON_COMMIT_DATE,USING_NEWEST_VERSION,MESSAGE";
	public static final String INITIAL_VERSION = "initial_version";
	static final int ARTIFACT_ID_OUTPUT_INDEX = 8;

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
	private boolean versionEverChanged;
	private String versionReleaseDate;
	private String newerVersionOnCommitDate;
	private boolean usingNewestVersion;
	private String message;
	
	public static enum CommitLineType {INPUT, OUTPUT, DATE_VERSION};
	
	/**
	 *"Use Static Factory Method parseCommitLine(String line)!
	 */
	private CommitLine() {}

	public static CommitLine parseCommitLine(final String line, final CommitLineType type) throws NonParseableCommitLineException {
		final String[] tokens = validateAndTokenizeLine(line);
		final CommitLine commitLine = new CommitLine();
		int tokenIndex = 0;
		commitLine.setHash(tokens[tokenIndex++]);
		commitLine.setDate(tokens[tokenIndex++]);
		commitLine.setRepository(tokens[tokenIndex++]);
		commitLine.setFile(tokens[tokenIndex++]);
		commitLine.setCommitPosition(Integer.parseInt(tokens[tokenIndex++]));
		commitLine.setTotalCommits(Integer.parseInt(tokens[tokenIndex++]));
		commitLine.setPercent(Float.parseFloat(tokens[tokenIndex++]));
		if(type == INPUT) commitLine.setDependencyManaged(Boolean.parseBoolean(tokens[tokenIndex++]));
		commitLine.setGroupId(tokens[tokenIndex++]);
		commitLine.setArtifactId(tokens[tokenIndex++]);
		commitLine.setVersion(tokens[tokenIndex++]);
		if(type == OUTPUT || type == DATE_VERSION) commitLine.setPreviousVersion(tokens[tokenIndex++]);
		if(type == OUTPUT || type == DATE_VERSION) commitLine.setVersionChanged(Boolean.parseBoolean(tokens[tokenIndex++]));
		if(type == OUTPUT || type == DATE_VERSION) commitLine.setVersionEverChanged(Boolean.parseBoolean(tokens[tokenIndex++]));
		if(type == DATE_VERSION) commitLine.setVersionReleaseDate(tokens[tokenIndex++]);
		if(type == DATE_VERSION) commitLine.setNewerVersionOnCommitDate(tokens[tokenIndex++]);
		if(type == DATE_VERSION) commitLine.setUsingNewestVersion(Boolean.parseBoolean(tokens[tokenIndex++]));
		commitLine.setMessage(tokens[tokenIndex++].trim());
		return commitLine;
	}

	private static String[] validateAndTokenizeLine(String line) throws NonParseableCommitLineException {
		if(INPUT_HEADER.equals(line) || OUTPUT_HEADER.equals(line) || OUTPUT_DATESEARCH_HEADER.equals(line)) {
			throw new NonParseableCommitLineException("Should not parse the CSV header.");
		}
		if(StringUtils.isEmpty(line)) {
			throw new NonParseableCommitLineException("CSV line is empty or null.");
		}
		line = validateCommitMessage(line);
		final String[] tokens = line.split(",");
		final boolean isInputTokens = tokens.length == 12;
		final boolean isOutputTokens = tokens.length == 14;
		final boolean isOutputDateSearchTokens = tokens.length == 17;
		if(!(isInputTokens || isOutputTokens || isOutputDateSearchTokens)) {
			throw new NonParseableCommitLineException("Line cannot be parsed:\n"+line);
		}
		return tokens;
	}

	private static String validateCommitMessage(String line) {
		final boolean commitLineHasntMessage = line.endsWith(",");
		if(commitLineHasntMessage) line = line + " "; // add space that will be trimmed later
		return line;
	}

	public static void removeHeader(final List<String> listCsvLines) {
		if(!listCsvLines.isEmpty()) {
			listCsvLines.remove(listCsvLines.get(0));
		}
	}

	public static String lastLine(final List<String> linesCsvInput) {
		if(!linesCsvInput.isEmpty()) {
			return linesCsvInput.get(linesCsvInput.size()-1);
		} else {
			return "";
		}
	}

	public String getHash() {
		return hash;
	}

	public void setHash(final String hash) {
		this.hash = hash;
	}

	public String getDate() {
		return date;
	}

	public void setDate(final String date) {
		this.date = date;
	}

	public String getRepository() {
		return repository;
	}

	public void setRepository(final String repository) {
		this.repository = repository;
	}

	public String getFile() {
		return file;
	}

	public void setFile(final String file) {
		this.file = file;
	}

	public int getCommitPosition() {
		return commitPosition;
	}

	public void setCommitPosition(final int commitPosition) {
		this.commitPosition = commitPosition;
	}

	public int getTotalCommits() {
		return totalCommits;
	}

	public void setTotalCommits(final int totalCommits) {
		this.totalCommits = totalCommits;
	}

	public float getPercent() {
		return percent;
	}

	public void setPercent(final float porcent) {
		this.percent = porcent;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(final String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(final String artifactId) {
		this.artifactId = artifactId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(final String version) {
		this.version = version;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	public String getPreviousVersion() {
		return previousVersion;
	}

	public void setPreviousVersion(final String previousVersion) {
		this.previousVersion = previousVersion;
	}

	public boolean versionHasChanged() {
		return versionChanged;
	}

	public void setVersionChanged(final boolean versionChanged) {
		this.versionChanged = versionChanged;
	}

	public boolean isDependencyManaged() {
		return dependencyManaged;
	}

	public void setDependencyManaged(final boolean dependencyManaged) {
		this.dependencyManaged = dependencyManaged;
	}

	public boolean versionHasEverChanged() {
		return versionEverChanged;
	}

	public void setVersionEverChanged(final boolean versionEverChanged) {
		this.versionEverChanged = versionEverChanged;
	}
	
	public String getVersionReleaseDate() {
		return versionReleaseDate;
	}
	
	public void setVersionReleaseDate(String versionReleaseDate) {
		this.versionReleaseDate = versionReleaseDate;
	}
	
	public String getNewerVersionOnCommitDate() {
		return newerVersionOnCommitDate;
	}
	
	public void setNewerVersionOnCommitDate(String newerVersionOnCommitDate) {
		this.newerVersionOnCommitDate = newerVersionOnCommitDate;
	}
	
	public boolean isUsingNewestVersion() {
		return usingNewestVersion;
	}

	public void setUsingNewestVersion(boolean usingNewestVersion) {
		this.usingNewestVersion = usingNewestVersion;
	}

	public void recalculatePosition(final int lastTotalCommits) {
		this.totalCommits = lastTotalCommits;
		this.percent = roundFiveDigits(this.commitPosition * 100 / (float) this.totalCommits);
	}

	public static float roundFiveDigits(final float fullPercent) {
		return Math.round(fullPercent * 100000) / 100000f;
	}

	public static String parseArtifactId(final String csvLine) {
		final String[] tokens = validateCommitMessage(csvLine).split(",");
		return tokens[ARTIFACT_ID_OUTPUT_INDEX];
	}

}
