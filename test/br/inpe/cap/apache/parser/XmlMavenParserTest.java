package br.inpe.cap.apache.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.Test;

import br.inpe.cap.apache.domain.MavenDependency;
import br.inpe.cap.apache.domain.MavenProject;

public class XmlMavenParserTest extends MavenParserAbstractTest {

	private XmlMavenParser parser = new XmlMavenParser();
	private MavenProject projectFromPOM = parser.readPOM(pom);
	
	@Test
	public void leituraDasDependenciasDoMaven() {
		List<MavenDependency> dependencies = projectFromPOM.getDependencies();
		assertTrue(Collection.class.isAssignableFrom(dependencies.getClass()));
		assertNotNull(dependencies);
		assertTrue(dependencies.size() > 0);
		assertTrue(dependencies.size() == 4);
	}
	
	@Test
	public void lerPrimeiraDependenciaDoMaven() {
		MavenDependency dependency = projectFromPOM.getDependencies().get(0);
		assertEquals("org.eclipse.tycho", dependency.getGroupId());
		assertEquals("org.eclipse.jdt.core", dependency.getArtifactId());
		assertEquals("3.10.0.v20140604-1726", dependency.getVersion());
	}
	
	@Test
	public void dependenciasIguais() {
		List<MavenDependency> dependencies = projectFromPOM.getDependencies();
		MavenDependency dependency1 = dependencies.get(0);
		MavenDependency dependency2 = dependencies.get(1);
		assertEquals(dependency1, dependency2);
		assertNotSame(dependency1, dependency2);
	}
	
	@Test
	public void dependenciaSemVersao() {
		List<MavenDependency> dependencies = projectFromPOM.getDependencies();
		MavenDependency dependency = dependencies.get(3);
		assertEquals("org.apache.velocity", dependency.getGroupId());
		assertEquals("velocity-tools", dependency.getArtifactId());
		assertNull(dependency.getVersion());
	}
	
}
