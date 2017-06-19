package br.inpe.cap.evolution.processor;

import static br.inpe.cap.evolution.maven.CommitLine.INITIAL_VERSION;
import static br.inpe.cap.evolution.maven.CommitLine.CommitLineType.OUTPUT;
import static org.junit.Assert.assertEquals;

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

public class RemoveUnchangedArtifactsPostProcessorTest {
	
	public static final String RESOURCE_OUTPUT_DIRECTORY = 
			"test" 		+ File.separator +
			"resources"	+ File.separator +
			"output"	+ File.separator;
	private RemoveUnchangedArtifactsPostProcessor postProcessor;
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
		listCsvLines = FileUtils.readLines(new File(Resources.getResource("all-dependency-detector-'blueprints'.csv").toURI())).stream();
		outputFileName = RESOURCE_OUTPUT_DIRECTORY + "test-remove-unchanged-version-'blueprints'.csv";
		csvOutput = new CSVFile(outputFileName, false);
		fileOutput = new File(outputFileName);
	}
	
	@Test
	public void removeUnchangedArtifacts() throws IOException {
		postProcessor = new RemoveUnchangedArtifactsPostProcessor(false);
		postProcessor.processCsvLines(csvOutput, listCsvLines);
		List<String> outputLines = FileUtils.readLines(fileOutput);
		
		CommitLine.removeHeader(outputLines);
		boolean expectedVersionHasChanged = true;
		outputLines.forEach((line) -> {
			CommitLine parsedOutputCommitLine = CommitLine.parseCommitLine(line, OUTPUT);
			assertEquals(expectedVersionHasChanged, parsedOutputCommitLine.versionHasChanged());
		});
	}
	
	@Test
	public void removeUnchangedArtifactsKeepInitialVersion() throws IOException {
		postProcessor = new RemoveUnchangedArtifactsPostProcessor(true);
		postProcessor.processCsvLines(csvOutput, listCsvLines);
		List<String> outputLines = FileUtils.readLines(fileOutput);
		
		CommitLine.removeHeader(outputLines);
		for(int index = 0; index < outputLines.size(); index++) {
			CommitLine parsedOutputCommitLine = CommitLine.parseCommitLine(outputLines.get(index), OUTPUT);
			
			if(index <= 16) {
				assertEquals("bc58a6f95d36798b2b0485596ed7dca89dd2b10a", parsedOutputCommitLine.getHash());
				assertEquals(INITIAL_VERSION, parsedOutputCommitLine.getPreviousVersion());
				assertEquals(false, parsedOutputCommitLine.versionHasChanged());
			} else if(index == 30) {
				assertEquals("4993d3c093412d40b627be33e29375e6e0383ae8", parsedOutputCommitLine.getHash());
				assertEquals("jredis", parsedOutputCommitLine.getGroupId());
				assertEquals("org.jredis.ri", parsedOutputCommitLine.getArtifactId());
				assertEquals("a.0-SNAPSHOT-jar-with-dependencies", parsedOutputCommitLine.getVersion());
				assertEquals(INITIAL_VERSION, parsedOutputCommitLine.getPreviousVersion());
				assertEquals(false, parsedOutputCommitLine.versionHasChanged());
			} 

			boolean expectedVersionChanged = !INITIAL_VERSION.equals(parsedOutputCommitLine.getPreviousVersion()) || parsedOutputCommitLine.versionHasChanged();
			assertEquals(expectedVersionChanged, parsedOutputCommitLine.versionHasChanged());
			
		};
	}

	@After
	public void clearTempFiles() throws IOException {
		// Arquivo "temporário" deveria ser deletado.
		// Entretanto, a independência dos testes é garantida com o argumento do csvOutput append = false 
		FileUtils.forceDeleteOnExit(fileOutput);
	}
	
}