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
		
		String pathToLook = "D:\\apriori";
		List<List<String>> linesCsvInputs = new ArrayList<>();
		List<File> arquivos = org.repodriller.util.FileUtils.getAllFilesInPath(pathToLook);
		for (File csvInput : arquivos) {
			System.out.println("Reading lines from " + csvInput.getName());
			linesCsvInputs.add(FileUtils.readLines(csvInput));
		}
		
		AprioriAggregatorPostProcessor processor = new AprioriAggregatorPostProcessor();
		String csvOutput = "study\\artifact_aggregation\\all-dependency-first35+next10_04-apriori.csv";
		processor.process(new CSVFile(csvOutput), linesCsvInputs);
			
		System.out.println("Finished.");

	}
	
}
