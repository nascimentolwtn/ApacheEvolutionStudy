package br.inpe.cap.evolution.processor;

import static br.inpe.cap.evolution.maven.CommitLine.CommitLineType.INPUT;
import static br.inpe.cap.evolution.maven.CommitLine.CommitLineType.OUTPUT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.repodriller.persistence.PersistenceMechanism;

import br.inpe.cap.evolution.domain.MavenDependency;
import br.inpe.cap.evolution.domain.MavenProject;
import br.inpe.cap.evolution.maven.CommitLine;
import br.inpe.cap.evolution.maven.CommitLine.CommitLineType;
import br.inpe.cap.evolution.maven.NonParseableCommitLineException;

public class VersionEvolutionDetectorPostProcessor {
	
	private CommitLine previousCommit;
	private MavenProject currentProject;
	private final Map<String, MavenProject> currentMavenProjects = new HashMap<>();
	private int lastTotalCommits;

	public void processCsvLines(final PersistenceMechanism writer, final Stream<String> listCsvLines) {
		processCsvLines(writer, listCsvLines, INPUT);
	}

	public void reprocessVersionDetectorOutputCsvLines(final PersistenceMechanism writer, final List<String> listCsvLines) {
		final CommitLine lastCommit = CommitLine.parseCommitLine(listCsvLines.get(listCsvLines.size()-1), OUTPUT);
		lastTotalCommits = lastCommit.getTotalCommits();
		processCsvLines(writer, listCsvLines.stream(), OUTPUT);
	}

	public void processCsvLines(final PersistenceMechanism writer, final Stream<String> listCsvLines, final CommitLineType commitLineType) {
		writeCsvHeader(writer);
		listCsvLines.forEach((line) -> processLine(writer, line, commitLineType));
	}

	public void processLine(final PersistenceMechanism writer, final String line) {
		processLine(writer, line, INPUT);
	}
	
	public void processLine(final PersistenceMechanism writer, final String line, final CommitLineType commitLineType) {
		try {
			final CommitLine currentCommit = CommitLine.parseCommitLine(line, commitLineType);
			keepCurrentCommitFilePathAsOriginalPath(currentCommit);
			final MavenProject currentProject = startProject(currentCommit);

			final MavenDependency mavenDependencyByArtifactId = findPreviousVersionAndSetVersionChanged(currentCommit);
			if(mavenDependencyByArtifactId == null) {
				currentProject.getDependencies().add(getMavenDependencyFromCSVLine(currentCommit));
			} else {
				mavenDependencyByArtifactId.setVersion(currentCommit.getVersion());
			}
			
			if(commitLineType == OUTPUT) {
				currentCommit.recalculatePosition(lastTotalCommits);
			}
			
			writeCsvLine(writer, currentCommit);
		} catch (NonParseableCommitLineException e) {
			// Silenced in order to remove if(removeHeader) check
		}
	}

	private void keepCurrentCommitFilePathAsOriginalPath(final CommitLine currentCommit) {
		currentCommit.setFile(currentCommit.getFile().replace("C:\\", "E:\\"));
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

	private MavenProject startProject(final CommitLine currentCommit) {
		if(previousCommit == null || !currentCommit.getHash().equals(this.previousCommit.getHash())) {
			startNewCommit(currentCommit);
		}
		return projectRegardCurrentCommit(currentCommit);
	}

	private void startNewCommit(final CommitLine currentCommit) {
		this.previousCommit = currentCommit;
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

	public void writeCsvHeader(final PersistenceMechanism writer) {
		final String[] header = CommitLine.OUTPUT_HEADER.split(",");
		writer.write(header[0], header[1], header[2], header[3],
					 header[4], header[5], header[6], header[7],
					 header[8], header[9], header[10],header[11],
					 header[12],header[13]);
	}
	
	private void writeCsvLine(final PersistenceMechanism writer, final CommitLine commitLine) {
		writer.write(
			commitLine.getHash(),
			commitLine.getDate(),
			commitLine.getRepository(),
			commitLine.getFile(),
			commitLine.getCommitPosition(),
			commitLine.getTotalCommits(),
			commitLine.getPercent(),
			commitLine.getGroupId(),
			commitLine.getArtifactId(),
			commitLine.getVersion(),
			commitLine.getPreviousVersion(),
			commitLine.versionHasChanged(),
			commitLine.versionHasEverChanged(),
			commitLine.getMessage()
		);
		
	}

	protected MavenDependency getMavenDependencyFromCSVLine(final CommitLine parsedCommitLine) {
		final MavenDependency dependency = new MavenDependency();
		dependency.setGroupId(parsedCommitLine.getGroupId());
		dependency.setArtifactId(parsedCommitLine.getArtifactId());
		dependency.setVersion(parsedCommitLine.getVersion());
		return dependency ;
	}
	
}