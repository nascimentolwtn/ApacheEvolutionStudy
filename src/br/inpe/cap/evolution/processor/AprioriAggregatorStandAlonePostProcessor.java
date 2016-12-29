package br.inpe.cap.evolution.processor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.repodriller.persistence.csv.CSVFile;
import org.repodriller.util.FileUtils;

public class AprioriAggregatorStandAlonePostProcessor {

	public static void main(String[] args) throws IOException {
		System.out.println("Starting...");
		
		String pathToLook = "D:\\apriori";
		List<File> linesCsvInputs = new ArrayList<>(160);
		for (File csvInput : FileUtils.getAllFilesInPath(pathToLook)) {
			System.out.println("Reading lines from " + csvInput.getName());
			linesCsvInputs.add(csvInput);
		}
		
		AprioriAggregatorPostProcessor processor = new AprioriAggregatorPostProcessor();
		String csvOutput = "D:\\all-dependency-first35+next10_12-run12-apriori(QuestionMark).csv";
		processor.process(new CSVFile(csvOutput), linesCsvInputs);
		
		System.out.println("Finished.");

	}
	
}
