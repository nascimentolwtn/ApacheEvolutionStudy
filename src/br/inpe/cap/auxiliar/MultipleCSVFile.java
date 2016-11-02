package br.inpe.cap.auxiliar;

import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.persistence.csv.CSVFile;

public class MultipleCSVFile implements PersistenceMechanism {
	
	private CSVFile[] csvFiles;
	
	public MultipleCSVFile(CSVFile... csvFiles) {
		this.csvFiles = csvFiles;
	}
	
	@Override
	public synchronized void write(Object... line) {
		for (CSVFile csvFile : this.csvFiles) {
			csvFile.write(line);
		}
	}

	@Override
	public void close() {
		for (CSVFile csvFile : this.csvFiles) {
			csvFile.close();
		}
	}

}
