package br.inpe.cap.evolution.processor.date;

import static br.inpe.cap.evolution.maven.CommitLine.CommitLineType.DATE_VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Stream;

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
	private Stream<String> listCsvLines;
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
		listCsvLines = FileUtils.readLines(new File(Resources.getResource("all-dependency-'disconf'.csv").toURI())).stream();
		outputFileName = RESOURCE_OUTPUT_DIRECTORY + "test-maven-search-'disconf'.csv";
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
		
		assertEquals("23/03/2013 10:59:36", parsedOutputCommitLine.getVersionReleaseDate());
		assertEquals("0.9.9-RC1", parsedOutputCommitLine.getNewerVersionOnCommitDate());
		assertTrue(parsedOutputCommitLine.isUsingNewestVersion());
	}
	
	@After
	public void clearTempFiles() throws IOException {
		// Arquivo "temporário" deveria ser deletado.
		// Entretanto, a independência dos testes é garantida com o argumento do csvOutput append = false 
		FileUtils.forceDeleteOnExit(fileOutput);
	}
	
}