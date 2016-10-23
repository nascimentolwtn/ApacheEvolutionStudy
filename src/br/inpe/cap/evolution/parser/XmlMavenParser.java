package br.inpe.cap.evolution.parser;

import com.thoughtworks.xstream.XStream;

import br.inpe.cap.evolution.domain.MavenProject;

public class XmlMavenParser {

	public static final String REGEX_LINEFEED_AND_COMMA = "[\\r\\n,]+";
	private XStream xstream;

	public XmlMavenParser() {
		xstream = new XStream();
		xstream.ignoreUnknownElements();
		xstream.processAnnotations(
				new Class[] {
					MavenProject.class, 
				});
	}

	/**
	 * Factory Method
	 */
	public MavenProject readPOM(String pom) {
		MavenProject mavenProjectfromXML = (MavenProject) xstream.fromXML(pom);
		mavenProjectfromXML.replaceDependencyLineFeedCarriageReturn();
		mavenProjectfromXML.replaceVariables();
		mavenProjectfromXML.setupDependencyManagedDependencies();
		return mavenProjectfromXML;
	}

	public static String replaceLineFeedAndComma(String commitMessage) {
		return commitMessage.replaceAll(REGEX_LINEFEED_AND_COMMA, "");
	}

}
