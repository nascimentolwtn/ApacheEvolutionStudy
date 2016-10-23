package br.inpe.cap.evolution.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.Test;

import br.inpe.cap.evolution.domain.MavenDependency;
import br.inpe.cap.evolution.domain.MavenDependencyManagement;
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
		assertEquals("river-lib", dependency.getArtifactId());
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
	
	@Test
	public void lerModules() {
		MavenProject projectFromPOM = parser.readPOM(pom8290_startOfDependencyManagement);
		List<String> modules = projectFromPOM.getModules();
		assertTrue(Collection.class.isAssignableFrom(modules.getClass()));
		assertNotNull(modules);
		assertTrue(modules.size() > 0);
		assertEquals(6, modules.size());
		assertEquals("graylog2-plugin-interfaces", modules.get(0));
		assertEquals("graylog2-shared", modules.get(1));
		assertEquals("graylog2-server", modules.get(2));
		assertEquals("graylog2-inputs", modules.get(3));
		assertEquals("graylog2-radio", modules.get(4));
		assertEquals("graylog2-rest-routes", modules.get(5));
	}

	
	@Test
	public void lerDependencyManagement() {
		MavenProject projectFromPOM = parser.readPOM(pom8290_startOfDependencyManagement);
		MavenDependencyManagement dependencyManagement = projectFromPOM.getDependencyManagement();
		assertNotNull(dependencyManagement);
		
		List<MavenDependency> dependencies = dependencyManagement.getDependencies();
		assertTrue(dependencies.size() > 0);
		assertEquals(57, dependencies.size());

		MavenDependency dependency = dependencies.get(0);
		assertEquals("org.graylog2", dependency.getGroupId());
		assertEquals("graylog2-plugin", dependency.getArtifactId());
		assertEquals("0.21.0-SNAPSHOT", dependency.getVersion());
		assertTrue(dependency.isDependencyManaged());
		
		dependency = dependencies.get(9);
		assertEquals("com.codahale.metrics", dependency.getGroupId());
		assertEquals("metrics-core", dependency.getArtifactId());
		assertEquals("3.0.0", dependency.getVersion());
		assertTrue(dependency.isDependencyManaged());
	}
	
	@Test
	public void lerTodasDependenciasInclusiveDependencyManagement() {
		MavenProject projectFromPOM = parser.readPOM(pom8290_startOfDependencyManagement);
		List<MavenDependency> dependencies = projectFromPOM.getAllDependencies();
		assertNotNull(dependencies);
		assertTrue(dependencies.size() > 0);
		assertEquals(62, dependencies.size());

		MavenDependency dependency = dependencies.get(0);
		assertEquals("org.graylog2", dependency.getGroupId());
		assertEquals("graylog2-plugin", dependency.getArtifactId());
		assertEquals("0.21.0-SNAPSHOT", dependency.getVersion());
		assertTrue(dependency.isDependencyManaged());

		dependency = dependencies.get(57);
		assertEquals("org.slf4j", dependency.getGroupId());
		assertEquals("slf4j-api", dependency.getArtifactId());
		assertNull(dependency.getVersion());
		assertFalse(dependency.isDependencyManaged());
	}
	
	@Test
	public void invalidPOMvariaveisProjectNaoDefinidasNoParent() {
		MavenProject projectFromPOM = parser.readPOM(pomProjectParentInvalidVariables);
		List<MavenDependency> dependencies = projectFromPOM.getDependencies();
		MavenDependency dependency = dependencies.get(0);
		assertEquals("${project.groupId}", dependency.getGroupId());
		assertEquals("${project.artifactId}", dependency.getArtifactId());
		assertEquals("${project.version}", dependency.getVersion());
	}
	
	@Test
	public void invalidPOMRetirarCaracteresIndesejadosProCSVDoMeioDosValores() {
		MavenProject projectFromPOM = parser.readPOM(pomProjectParentInvalidVariables);
		List<MavenDependency> dependencies = projectFromPOM.getDependencies();
		MavenDependency dependency = dependencies.get(1);
		assertEquals("LineFeedIsValid but undesirable for CSV", dependency.getGroupId());
		assertEquals("Comma is not expected here but it'd scramble CSV also!", dependency.getArtifactId());
		assertEquals("10.0", dependency.getVersion());
	}
	
}
