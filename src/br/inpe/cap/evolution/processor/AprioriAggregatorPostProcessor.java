package br.inpe.cap.evolution.processor;

import static br.inpe.cap.evolution.maven.CommitLine.lastLine;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.persistence.csv.CSVFile;

import br.inpe.cap.evolution.domain.MavenDependency;
import br.inpe.cap.evolution.domain.MavenProject;
import br.inpe.cap.evolution.maven.AprioriLine;
import br.inpe.cap.evolution.maven.CommitLine;
import br.inpe.cap.evolution.maven.CommitLine.CommitLineType;

public class AprioriAggregatorPostProcessor {

	public static final String FIRST_CELL = "SUBPROJECT";
	
	private Set<String> allSubProjectArtifacts = new TreeSet<>();

	private MavenProject currentProject;
	private final Map<String, MavenProject> currentMavenProjects = new TreeMap<>();

	private static CommitLine parseLastCommitLine(List<String> linesCsvInput) {
		return CommitLine.parseCommitLine(lastLine(linesCsvInput), CommitLineType.OUTPUT);
	}
	
	private static Set<String> extractLastCommitSubProjectArtifacts(List<String> linesCsvInput, String lastCommitHash) {
		Set<String> artifacts = new TreeSet<>();
		linesCsvInput.stream()
			.filter((csvLine) -> csvLine.contains(lastCommitHash))
			.forEach((csvLine) -> {
				final CommitLine commitLine = CommitLine.parseCommitLine(csvLine, CommitLineType.OUTPUT);
				artifacts.add(commitLine.getArtifactId());
			});
		return artifacts;
	}

	public void process(CSVFile writer, List<List<String>> linesCsvInputs) {
		extractAllArtifactsFromAllSubProjects(linesCsvInputs);
			
		for (List<String> linesCsvInput : linesCsvInputs) {
			final String lastCommitHash = parseLastCommitLine(linesCsvInput).getHash();
			CommitLine.removeHeader(linesCsvInput);

			linesCsvInput.stream()
				.filter((csvLine) -> csvLine.contains(lastCommitHash))
				.forEach((line) -> processLine(line));
		}
		
		writeHeader(writer, AprioriLine.createHeader(allSubProjectArtifacts));
		for (MavenProject mavenProject : this.currentMavenProjects.values()) {
			AprioriLine aprioriLine = AprioriLine.parseMavenProject(mavenProject, this.allSubProjectArtifacts);
			writer.write(aprioriLine.toCSVLine());
		}
		
	}

	private void extractAllArtifactsFromAllSubProjects(List<List<String>> linesCsvInputs) {
		for (List<String> linesCsvInput : linesCsvInputs) {
			final String lastCommitHash = parseLastCommitLine(linesCsvInput).getHash();
			CommitLine.removeHeader(linesCsvInput);
			
			this.allSubProjectArtifacts.addAll(extractLastCommitSubProjectArtifacts(linesCsvInput, lastCommitHash));
		}
	}

	private void writeHeader(PersistenceMechanism writer, String aprioriHeader) {
		Object[] headerTokens = aprioriHeader.split(",");
		writer.write(headerTokens);
	}
	
	public Set<String> getAllSubProjectArtifacts() {
		return allSubProjectArtifacts;
	}

	private void processLine(String line) {
		final CommitLine currentCommit = CommitLine.parseCommitLine(line, CommitLineType.OUTPUT);
		final MavenProject currentProject = projectRegardCurrentCommit(currentCommit);
		
		final MavenDependency mavenDependencyByArtifactId = getMavenDependencyCommitLine(currentCommit);
		if(mavenDependencyByArtifactId == null) {
			currentProject.getDependencies().add(getMavenDependencyFromCSVLine(currentCommit));
		}
	}
	
	protected MavenDependency getMavenDependencyFromCSVLine(final CommitLine parsedCommitLine) {
		final MavenDependency dependency = new MavenDependency();
		dependency.setGroupId(parsedCommitLine.getGroupId());
		dependency.setArtifactId(parsedCommitLine.getArtifactId());
		dependency.setVersion(parsedCommitLine.getVersion());
		return dependency;
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

	private MavenDependency getMavenDependencyCommitLine(final CommitLine currentCommit) {
		final MavenProject mavenProject = this.currentMavenProjects.get(currentCommit.getFile());
		return mavenProject.getMavenDependencyByArtifactId(currentCommit.getArtifactId());
	}

}
