package br.inpe.cap.apache;

import br.com.metricminer2.persistence.PersistenceMechanism;
import br.com.metricminer2.persistence.csv.CSVFile;

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
