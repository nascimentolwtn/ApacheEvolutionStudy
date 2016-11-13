package br.inpe.cap.evolution.processor;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.repodriller.persistence.csv.CSVFile;

import br.inpe.cap.evolution.maven.CommitLine;

public class ArtifactAggregatorStandAlonePostProcessor {

	public static void main(String[] args) throws Exception {
		
		System.out.println("Starting...");
		
		String pathToLook = "study\\detector_first35\\evolutions";
		String outputPath = "study\\artifact_aggregation\\evolutions\\";
		new File(outputPath).mkdirs();
		List<File> arquivos = org.repodriller.util.FileUtils.getAllFilesInPath(pathToLook);
		
		arquivos.parallelStream().forEach((csvInput)-> {
			
			try {
				ArtifactAggregatorPostProcessor processor = new ArtifactAggregatorPostProcessor();
				List<String> linesCsvInput = FileUtils.readLines(csvInput);
				
				CommitLine parseCommitLine = processor.parseLastCommitLine(linesCsvInput);
				
				String csvOutput = outputPath
						+ csvInput.getName().substring(0, csvInput.getName().lastIndexOf(".csv"))
						+ "_aggregated.csv";
				processor.process(new CSVFile(csvOutput), linesCsvInput, parseCommitLine.getHash());
				System.out.println(csvOutput + " processded.");
			} catch (RuntimeException | IOException e) {
				e.printStackTrace();
			}
			
		});

		System.out.println("Finished.");
		
	}
	
}
