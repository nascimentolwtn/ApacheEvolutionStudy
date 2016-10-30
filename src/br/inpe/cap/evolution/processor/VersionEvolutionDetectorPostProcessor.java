package br.inpe.cap.evolution.processor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import br.com.metricminer2.persistence.PersistenceMechanism;
import br.com.metricminer2.persistence.csv.CSVFile;
import br.inpe.cap.evolution.domain.MavenDependency;
import br.inpe.cap.evolution.domain.MavenProject;
import br.inpe.cap.evolution.maven.CommitLine;

public class VersionEvolutionDetectorPostProcessor {
	
	public static void main(String[] args) throws IOException {
		System.out.println("Starting...");

		VersionEvolutionDetectorPostProcessor postProcessor = new VersionEvolutionDetectorPostProcessor();
		
		File csvInput = new File("study" + File.separator + "all_dependency" + File.separator + "evolutions" + File.separator + "all-dependency-'disconf'.csv");
		CSVFile csvOutput = new CSVFile("study" + File.separator + "version-detector-'disconf'.csv");
		
		List<String> listCsvLines = FileUtils.readLines(csvInput);

		postProcessor.processCsvLines(csvOutput, listCsvLines);
		
		System.out.println("Finish!");
	}

	private CommitLine previousCommit;
	private MavenProject currentProject;
	private Map<String, MavenProject> currentMavenProjects = new HashMap<>();

	public void processCsvLines(CSVFile csv, List<String> listCsvLines) {
		processCsvLines(csv, listCsvLines, true);
	}

	public void processCsvLines(CSVFile csv, List<String> listCsvLines, boolean removeHeader) {
		writeCsvHeader(csv);
		if(removeHeader) removeHeader(listCsvLines);
		listCsvLines.forEach((line) -> processLine(line, csv));
	}

	private void processLine(String line, PersistenceMechanism writer) {
		CommitLine currentCommit = CommitLine.parseInputCommitLine(line);
		MavenProject currentProject = startProject(currentCommit);

		MavenDependency dependency = getMavenDependencyFromCSVLine(currentCommit);
		currentCommit.setMavenDependencyValues(dependency);
		findPreviousVersionsAndVersionChanges(currentCommit);
		
		MavenDependency mavenDependencyByArtifactId = currentProject.getMavenDependencyByArtifactId(dependency.getArtifactId());
		if(mavenDependencyByArtifactId == null) {
			currentProject.getDependencies().add(dependency);
		} else {
			mavenDependencyByArtifactId.setVersion(currentCommit.getVersion());
		}
		
		writeCsvLine(writer, currentCommit);
	}

	private void findPreviousVersionsAndVersionChanges(CommitLine currentCommit) {
		MavenProject mavenProject = this.currentMavenProjects.get(currentCommit.getFile());
		MavenDependency dependency = mavenProject.getMavenDependencyByArtifactId(currentCommit.getArtifactId());
		if(dependency != null) {
			currentCommit.setPreviousVersion(dependency.getVersion());
			if(!currentCommit.getVersion().equals(dependency.getVersion())) {
				currentCommit.setVersionChanged(true);
			}
		}
	}

	private MavenProject startProject(CommitLine currentCommit) {
		if(previousCommit == null || !currentCommit.getHash().equals(this.previousCommit.getHash())) {
			startNewCommit(currentCommit);
		}
		return projectRegardCurrentCommit(currentCommit);
	}

	private void startNewCommit(CommitLine currentCommit) {
		this.previousCommit = currentCommit;
	}

	private MavenProject projectRegardCurrentCommit(CommitLine currentCommit) {
		if(currentProject == null || this.currentMavenProjects.get(currentCommit.getFile()) == null) {
			this.currentProject = new MavenProject();
			this.currentProject.setPath(currentCommit.getFile());
			this.currentMavenProjects.put(currentCommit.getFile(), this.currentProject);
		} else {
			this.currentProject = this.currentMavenProjects.get(currentCommit.getFile());
		}
		return this.currentProject;
	}

	private void writeCsvHeader(PersistenceMechanism writer) {
		writer.write(CommitLine.HEADER);
	}
	
	private void writeCsvLine(PersistenceMechanism writer, CommitLine commitLine) {
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
			commitLine.getMessage()
		);
		
	}

	public static void removeHeader(List<String> listCsvLines) {
		listCsvLines.remove(listCsvLines.get(0));
	}

	protected MavenDependency getMavenDependencyFromCSVLine(CommitLine parsedCommitLine) {
		MavenDependency dependency = new MavenDependency();
		dependency.setGroupId(parsedCommitLine.getGroupId());
		dependency.setArtifactId(parsedCommitLine.getArtifactId());
		dependency.setVersion(parsedCommitLine.getVersion());
		return dependency ;
	}
	
}