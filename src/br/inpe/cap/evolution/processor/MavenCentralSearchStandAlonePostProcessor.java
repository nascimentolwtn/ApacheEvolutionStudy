package br.inpe.cap.evolution.processor;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.repodriller.persistence.csv.CSVFile;

import br.inpe.cap.evolution.processor.date.MavenCentralSearchPostProcessor;

public class MavenCentralSearchStandAlonePostProcessor {

	public static void main(String[] args) {

		System.out.println("Starting...");
		long inicio = System.currentTimeMillis();
		
		final File pathToLook = new File("C:\\temp\\detector");
		final String outputPath = pathToLook + File.separator + "output";
		pathToLook.mkdirs();
		new File(outputPath).mkdirs();
		Collection<File> arquivos = FileUtils.listFiles(pathToLook, null, false);
		
		arquivos.parallelStream().forEach((csvInput) -> {
			
			final MavenCentralSearchPostProcessor postProcessor = new MavenCentralSearchPostProcessor();
			final String name = csvInput.getName();
			System.out.println("Processing "+name);
			String fileOutput = outputPath + File.separator + name;
			final CSVFile csvOutput = new CSVFile(fileOutput);
			
			try {
				final List<String> inputLines = FileUtils.readLines(csvInput);
				postProcessor.processCsvLines(csvOutput, inputLines);
				
				if(inputLines.size() != FileUtils.readLines(new File(fileOutput)).size())
					System.err.println("Line qtd error processing " + name);
				
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Error processing " + name);
			}

			System.gc();
			
			System.out.println("Done "+name);
			
		});

		System.out.println("Finish in " + (System.currentTimeMillis() - inicio) / 1000 + "s!");
	
	}
	
}
