package br.inpe.cap.evolution.processor.date;

import static br.inpe.cap.evolution.maven.CommitLine.CommitLineType.DATE_VERSION;
import static br.inpe.cap.evolution.processor.date.search.MavenCentralSearch.DATE_NOT_FOUND;
import static br.inpe.cap.evolution.processor.date.search.MavenCentralSearch.VERSION_NOT_FOUND;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.repodriller.persistence.csv.CSVFile;

import com.google.common.io.Resources;

import br.inpe.cap.evolution.maven.CommitLine;

public class MavenCentralSearchPostProcessorTest {
	
	public static final String RESOURCE_OUTPUT_DIRECTORY = 
			"test" 		+ File.separator +
			"resources"	+ File.separator +
			"output"	+ File.separator;
	private MavenCentralSearchPostProcessor postProcessor = new MavenCentralSearchPostProcessor();
	private List<String> listCsvLines;
	private String outputFileName;
	private CSVFile csvOutput;
	private File fileOutput;
	
	@BeforeClass
	public static void checkResourceOutputDirectory() {
		File outputDir = new File(RESOURCE_OUTPUT_DIRECTORY);
		if(!outputDir.exists()) {
			outputDir.mkdirs();
		}
	}
	
	@Before
	public void setUpFiles() throws IOException, URISyntaxException {
		listCsvLines = FileUtils.readLines(new File(Resources.getResource("all-dependency-reprocess-'blueprints'.csv").toURI()));
		outputFileName = RESOURCE_OUTPUT_DIRECTORY + "test-maven-search-'reprocess-blueprints'.csv";
		csvOutput = new CSVFile(outputFileName, false);
		fileOutput = new File(outputFileName);
	}
	
	@Test
	public void escritaDoHeader() throws IOException {
		postProcessor.processCsvLines(csvOutput, listCsvLines);
		List<String> outputLines = FileUtils.readLines(fileOutput);
		assertEquals(CommitLine.OUTPUT_DATESEARCH_HEADER, outputLines.get(0));
	}
	
	@Test
	public void leituraDaPrimeiraDataEVersao() throws IOException {
		postProcessor.processCsvLines(csvOutput, listCsvLines);
		List<String> outputLines = FileUtils.readLines(fileOutput);
		CommitLine parsedOutputCommitLine = CommitLine.parseCommitLine(outputLines.get(1), DATE_VERSION);
		
		assertEquals("jung-algorithms", parsedOutputCommitLine.getArtifactId());
		assertEquals("10/04/2009 05:29:32", parsedOutputCommitLine.getVersionReleaseDate());
		assertEquals("2.0.1", parsedOutputCommitLine.getNewerVersionOnCommitDate());
		assertFalse(parsedOutputCommitLine.isUsingNewestVersion());
	}
	
	@Test
	public void leituraDosCommitsUsandoAUltimaVersao() throws IOException {
		postProcessor.processCsvLines(csvOutput, listCsvLines);
		List<String> outputLines = FileUtils.readLines(fileOutput);
		CommitLine parsedOutputCommitLine = CommitLine.parseCommitLine(outputLines.get(50), DATE_VERSION);
		assertEquals("commons-io", parsedOutputCommitLine.getArtifactId());
		assertEquals("09/10/2012 16:28:51", parsedOutputCommitLine.getVersionReleaseDate());
		assertEquals("1.3.2", parsedOutputCommitLine.getNewerVersionOnCommitDate());
		assertTrue(parsedOutputCommitLine.isUsingNewestVersion());

		parsedOutputCommitLine = CommitLine.parseCommitLine(outputLines.get(54), DATE_VERSION);
		assertEquals("jung-api", parsedOutputCommitLine.getArtifactId());
		assertEquals("24/01/2010 15:18:06", parsedOutputCommitLine.getVersionReleaseDate());
		assertEquals("2.0.1", parsedOutputCommitLine.getNewerVersionOnCommitDate());
		assertTrue(parsedOutputCommitLine.isUsingNewestVersion());

		parsedOutputCommitLine = CommitLine.parseCommitLine(outputLines.get(72), DATE_VERSION);
		assertEquals("sparkseejava", parsedOutputCommitLine.getArtifactId());
		assertEquals("16/09/2014 08:12:57", parsedOutputCommitLine.getVersionReleaseDate());
		assertEquals("5.1.0", parsedOutputCommitLine.getNewerVersionOnCommitDate());
		assertTrue(parsedOutputCommitLine.isUsingNewestVersion());
	}
	
	@Test
	public void leituraDeCommitSemEncontrarVersaoNoMavenCentral() throws IOException {
		postProcessor.processCsvLines(csvOutput, listCsvLines);
		List<String> outputLines = FileUtils.readLines(fileOutput);
		CommitLine parsedOutputCommitLine = CommitLine.parseCommitLine(outputLines.get(2), DATE_VERSION);
		assertEquals("neo4j-kernel", parsedOutputCommitLine.getArtifactId());
		assertEquals(DATE_NOT_FOUND, parsedOutputCommitLine.getVersionReleaseDate());
		assertEquals(VERSION_NOT_FOUND, parsedOutputCommitLine.getNewerVersionOnCommitDate());
		assertFalse(parsedOutputCommitLine.isUsingNewestVersion());

		parsedOutputCommitLine = CommitLine.parseCommitLine(outputLines.get(51), DATE_VERSION);
		assertEquals("rexster-core", parsedOutputCommitLine.getArtifactId());
		assertEquals(DATE_NOT_FOUND, parsedOutputCommitLine.getVersionReleaseDate());
		assertEquals("2.6.0", parsedOutputCommitLine.getNewerVersionOnCommitDate());
		assertFalse(parsedOutputCommitLine.isUsingNewestVersion());
	}
	
	@After
	public void clearTempFiles() throws IOException {
		// Arquivo "temporário" deveria ser deletado.
		// Entretanto, a independência dos testes é garantida com o argumento do csvOutput append = false 
		FileUtils.forceDeleteOnExit(fileOutput);
	}
	
}