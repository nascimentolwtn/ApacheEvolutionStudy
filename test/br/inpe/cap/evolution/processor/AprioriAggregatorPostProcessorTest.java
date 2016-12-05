package br.inpe.cap.evolution.processor;

import static br.inpe.cap.evolution.processor.VersionEvolutionDetectorPostProcessorTest.RESOURCE_OUTPUT_DIRECTORY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.repodriller.persistence.csv.CSVFile;

import com.google.common.io.Resources;

import br.inpe.cap.evolution.maven.AprioriLine;
import br.inpe.cap.evolution.maven.CommitLine;

public class AprioriAggregatorPostProcessorTest {
	
	private List<List<String>> linesCsvInputs = new ArrayList<>();
	private AprioriAggregatorPostProcessor processor;
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
		processor = new AprioriAggregatorPostProcessor();
		File csvFile1 = new File(Resources.getResource("apriori\\all-dependency-'blueprints'.csv").toURI());
		File csvFile2 = new File(Resources.getResource("apriori\\all-dependency-'disconf'.csv").toURI());
		linesCsvInputs.add(FileUtils.readLines(csvFile1));
		linesCsvInputs.add(FileUtils.readLines(csvFile2));
		outputFileName = RESOURCE_OUTPUT_DIRECTORY + "test-all-dependency-apriori.csv";
		csvOutput = new CSVFile(outputFileName, false);
		fileOutput = new File(outputFileName);
	}

	@Test
	public void leituraDeTodosArtifactsDeTodosSubprojetosDoUltimoCommitEscritaNoHeader() throws IOException {
		processor.process(csvOutput, linesCsvInputs);

		Set<String> allSubProjectArtifacts = processor.getAllSubProjectArtifacts();
		assertEquals(85, allSubProjectArtifacts.size());

		List<String> outputLines = FileUtils.readLines(fileOutput);
		String outputHeader = outputLines.get(0);
		assertTrue(outputHeader.startsWith(AprioriLine.FIRST_CELL));
		for (String artifact : allSubProjectArtifacts) {
			assertTrue(outputHeader.contains(artifact));
		}
	}
	
	@Test
	public void leituraDosSubmodulosEscritaLinhasApriori() throws IOException {
		processor.process(csvOutput, linesCsvInputs);
		
		List<String> outputLines = FileUtils.readLines(fileOutput);
		CommitLine.removeHeader(outputLines);
		assertEquals(12, outputLines.size());
		
		String blueprints_core = outputLines.get(0);
		assertQtdContaisAndNotContains(blueprints_core, "blueprints-core", 6, 79);
		
		String blueprints_graph_jung = outputLines.get(1);
		assertQtdContaisAndNotContains(blueprints_graph_jung, "blueprints-graph-jung", 5, 80);
		
		String blueprints_graph_sail = outputLines.get(2);
		assertQtdContaisAndNotContains(blueprints_graph_sail, "blueprints-graph-sail", 15, 70);
		
		String blueprints_neo4j_graph = outputLines.get(3);
		assertQtdContaisAndNotContains(blueprints_neo4j_graph, "blueprints-neo4j-graph", 5, 80);
		
		String blueprints_neo4j2_graph = outputLines.get(4);
		assertQtdContaisAndNotContains(blueprints_neo4j2_graph, "blueprints-neo4j2-graph", 9, 76);
		
		String blueprints_rexster_graph = outputLines.get(5);
		assertQtdContaisAndNotContains(blueprints_rexster_graph, "blueprints-rexster-graph", 3, 82);
		
		String blueprints_sail_graph = outputLines.get(6);
		assertQtdContaisAndNotContains(blueprints_sail_graph, "blueprints-sail-graph", 8, 77);
		
		String blueprints_sparksee_graph = outputLines.get(7);
		assertQtdContaisAndNotContains(blueprints_sparksee_graph, "blueprints-sparksee-graph", 3, 82);
		
		String blueprints_test = outputLines.get(8);
		assertQtdContaisAndNotContains(blueprints_test, "blueprints-test", 2, 83);
		
		String disconf_client = outputLines.get(9);
		assertQtdContaisAndNotContains(disconf_client, "disconf-client", 20, 65);
		
		String disconf_core = outputLines.get(10);
		assertQtdContaisAndNotContains(disconf_core, "disconf-core", 11, 74);
		
		String disconf_web = outputLines.get(11);
		assertQtdContaisAndNotContains(disconf_web, "disconf-web", 47, 38);
	
	}

	private void assertQtdContaisAndNotContains(String aprioriLine, String expectedSubProject, int expectedQtdContains, int expectedQtdNotContains) {
		String[] apririSplit = aprioriLine.split(",");
		int qtdContains = Arrays.asList(apririSplit).stream().filter((c)->c.equals(AprioriLine.CONTAINS_ARTIFACT)).collect(Collectors.toList()).size();
		int qtdNotContains = Arrays.asList(apririSplit).stream().filter((c)->c.equals(AprioriLine.NOT_CONTAINS_ARTIFACT)).collect(Collectors.toList()).size();
		assertTrue(aprioriLine.contains(expectedSubProject));
		assertEquals(expectedQtdContains, qtdContains);
		assertEquals(expectedQtdNotContains, qtdNotContains);
	}

	@After
	public void clearTempFiles() throws IOException {
		// Arquivo "temporário" deveria ser deletado.
		// Entretanto, a independência dos testes é garantida com o argumento do csvOutput append = false 
		FileUtils.forceDeleteOnExit(fileOutput);
	}
	
}
