package br.inpe.cap.apache.parser;

import br.inpe.cap.apache.domain.MavenProject;

public interface MavenParser {

	String extractApacheLibVersion(String sourceCode);

	String extractApacheLib(String sourceCode);

	MavenProject readPOM(String pom);

}