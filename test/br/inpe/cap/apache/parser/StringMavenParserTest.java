package br.inpe.cap.apache.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import br.inpe.cap.apache.parser.StringMavenParser;

public class StringMavenParserTest extends MavenParserTest {
	
	@Before
	public void setUp() {
		parser = new StringMavenParser();
	}
	
	@Test
	public void testExtractLib() {
		assertEquals("org.apache.commons", parser.extractApacheLib(pom));
	}
	
	@Test
	public void testExtractLibVersion() {
		assertEquals("3.3.2", parser.extractApacheLibVersion(pom));
	}
	
	@Test
	public void testExtractLibWithoutVersion() {
		assertEquals("no version", parser.extractApacheLibVersion(pom));
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