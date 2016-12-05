package br.inpe.cap.evolution.processor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.repodriller.persistence.csv.CSVFile;

public class AprioriAggregatorStandAlonePostProcessor {

	public static void main(String[] args) throws IOException {
		System.out.println("Starting...");
		
		String pathToLook = "study\\detector_first35\\evolutions";
		String outputPath = "study\\artifact_aggregation\\";
		new File(outputPath).mkdirs();
		List<List<String>> linesCsvInputs = new ArrayList<>();
		List<File> arquivos = org.repodriller.util.FileUtils.getAllFilesInPath(pathToLook);
		for (File csvInput : arquivos) {
			linesCsvInputs.add(FileUtils.readLines(csvInput));
		}
		
		AprioriAggregatorPostProcessor processor = new AprioriAggregatorPostProcessor();
		String csvOutput = outputPath + "all-dependency-first35-apriori.csv";
		processor.process(new CSVFile(csvOutput), linesCsvInputs);
			
		System.out.println("Finished.");

	}
	
}
