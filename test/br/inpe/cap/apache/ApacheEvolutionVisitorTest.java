package br.inpe.cap.apache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class ApacheEvolutionVisitorTest {
	
	private static String pom;

	@BeforeClass
	public static void setUp() {
		StringBuilder sb = new StringBuilder();
		sb.append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n");
		sb.append("	<modelVersion>4.0.0</modelVersion>\n");
		sb.append("	<groupId>br.usp</groupId>\n");
		sb.append("	<artifactId>metricminer</artifactId>\n");
		sb.append("	<packaging>jar</packaging>\n");
		sb.append("	<version>2.4.2-SNAPSHOT</version>\n");
		sb.append("	<name>metricminer2</name>\n");
		sb.append("	<url>http://www.metricminer.org.br</url>\n");
		sb.append("	<description>Framework for researchers in MSR</description>\n");
		sb.append("\n");
		sb.append("	<dependencies>\n");
		sb.append("		<dependency>\n");
		sb.append("			<groupId>org.eclipse.tycho</groupId>\n");
		sb.append("			<artifactId>org.eclipse.jdt.core</artifactId>\n");
		sb.append("			<version>3.10.0.v20140604-1726</version>\n");
		sb.append("		</dependency>\n");
		sb.append("\n");
		sb.append("	<dependencies>\n");
		sb.append("		<dependency>\n");
		sb.append("			<groupId>org.eclipse.tycho</groupId>\n");
		sb.append("			<artifactId>org.eclipse.jdt.core</artifactId>\n");
		sb.append("			<version>3.10.0.v20140604-1726</version>\n");
		sb.append("		</dependency>\n");
		sb.append("\n");
		sb.append("		<dependency>\n");
		sb.append("			<groupId>org.apache.commons</groupId>\n");
		sb.append("			<artifactId>commons-lang3</artifactId>\n");
//		sb.append("			<version>3.3.2</version>\n");
		sb.append("		</dependency>\n");
		sb.append("\n");
		sb.append("		<dependency>\n");
		sb.append("			<groupId>org.apache.velocity</groupId>\n");
		sb.append("			<artifactId>velocity-tools</artifactId>\n");
		sb.append("			<scope>provided</scope>\n");
		sb.append("			<optional>true</optional>\n");
		sb.append("		</dependency>\n");
		sb.append("\n");
		sb.append("	</dependencies>\n");
		sb.append("\n");
		sb.append("	<build>\n");
		sb.append("		<plugins>\n");
		sb.append("			<plugin>\n");
		sb.append("				<groupId>org.apache.felix</groupId>\n");
		sb.append("				<artifactId>maven-bundle-plugin</artifactId>\n");
		sb.append("			</plugin>\n");
		sb.append("\n");
		sb.append("			<plugin>\n");
		sb.append("				<groupId>org.apache.felix</groupId>\n");
		sb.append("				<artifactId>maven-bundle-plugin</artifactId>\n");
//		sb.append("				<version>2.10.1</version>\n");
		sb.append("			</plugin>\n");
		sb.append("		</plugins>\n");
		sb.append("	</build>\n");
		sb.append("\n");
		sb.append("</project>\n");
                
		pom = sb.toString();
	}
	
	@Test
	public void testExtractLib() {
		ApacheEvolutionVisitor visitor = new ApacheEvolutionVisitor();
		assertEquals("org.apache.commons", visitor.extractApacheLib(pom));
	}
	
	@Test
	public void testExtractLibVersion() {
		ApacheEvolutionVisitor visitor = new ApacheEvolutionVisitor();
		assertEquals("3.3.2", visitor.extractApacheLibVersion(pom));
	}
	
	@Test
	public void testExtractLibWithoutVersion() {
		ApacheEvolutionVisitor visitor = new ApacheEvolutionVisitor();
		assertEquals("no version", visitor.extractApacheLibVersion(pom));
	}
	
	@Test
	public void testExtractCommonsIO() {
		// FIXME Tratar casos especiais 1 a 1? Ex.: commons-io
		fail("<groupId> da biblioteca Commons.io não é org.apache. É <groupId>commons-io</groupId>");
	}
	
	@Test
	@Ignore
	public void testParsePomDeProjetoDaPropriaApacheOuFork() {
		// FIXME Verificar como ficará o parsing das versões dos projetos da própria Apache (talvez verificar <parent>?)
		fail("indexOf('<version>') de projetos Apache pegam o primeiro version, referente ao <parent> da própria Apache.");
	}
	
}