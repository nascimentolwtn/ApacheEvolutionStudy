package br.inpe.cap.apache.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import br.inpe.cap.apache.domain.MavenDependency;

public class XmlMavenParserTest extends MavenParserAbstractTest {

	private XmlMavenParser parser = new XmlMavenParser();

	@Test
	public void readListaDependenciasMaven() {
		MavenDependency dependency = parser.readPOM(pom).getDependencies().get(0);
		assertEquals("org.eclipse.tycho", dependency.getGroupId());
		assertEquals("org.eclipse.jdt.core", dependency.getArtifactId());
		assertEquals("3.10.0.v20140604-1726", dependency.getVersion());
	}
	
	@Test
	public void listaDependenciasMaven() {
		List<MavenDependency> dependencies = parser.readPOM(pom).getDependencies();
		assertNotNull(dependencies);
		assertTrue(dependencies.size() > 0);
	}
	
}
