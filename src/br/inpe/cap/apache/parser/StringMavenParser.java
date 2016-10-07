package br.inpe.cap.apache.parser;

import org.apache.commons.lang3.NotImplementedException;

import br.inpe.cap.apache.domain.MavenProject;

public class StringMavenParser implements MavenParser {

	@Override
	public String extractApacheLibVersion(String sourceCode) {
		int firstIndexOfApacheGroupId = sourceCode.indexOf("<groupId>org.apache");
		int indexOfGroupIdEndTag = sourceCode.indexOf("</groupId>", firstIndexOfApacheGroupId);
	
		int firstIndexOfVersionAfterApacheGroupId = sourceCode.indexOf("<version>", indexOfGroupIdEndTag);
		if(firstIndexOfVersionAfterApacheGroupId == -1) {
			return "no version";
		}
		int indexOfVersionEndTag = sourceCode.indexOf("</version>", firstIndexOfVersionAfterApacheGroupId);
		int lengthVersion = 9; // "<version>".length();
		String apacheLibVersion = sourceCode.substring(firstIndexOfVersionAfterApacheGroupId+lengthVersion, indexOfVersionEndTag);
		return apacheLibVersion;
	}

	@Override
	public String extractApacheLib(String sourceCode) {
		int firstIndexOfApacheGroupId = sourceCode.indexOf("<groupId>org.apache");
		int indexOfEndGroupIdTag = sourceCode.indexOf("</groupId>", firstIndexOfApacheGroupId);
		int lengthGroupId = 9; // "<groupId>".length();
		String apacheLib = sourceCode.substring(firstIndexOfApacheGroupId+lengthGroupId, indexOfEndGroupIdTag);
		return apacheLib;
	}
	
	@Override
	public MavenProject readPOM(String pom) {
		throw new NotImplementedException(pom);
	}
}
