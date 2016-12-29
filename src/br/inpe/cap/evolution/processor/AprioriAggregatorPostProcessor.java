package br.inpe.cap.evolution.processor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.persistence.csv.CSVFile;

import br.inpe.cap.evolution.domain.MavenDependency;
import br.inpe.cap.evolution.domain.MavenProject;
import br.inpe.cap.evolution.maven.AprioriLine;
import br.inpe.cap.evolution.maven.CommitLine;
import br.inpe.cap.evolution.maven.CommitLine.CommitLineType;

public class AprioriAggregatorPostProcessor {

	public static final String FIRST_CELL = "SUBPROJECT";
	
	private final Set<String> allSubProjectArtifacts = new TreeSet<>();

	private MavenProject currentProject;
	private final Map<String, MavenProject> currentMavenProjects = new TreeMap<>();

	private String parseLastCommitHash(final File csvInput) throws IOException {
		final Object[] lines = Files.lines(csvInput.toPath()).toArray();
		final String lastLine = (String) lines[lines.length-1];
		return lastLine.substring(0, lastLine.indexOf(","));
	}

	private static Set<String> extractLastCommitSubProjectArtifacts(final Stream<String> linesCsvInput, final String lastCommitHash) {
		final Set<String> artifacts = new TreeSet<>();
		linesCsvInput
			.filter((csvLine) -> csvLine.contains(lastCommitHash))
			.forEach((csvLine) -> {
				artifacts.add(CommitLine.parseArtifactId(csvLine));
			});
		return artifacts;
	}

	public void process(final CSVFile writer, final List<File> csvInputs) {
		final Map<String, String> projectsLastHashes = extractAllArtifactsFromAllSubProjects(csvInputs);
		csvInputs.forEach((csv) -> {
			try {
				final String fileName = csv.getName();
				final String lastCommitHash = projectsLastHashes.get(fileName);
		
				System.out.println("Processing artifacts from " + fileName);
				Files.lines(csv.toPath())
					.filter((csvLine) -> csvLine.contains(lastCommitHash))
					.forEach((line) -> processLine(line));
			} catch (final RuntimeException | IOException e) {
				System.err.println(e.getMessage());
			}
		});
		
		writeHeader(writer, AprioriLine.createHeader(allSubProjectArtifacts));
		for (final MavenProject mavenProject : this.currentMavenProjects.values()) {
			final AprioriLine aprioriLine = AprioriLine.parseMavenProject(mavenProject, this.allSubProjectArtifacts);
			writer.write(aprioriLine.toCSVLine());
		}
		
	}

	private Map<String, String> extractAllArtifactsFromAllSubProjects(final List<File> csvInputs) {
		final Map<String, String> mapa = new HashMap<>();
		csvInputs.forEach((csv) -> {
			try {
				final String lastCommitHash = parseLastCommitHash(csv);
				mapa.put(csv.getName(), lastCommitHash);
				System.out.println("Extracting artifiacts from " + csv.getName());
				this.allSubProjectArtifacts.addAll(extractLastCommitSubProjectArtifacts(Files.lines(csv.toPath()), lastCommitHash));
			} catch (final Exception e) {
				System.err.println(e.getMessage());
			}
		});
		return mapa;
	}

	private void writeHeader(final PersistenceMechanism writer, final String aprioriHeader) {
		final Object[] headerTokens = aprioriHeader.split(",");
		writer.write(headerTokens);
	}
	
	public Set<String> getAllSubProjectArtifacts() {
		return allSubProjectArtifacts;
	}

	private void processLine(final String line) {
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
