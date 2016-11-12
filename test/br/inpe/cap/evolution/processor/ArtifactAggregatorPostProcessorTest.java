package br.inpe.cap.evolution.processor;

import static br.inpe.cap.evolution.processor.VersionEvolutionDetectorPostProcessorTest.RESOURCE_OUTPUT_DIRECTORY;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.repodriller.persistence.csv.CSVFile;

import com.google.common.io.Resources;

import br.inpe.cap.evolution.maven.CommitLine;

public class ArtifactAggregatorPostProcessorTest {
	
	private ArtifactAggregatorPostProcessor processor;
	private List<String> linesCsvInput;
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
	public void setUp() throws URISyntaxException, IOException {
		processor = new ArtifactAggregatorPostProcessor();
		File csvFile = new File(Resources.getResource("all-dependency-'disconf'.csv").toURI());
		linesCsvInput = FileUtils.readLines(csvFile);
		outputFileName = RESOURCE_OUTPUT_DIRECTORY + "test-all-dependency-'disconf'-aggregated.csv";
		csvOutput = new CSVFile(outputFileName, false);
		fileOutput = new File(outputFileName);
	}

	@Test
	public void leituraDoUltimoCommit() {
		CommitLine parseLastCommitLine = processor.parseLastCommitLine(linesCsvInput);
		assertEquals("54c69f25cd46541d0cea7b1730c198fba4e4ac71", parseLastCommitLine.getHash());
	}
	
	@Test
	public void leituraDosSubmodulos() throws IOException {
		CommitLine parseLastCommitLine = processor.parseLastCommitLine(linesCsvInput);
		processor.process(csvOutput, linesCsvInput, parseLastCommitLine.getHash());
		
		List<String> outputLines = FileUtils.readLines(fileOutput);
		assertEquals(ArtifactAggregatorPostProcessor.HEADER, outputLines.get(0));
		CommitLine.removeHeader(outputLines);
		assertEquals(3, outputLines.size());
		Assert.assertTrue(outputLines.get(0).contains(ArtifactAggregatorPostProcessor.AGGREGATE_SEPARATOR));
	}

	@Test(expected = RuntimeException.class)
	public void processarCsvVazio() {
		processor.parseLastCommitLine(Collections.emptyList());
	}
	
	@After
	public void clearTempFiles() throws IOException {
		// Arquivo "temporário" deveria ser deletado.
		// Entretanto, a independência dos testes é garantida com o argumento do csvOutput append = false 
		FileUtils.forceDeleteOnExit(fileOutput);
	}
	
}
