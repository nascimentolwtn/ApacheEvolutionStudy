package br.inpe.cap.apache.parser;

import com.thoughtworks.xstream.XStream;

import br.inpe.cap.apache.domain.MavenDependency;
import br.inpe.cap.apache.domain.MavenProject;
import br.inpe.cap.apache.domain.MavenProjectProperty;

public class XmlMavenParser {

	private XStream xstream;

	public XmlMavenParser() {
		xstream = new XStream();
		xstream.ignoreUnknownElements();
		xstream.processAnnotations(
				new Class[] {
					MavenProject.class, 
					MavenDependency.class, 
					MavenProjectProperty.class
				});
	}

	public MavenProject readPOM(String pom) {
		MavenProject mavenProjectfromXML = (MavenProject) xstream.fromXML(pom);
		mavenProjectfromXML.replaceDependencyVariableVersions();
		return mavenProjectfromXML;
	}

}
