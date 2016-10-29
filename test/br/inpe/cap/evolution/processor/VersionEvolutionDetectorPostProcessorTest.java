package br.inpe.cap.evolution.processor;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.io.Resources;

import br.com.metricminer2.persistence.csv.CSVFile;
import br.inpe.cap.evolution.domain.MavenDependency;
import br.inpe.cap.evolution.maven.CommitLine;

public class VersionEvolutionDetectorPostProcessorTest {
	
	private VersionEvolutionDetectorPostProcessor postProcessor = new VersionEvolutionDetectorPostProcessor();
	private List<String> listCsvLines;
	private String outputFileName;
	private CSVFile csvOutput;
	private File fileOutput;
	
	@Before
	public void setUpFiles() throws IOException, URISyntaxException {
		listCsvLines = FileUtils.readLines(new File(Resources.getResource("all-dependency-'disconf'.csv").toURI()));
		outputFileName = "study" + File.separator + "version-detector-'disconf'.csv";
		csvOutput = new CSVFile(outputFileName, false);
		fileOutput = new File(outputFileName);
	}
	
	@Test
	public void escritaDoHeader() throws IOException {
		postProcessor.processCsvLines(csvOutput, listCsvLines);
		List<String> outputLines = FileUtils.readLines(fileOutput);
		assertEquals(CommitLine.HEADER, outputLines.get(0));
	}
	
	@Test
	public void leituraDaPrimeiraDependencia() throws IOException {
		postProcessor.processCsvLines(csvOutput, listCsvLines);
		List<String> outputLines = FileUtils.readLines(fileOutput);
		CommitLine parsedOutputCommitLine = CommitLine.parseOutputCommitLine(outputLines.get(1));
		MavenDependency actualDependency = postProcessor.getMavenDependencyFromCSVLine(parsedOutputCommitLine);
		
		MavenDependency expectedDependency = new MavenDependency();
		expectedDependency.setGroupId("org.reflections");
		expectedDependency.setArtifactId("reflections");
		expectedDependency.setVersion("0.9.9-RC1");
		
		assertEquals(expectedDependency, actualDependency);
		
		boolean expectedVersionHasChanged = false;
		assertEquals(CommitLine.INITIAL_VERSION, parsedOutputCommitLine.getPreviousVersion());
		assertEquals(expectedVersionHasChanged, parsedOutputCommitLine.versionHasChanged());
	}
	
	@Test
	@Ignore
	public void leituraDoPrimeiroCommit() throws IOException {
		postProcessor.processCsvLines(csvOutput, listCsvLines);
		List<String> outputLines = FileUtils.readLines(fileOutput);
		assertEquals(10, outputLines.size());
	}
	
	@After
	public void clearTempFiles() throws IOException {
		// Arquivo "temporário" deveria ser deletado
		// Mas a independência dos testes é garantida com o argumento do csvOutput append = false 
		FileUtils.forceDeleteOnExit(fileOutput);
	}
	

}