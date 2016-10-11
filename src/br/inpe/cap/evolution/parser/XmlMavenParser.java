package br.inpe.cap.evolution.parser;

import com.thoughtworks.xstream.XStream;

import br.inpe.cap.evolution.domain.MavenDependency;
import br.inpe.cap.evolution.domain.MavenProject;
import br.inpe.cap.evolution.domain.MavenProjectProperty;


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
