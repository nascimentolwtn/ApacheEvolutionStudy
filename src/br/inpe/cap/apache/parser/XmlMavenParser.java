package br.inpe.cap.apache.parser;

import com.thoughtworks.xstream.XStream;

import br.inpe.cap.apache.domain.MavenDependency;
import br.inpe.cap.apache.domain.MavenProject;

public class XmlMavenParser implements MavenParser {

	private XStream xstream;

	public XmlMavenParser() {
		xstream = new XStream();
		xstream.ignoreUnknownElements();
		xstream.processAnnotations(new Class[] {MavenProject.class, MavenDependency.class});
	}

	@Override
	public String extractApacheLib(String sourceCode) {
		return null;
	}
	
	@Override
	public String extractApacheLibVersion(String sourceCode) {
		return null;
	}
	
	@Override
	public MavenProject readPOM(String pom) {
		MavenProject fromXML = (MavenProject) xstream.fromXML(pom);
		return fromXML;
	}
	
}
