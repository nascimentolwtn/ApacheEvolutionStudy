package br.inpe.cap.apache.parser;

public interface MavenParser {

	String extractApacheLibVersion(String sourceCode);

	String extractApacheLib(String sourceCode);

}