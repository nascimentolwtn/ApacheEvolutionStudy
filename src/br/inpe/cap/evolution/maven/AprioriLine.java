package br.inpe.cap.evolution.maven;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import br.inpe.cap.evolution.domain.MavenProject;

public class AprioriLine {

	public static final String FIRST_CELL = "SUBPROJECT";
	public static final String CONTAINS_ARTIFACT = "T";
	public static final String NOT_CONTAINS_ARTIFACT = "?";
	
	private static final String GIT_REPOSITORY_PATH = "E:\\metricminer-evolution-stars\\";

	private String subProject;
	private String[] artifacts;
	
	/**
	 *"Use Static Factory Method parseCommitLine(CommitLine line)!
	 */
	private AprioriLine() {}

	public static void removeHeader(final List<String> listCsvLines) {
		if(!listCsvLines.isEmpty()) {
			listCsvLines.remove(listCsvLines.get(0));
		}
	}

	public static String createHeader(Set<String> allSubProjectArtifacts) {
		StringBuilder sb = new StringBuilder();
		sb.append(FIRST_CELL);
		allSubProjectArtifacts.forEach((artifact) -> {
			sb.append(",");
			sb.append(artifact);
		});
		return sb.toString();
	}

	public static AprioriLine parseMavenProject(MavenProject mavenProject, Set<String> allSubProjectArtifacts) {
		String[] aprioriArtifacts = new String[allSubProjectArtifacts.size()];
		int artifactIndex = 0;

		List<String> mavenProjectDependenciesArtifacts = mavenProject.getAllDependencies().stream()
				.map((d)->d.getArtifactId()).collect(Collectors.toList());
		
		for (String artifact : allSubProjectArtifacts) {
			if(mavenProjectDependenciesArtifacts.contains(artifact)) {
				aprioriArtifacts[artifactIndex++] = CONTAINS_ARTIFACT;
			} else {
				aprioriArtifacts[artifactIndex++] = NOT_CONTAINS_ARTIFACT;
			}
		}
		
		AprioriLine aprioriLine = new AprioriLine();
		aprioriLine.subProject = mavenProject.getPath();
		aprioriLine.artifacts = aprioriArtifacts;
		return aprioriLine;
	}

	public Object[] toCSVLine() {
		Object[] csvLine = new Object[this.artifacts.length + 1];
		csvLine[0] = this.subProject.replace(GIT_REPOSITORY_PATH, "");
		for (int i = 0; i < artifacts.length; i++) {
			csvLine[i+1] = artifacts[i];
		}
		return csvLine ;
	}

}
