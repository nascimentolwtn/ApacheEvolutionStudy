package br.inpe.cap.evolution.processor;

import static br.inpe.cap.evolution.maven.CommitLine.CommitLineType.INPUT;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.persistence.csv.CSVFile;

import br.inpe.cap.evolution.domain.MavenDependency;
import br.inpe.cap.evolution.domain.MavenProject;
import br.inpe.cap.evolution.maven.CommitLine;

public class VersionEvolutionDetectorPostProcessor {
	
	public static void main(final String[] args) throws IOException {
		System.out.println("Starting...");
		
		List<File> arquivos = org.repodriller.util.FileUtils.getAllFilesInPath("study" + File.separator + "all_dependency" + File.separator + "evolutions");
		
		arquivos.parallelStream().forEach((csvInput)-> {
			
			final VersionEvolutionDetectorPostProcessor postProcessor = new VersionEvolutionDetectorPostProcessor();
			String name = csvInput.getName();
			System.out.println("Processing "+name);
			final CSVFile csvOutput = new CSVFile("study" + File.separator + "detector" + File.separator + name);
			
			try {
				List<String> listCsvLines = FileUtils.readLines(csvInput);
				postProcessor.processCsvLines(csvOutput, listCsvLines, false);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		});

		
		System.out.println("Finish!");
	}

	private CommitLine previousCommit;
	private MavenProject currentProject;
	private final Map<String, MavenProject> currentMavenProjects = new HashMap<>();

	public void processCsvLines(final CSVFile csv, final List<String> listCsvLines) {
		processCsvLines(csv, listCsvLines, true);
	}

	public void processCsvLines(final CSVFile csv, final List<String> listCsvLines, final boolean removeHeader) {
		writeCsvHeader(csv);
		if(removeHeader) CommitLine.removeHeader(listCsvLines);
		listCsvLines.forEach((line) -> processLine(csv, line));
	}

	public void processLine(final PersistenceMechanism writer, final String line) {
		final CommitLine currentCommit = CommitLine.parseCommitLine(line, INPUT);
		final MavenProject currentProject = startProject(currentCommit);

		final MavenDependency mavenDependencyByArtifactId = findPreviousVersionAndSetVersionChanged(currentCommit);
		if(mavenDependencyByArtifactId == null) {
			currentProject.getDependencies().add(getMavenDependencyFromCSVLine(currentCommit));
		} else {
			mavenDependencyByArtifactId.setVersion(currentCommit.getVersion());
		}
		
		writeCsvLine(writer, currentCommit);
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

	private void writeCsvHeader(final PersistenceMechanism writer) {
		writer.write(CommitLine.HEADER);
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