package br.inpe.cap.evolution.processor;

import static br.inpe.cap.evolution.maven.CommitLine.lastLine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.repodriller.persistence.PersistenceMechanism;

import br.inpe.cap.evolution.domain.MavenDependency;
import br.inpe.cap.evolution.domain.MavenProject;
import br.inpe.cap.evolution.maven.CommitLine;
import br.inpe.cap.evolution.maven.CommitLine.CommitLineType;

public class ArtifactAggregatorPostProcessor {

	public static final String HEADER = "PROJECT,SUBPROJECT,TOTAL_COMMITS,QTD_GROUPS,GROUPS,QTD_ARTIFACTS,ARTIFACTS";
	public static final String AGGREGATE_SEPARATOR = ";";
	
	private MavenProject currentProject;
	private final Map<String, MavenProject> currentMavenProjects = new HashMap<>();
	private CommitLine lastCommitLine;
	
	public CommitLine parseLastCommitLine(List<String> linesCsvInput) {
		return CommitLine.parseCommitLine(lastLine(linesCsvInput), CommitLineType.OUTPUT);
	}
	
	public void process(PersistenceMechanism writer, List<String> linesCsvInput, String hash) {
		writeHeader(writer);
		linesCsvInput.stream()
			.filter((line)->line.startsWith(hash))
			.forEach((line) -> processLine(writer, line));
		for (MavenProject mavenProject : this.currentMavenProjects.values()) {
			String groupIds = aggregateGroupIds(mavenProject.getDependencies());
			String artifactIds = aggregateArtifactIds(mavenProject.getDependencies());
			writeCsvLine(writer, mavenProject.getPath(), groupIds, artifactIds);
		}
	}

	private void writeHeader(PersistenceMechanism writer) {
		String[] header = HEADER.split(",");
		writer.write(header[0],header[1],header[2],header[3],header[4],header[5],header[6]);
	}

	private void processLine(PersistenceMechanism writer, String line) {
		final CommitLine currentCommit = CommitLine.parseCommitLine(line, CommitLineType.OUTPUT);
		final MavenProject currentProject = projectRegardCurrentCommit(currentCommit);
		
		final MavenDependency mavenDependencyByArtifactId = findPreviousVersionAndSetVersionChanged(currentCommit);
		if(mavenDependencyByArtifactId == null) {
			currentProject.getDependencies().add(getMavenDependencyFromCSVLine(currentCommit));
		} else {
			mavenDependencyByArtifactId.setVersion(currentCommit.getVersion());
		}
		
		this.lastCommitLine = currentCommit;
	}

	private MavenProject projectRegardCurrentCommit(final CommitLine currentCommit) {
		if(currentProject == null || this.currentMavenProjects.get(currentCommit.getFile()) == null) {
			this.currentProject = new MavenProject();
			this.currentProject.setPath(currentCommit.getFile());
			this.currentMavenProjects.put(currentCommit.getFile(), this.currentProject);
		} else {
			this.currentProject = this.currentMavenProjects.get(currentCommit.getFile());
		}
		return this.currentProject;
	}

	private MavenDependency findPreviousVersionAndSetVersionChanged(final CommitLine currentCommit) {
		final MavenProject mavenProject = this.currentMavenProjects.get(currentCommit.getFile());
		final MavenDependency dependency = mavenProject.getMavenDependencyByArtifactId(currentCommit.getArtifactId());
		if(dependency != null) {
			if(!currentCommit.getVersion().equals(dependency.getVersion())) {
				currentCommit.setVersionChanged(true);
				dependency.setVersionEverChanged(true);
			}
			currentCommit.setPreviousVersion(dependency.getVersion());
			currentCommit.setVersionEverChanged(dependency.versionHasEverChanged());
		}
		return dependency;
	}

	protected MavenDependency getMavenDependencyFromCSVLine(final CommitLine parsedCommitLine) {
		final MavenDependency dependency = new MavenDependency();
		dependency.setGroupId(parsedCommitLine.getGroupId());
		dependency.setArtifactId(parsedCommitLine.getArtifactId());
		dependency.setVersion(parsedCommitLine.getVersion());
		return dependency;
	}

	private String aggregateGroupIds(List<MavenDependency> dependencies) {
		Set<String> groups = new HashSet<>();
		dependencies.forEach((d) -> groups.add(d.getGroupId()));
		return transformSetToString(groups);
	}

	private String aggregateArtifactIds(List<MavenDependency> dependencies) {
		Set<String> artifacts = new HashSet<>();
		dependencies.forEach((d) -> artifacts.add(d.getArtifactId()));
		return transformSetToString(artifacts);
	}

	private String transformSetToString(Set<String> aggregatedSet) {
		StringBuilder sb = new StringBuilder();
		sb.append(aggregatedSet.size());
		sb.append(",");
		aggregatedSet.forEach((string) -> sb.append(string + AGGREGATE_SEPARATOR));
		return removeLastAggregatedSeparator(sb.toString());
	}

	private String removeLastAggregatedSeparator(String aggregatedSet) {
		if(!StringUtils.isEmpty(aggregatedSet)) {
			return aggregatedSet.substring(0, aggregatedSet.length()-1);
		} else {
			return aggregatedSet;
		}
	}

	private void writeCsvLine(final PersistenceMechanism writer, String path, String groupIds, String artifactIds) {
		final String[] groupIdSplit = groupIds.split(",");
		final String[] artifactIdSplit = artifactIds.split(",");
		writer.write(
			this.lastCommitLine.getRepository(),
			path.replace("E:\\metricminer-evolution-stars\\", ""),
			this.lastCommitLine.getTotalCommits(),
			groupIdSplit[0],
			groupIdSplit[1],
			artifactIdSplit[0],
			artifactIdSplit[1]
		);
	}

}
