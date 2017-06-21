package br.inpe.cap.evolution.processor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.repodriller.persistence.csv.CSVFile;

public class RemoveUnchangedArtifactsStandAlonePostProcessor {

	public static void main(String[] args) {

		System.out.println("Starting...");
		
		final File pathToLook = new File("C:\\Users\\LuizWagner\\Desktop\\evolutions_joined\\maven_search\\maven_search-first35");
		final String outputPath = pathToLook + File.separator + "output_without-initials";
		new File(outputPath).mkdirs();
		Collection<File> arquivos = FileUtils.listFiles(pathToLook, null, false);
		
		arquivos.parallelStream().forEach((csvInput)-> {
			
			final RemoveUnchangedArtifactsPostProcessor postProcessor = new RemoveUnchangedArtifactsPostProcessor(false);
			final String name = csvInput.getName();
			System.out.println("Processing "+name);
			final CSVFile csvOutput = new CSVFile(outputPath + File.separator + name);
			
			try {
				final Stream<String> listCsvLines = Files.lines(Paths.get(csvInput.toURI()));
				postProcessor.processCsvLines(csvOutput, listCsvLines);
			} catch (RuntimeException | IOException e) {
				e.printStackTrace();
			}
			
			System.gc();
			System.out.println("Done "+name);
			
		});

		System.out.println("Finish!");
	
	}
	
}
