package br.inpe.cap.evolution.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.Test;

import br.inpe.cap.evolution.domain.MavenDependency;
import br.inpe.cap.evolution.domain.MavenProject;
import br.inpe.cap.evolution.domain.MavenProjectProperty;


public class XmlMavenParserTest extends MavenParserAbstractTest {

	private static XmlMavenParser parser = new XmlMavenParser();
	
	@Test
	public void leituraDasDependenciasDoMaven() {
		MavenProject projectFromPOM = parser.readPOM(pom);
		List<MavenDependency> dependencies = projectFromPOM.getDependencies();
		assertTrue(Collection.class.isAssignableFrom(dependencies.getClass()));
		assertNotNull(dependencies);
		assertTrue(dependencies.size() > 0);
		assertEquals(6, dependencies.size());
	}
	
	@Test
	public void lerPrimeiraDependenciaDoMaven() {
		MavenProject projectFromPOM = parser.readPOM(pom);
		MavenDependency dependency = projectFromPOM.getDependencies().get(0);
		assertEquals("org.eclipse.tycho", dependency.getGroupId());
		assertEquals("org.eclipse.jdt.core", dependency.getArtifactId());
		assertEquals("3.10.0.v20140604-1726", dependency.getVersion());
	}
	
	@Test
	public void dependenciasIguais() {
		MavenProject projectFromPOM = parser.readPOM(pom);
		List<MavenDependency> dependencies = projectFromPOM.getDependencies();
		MavenDependency dependency1 = dependencies.get(0);
		MavenDependency dependency2 = dependencies.get(1);
		assertEquals(dependency1, dependency2);
		assertNotSame(dependency1, dependency2);
	}
	
	@Test
	public void dependenciaSemVersao() {
		MavenProject projectFromPOM = parser.readPOM(pom);
		List<MavenDependency> dependencies = projectFromPOM.getDependencies();
		MavenDependency dependency = dependencies.get(3);
		assertEquals("org.apache.velocity", dependency.getGroupId());
		assertEquals("velocity-tools", dependency.getArtifactId());
		assertNull(dependency.getVersion());
	}
	
	@Test
	public void lerVariaveisDefinidasNoPOMdoMaven() {
		MavenProject projectFromPOM = parser.readPOM(pom);
		List<MavenProjectProperty> properties = projectFromPOM.getProperties();

		MavenProjectProperty property1 = properties.get(0);
		assertEquals("neo4j.version", property1.getName());
		assertEquals("3.0.4", property1.getValue());

		MavenProjectProperty property2 = properties.get(1);
		assertEquals("project.build.sourceEncoding", property2.getName());
		assertEquals("UTF-8", property2.getValue());
	}
	
	@Test
	public void versaoDeDepenciaPorVariavel() {
		MavenProject projectFromPOM = parser.readPOM(pom);
		List<MavenDependency> dependencies = projectFromPOM.getDependencies();
		MavenDependency dependency = dependencies.get(4);
		assertEquals("3.0.4", dependency.getVersion());
	}
	
	@Test
	public void retirandoLineFeedCarriageReturnDasDependencias() {
		MavenProject projectFromPOM = parser.readPOM(pom);
		List<MavenDependency> dependencies = projectFromPOM.getDependencies();
		MavenDependency dependency = dependencies.get(5);
		assertEquals("de.tudarmstadt.ukp.dkpro.core", dependency.getGroupId());
		assertEquals("de.tudarmstadt.ukp.dkpro.core.opennlp-model-tagger-en-maxent", dependency.getArtifactId());
		assertEquals("20120616.0", dependency.getVersion());
	}
	
	@Test
	public void pomSemNenhumaDependencia() {
		MavenProject projectFromPOM = parser.readPOM(pomNoDependencies);
		List<MavenDependency> dependencies = projectFromPOM.getDependencies();
		assertEquals(0, dependencies.size());
	}
	
	@Test
	public void versaoDeDepenciaPorVariavelSemDefinicaoDeProperties() {
		MavenProject projectFromPOM = parser.readPOM(pomNoPropertiesDefined);
		List<MavenDependency> dependencies = projectFromPOM.getDependencies();
		MavenDependency dependency = dependencies.get(0);
		assertEquals("${neo4j.version}", dependency.getVersion());
	}
	
	@Test
	public void versaoDeDepenciaSemDefinicaoDeVariavel() {
		MavenProject projectFromPOM = parser.readPOM(pomNoVersionDefined);
		List<MavenDependency> dependencies = projectFromPOM.getDependencies();
		MavenDependency dependency = dependencies.get(0);
		assertNull(dependency.getVersion());
	}
	
	@Test
	public void variaveisProject() {
		MavenProject projectFromPOM = parser.readPOM(pomProjectVariables);
		List<MavenDependency> dependencies = projectFromPOM.getDependencies();
		MavenDependency dependency = dependencies.get(0);
		assertEquals("org.apache.river", dependency.getGroupId());
		assertEquals("river-dl", dependency.getArtifactId());
		assertEquals("3.0-SNAPSHOT", dependency.getVersion());
	}
	
	@Test
	public void variaveisProjectDefinidasNoParent() {
		MavenProject projectFromPOM = parser.readPOM(pomProjectParentVariables);
		List<MavenDependency> dependencies = projectFromPOM.getDependencies();
		MavenDependency dependency = dependencies.get(0);
		assertEquals("org.apache.river", dependency.getGroupId());
		assertEquals("reggie", dependency.getArtifactId());
		assertEquals("3.0-SNAPSHOT", dependency.getVersion());
	}
	
}
