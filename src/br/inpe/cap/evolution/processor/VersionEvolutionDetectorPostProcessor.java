package br.inpe.cap.evolution.processor;

import java.io.File;
import java.io.IOException;
import java.util.List;

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

	public void processCsvLines(CSVFile csv, List<String> listCsvLines) {
		writeCsvHeader(csv);
		removeHeader(listCsvLines);
		listCsvLines.forEach((line) -> processLine(line, csv));
	}

	private void processLine(String line, PersistenceMechanism writer) {

		CommitLine currentCommit = CommitLine.parseInputCommitLine(line);
		currentCommit.setPreviousVersion(CommitLine.INITIAL_VERSION);
		MavenProject pom = new MavenProject();
		pom.getDependencies().add(getMavenDependencyFromCSVLine(currentCommit));
		
		pom.getDependencies().forEach(
			(dependency) -> {
				CommitLine outputLine = currentCommit;
				outputLine.setMavenDependencyValues(dependency);
				writeCsvLine(writer, outputLine);
			});
		
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

	private void removeHeader(List<String> listCsvLines) {
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