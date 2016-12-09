package br.inpe.cap.evolution.processor;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.repodriller.persistence.csv.CSVFile;

public class VersionEvolutionDetectorStandAlonePostProcessor {

	public static void main(String[] args) {

		System.out.println("Starting...");
		
		final String pathToLook = "D:\\version_reprocess";
		final String outputPath = pathToLook + File.separator + "output";
		new File(outputPath).mkdirs();
		List<File> arquivos = org.repodriller.util.FileUtils.getAllFilesInPath(pathToLook);
		
		arquivos.parallelStream().forEach((csvInput)-> {
			
			final VersionEvolutionDetectorPostProcessor postProcessor = new VersionEvolutionDetectorPostProcessor();
			final String name = csvInput.getName();
			System.out.println("Processing "+name);
			final CSVFile csvOutput = new CSVFile(outputPath + File.separator + name);
			
			try {
				List<String> listCsvLines = FileUtils.readLines(csvInput);
				postProcessor.reprocessVersionDetectorOutputCsvLines(csvOutput, listCsvLines);
			} catch (RuntimeException | IOException e) {
				e.printStackTrace();
			}
			
		});

		
		System.out.println("Finish!");
	
	}
	
}
