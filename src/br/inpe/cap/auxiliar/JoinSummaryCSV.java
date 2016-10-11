package br.inpe.cap.auxiliar;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;

public class JoinSummaryCSV {
	
	private static final String APACHE_FILE_PREFIX = "modifications-threadpool";
	private static final String STUDY_LOG_PATH = "." + File.separator + "study" + File.separator + APACHE_FILE_PREFIX;
	private static final String EVOLUTION_LOG_PATH = STUDY_LOG_PATH + File.separator + "modifications";
	private static final File OUTPUT = new File(EVOLUTION_LOG_PATH + ".csv");

	public static void main(String[] args) throws Exception {
		System.out.println("Starting...");
		joinSummaryCSV(EVOLUTION_LOG_PATH, OUTPUT);
		System.out.println("Finish!");
	}
	
	public static void joinSummaryCSV(String evolutionLogPath, File output) throws Exception {
		
		File evolutions = new File(evolutionLogPath);
		Iterator<File> iterateFiles = FileUtils.iterateFiles(evolutions, FileFileFilter.FILE, null);
		
		iterateFiles.forEachRemaining(
				(f) -> {
					try {
						FileUtils.writeStringToFile(output, FileUtils.readFileToString(f), true);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
	}

}