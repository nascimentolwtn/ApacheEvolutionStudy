package br.inpe.cap.evolution.processor;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;

import br.inpe.cap.evolution.maven.CommitLine;

public class JoinSummaryCSVPostProcessor {
	
	private static final String APACHE_FILE_PREFIX = "all_dependency_detector";
	private static final String STUDY_LOG_PATH = "." + File.separator + "study" + File.separator + APACHE_FILE_PREFIX;
	private static final String EVOLUTION_LOG_PATH = STUDY_LOG_PATH + File.separator + "evolutions";
	private static final File OUTPUT = new File(EVOLUTION_LOG_PATH + "_joined.csv");

	private boolean processCsvWithHeader = false;
	
	public static void main(String[] args) throws Exception {
		System.out.println("Starting...");
		new JoinSummaryCSVPostProcessor(true).process(EVOLUTION_LOG_PATH, OUTPUT, CommitLine.HEADER);
		System.out.println("Finish!");
	}

	public JoinSummaryCSVPostProcessor() {}
	
	public JoinSummaryCSVPostProcessor(boolean processCsvWithHeader) {
		this.processCsvWithHeader = processCsvWithHeader;
	}
	
	public void process(String evolutionLogPath, File output, String header) throws Exception {
		
		File evolutions = new File(evolutionLogPath);
		Iterator<File> iterateFiles = FileUtils.iterateFiles(evolutions, FileFileFilter.FILE, null);

		if(processCsvWithHeader) FileUtils.write(output, header+"\n");

		iterateFiles.forEachRemaining(
			(f) -> {
				try {
					if(processCsvWithHeader) {
						List<String> lines = FileUtils.readLines(f);
						CommitLine.removeHeader(lines);
						FileUtils.writeLines(output, lines, true);
					} else {
						FileUtils.writeStringToFile(output, FileUtils.readFileToString(f), true);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
	}

	public void process(String evolutionLogPath, File file) throws Exception {
		process(evolutionLogPath, file, CommitLine.HEADER);
	}

}