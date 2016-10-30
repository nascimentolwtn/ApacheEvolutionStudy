package br.inpe.cap.evolution.processor;

import static br.inpe.cap.evolution.maven.CommitLine.INITIAL_VERSION;
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
		assertEquals(INITIAL_VERSION, parsedOutputCommitLine.getPreviousVersion());
		assertEquals(expectedVersionHasChanged, parsedOutputCommitLine.versionHasChanged());
	}
	
	@Test
	public void leituraDoPrimeiroCommit() throws IOException {
		postProcessor.processCsvLines(csvOutput, listCsvLines);
		List<String> outputLines = FileUtils.readLines(fileOutput);
		VersionEvolutionDetectorPostProcessor.removeHeader(outputLines);
		
		String firstHash = CommitLine.parseOutputCommitLine(outputLines.get(0)).getHash();
		int firstCommitCount = 0;
		for (String line : outputLines) {
			CommitLine parsedOutputCommitLine = CommitLine.parseOutputCommitLine(line);
			if(parsedOutputCommitLine.getHash().equals(firstHash)) {
				assertEquals(INITIAL_VERSION, parsedOutputCommitLine.getPreviousVersion());
				firstCommitCount++;
			}
		}
		assertEquals(9, firstCommitCount);

	}
	
	@Test
	public void contagemDeTodosCommitsESubmodulosDoTerceiroCommit() throws IOException {
		postProcessor.processCsvLines(csvOutput, listCsvLines);
		List<String> outputLines = FileUtils.readLines(fileOutput);
		VersionEvolutionDetectorPostProcessor.removeHeader(outputLines);
		assertEquals(35727, outputLines.size());
		
		String thirdHash = CommitLine.parseOutputCommitLine(outputLines.get(19)).getHash();
		int thirdCommitCount = 0;
		int submodulesCount = 0;
		int[] submodulesDependencyCount = {0,0,0};
		int submoduleIndex = 0;
		String lastModule = null;
		for (String line : outputLines) {
			CommitLine parsedOutputCommitLine = CommitLine.parseOutputCommitLine(line);
			if(parsedOutputCommitLine.getHash().equals(thirdHash)) {
				thirdCommitCount++;

				String currentModule = parsedOutputCommitLine.getFile();
				if(lastModule == null) {
					lastModule = currentModule;
					submodulesCount++;
				}
				if(currentModule.equals(lastModule)) {
					submodulesDependencyCount[submoduleIndex]++;
				} else {
					lastModule = currentModule;
					submodulesCount++;
					submoduleIndex++;
					submodulesDependencyCount[submoduleIndex]++;
				}
			}
		}
		assertEquals(27, thirdCommitCount);
		assertEquals(3, submodulesCount);
		for (int submoduleCount : submodulesDependencyCount) {
			assertEquals(9, submoduleCount);
		}
	}
	
	@Test
	public void dependenciasDosSubmodulosDoTerceiroCommit() throws IOException {
		postProcessor.processCsvLines(csvOutput, listCsvLines);
		List<String> outputLines = FileUtils.readLines(fileOutput);
		VersionEvolutionDetectorPostProcessor.removeHeader(outputLines);
		
		int thirdCommitIndex = 18;
		int submoduleIndex = 0;
		String lastModule = null;
		
		CommitLine parsedOutputCommitLine = CommitLine.parseOutputCommitLine(outputLines.get(thirdCommitIndex));
		String thirdHash = CommitLine.parseOutputCommitLine(outputLines.get(thirdCommitIndex)).getHash();
		do {
			String currentModule = parsedOutputCommitLine.getFile();
			if(lastModule == null) {
				lastModule = currentModule;
			}
			if(currentModule.equals(lastModule)) {
				if(submoduleIndex == 0) {
					assertFalse(INITIAL_VERSION.equals(parsedOutputCommitLine.getPreviousVersion()));
				} else {
					assertTrue(INITIAL_VERSION.equals(parsedOutputCommitLine.getPreviousVersion()));
				}
			} else {
				lastModule = currentModule;
				submoduleIndex++;
			}
			parsedOutputCommitLine = CommitLine.parseOutputCommitLine(outputLines.get(++thirdCommitIndex));
		} while (parsedOutputCommitLine.getHash().equals(thirdHash));
	}
	
	@Test
	public void dependenciasDosSubmodulosDoQuartoCommit() throws IOException {
		postProcessor.processCsvLines(csvOutput, listCsvLines);
		List<String> outputLines = FileUtils.readLines(fileOutput);
		VersionEvolutionDetectorPostProcessor.removeHeader(outputLines);
		
		int thirdCommitIndex = 45;
		int submoduleIndex = 0;
		String lastModule = null;
		
		CommitLine parsedOutputCommitLine = CommitLine.parseOutputCommitLine(outputLines.get(thirdCommitIndex));
		String thirdHash = CommitLine.parseOutputCommitLine(outputLines.get(thirdCommitIndex)).getHash();
		do {
			String currentModule = parsedOutputCommitLine.getFile();
			if(lastModule == null) {
				lastModule = currentModule;
			}
			if(currentModule.equals(lastModule)) {
				if(submoduleIndex == 0 || submoduleIndex == 2) {
					assertTrue(INITIAL_VERSION.equals(parsedOutputCommitLine.getPreviousVersion()));
				} else {
					assertFalse(INITIAL_VERSION.equals(parsedOutputCommitLine.getPreviousVersion()));
				}
			} else {
				lastModule = currentModule;
				submoduleIndex++;
			}
			parsedOutputCommitLine = CommitLine.parseOutputCommitLine(outputLines.get(++thirdCommitIndex));
		} while (parsedOutputCommitLine.getHash().equals(thirdHash));
	}
	
	@After
	public void clearTempFiles() throws IOException {
		// Arquivo "temporário" deveria ser deletado.
		// Entretanto, a independência dos testes é garantida com o argumento do csvOutput append = false 
		FileUtils.forceDeleteOnExit(fileOutput);
	}
	

}