package br.inpe.cap.auxiliar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;

public class SearchUrlByCsvRepository {

	public static void main(String[] args) throws Exception {
		System.out.println("Starting...");
		execute();
		System.out.println("Finish!");
	}
	
	public static void execute() throws Exception {
			
		FileReader csv = new FileReader("fountain" + File.separator + "evolutions_joined_first35.csv");
		BufferedReader readerCsv = new BufferedReader(csv);
		
		FileWriter output = new FileWriter("fountain" + File.separator + "stars-maven_first35.urls");
		Writer writer = new BufferedWriter(output);

		String linha = readerCsv.readLine();
		csv:
		while (linha != null) {
			FileReader url = new FileReader("fountain" + File.separator + "stars-maven.urls");
			BufferedReader readerUrl = new BufferedReader(url);
			String linhaUrl = readerUrl.readLine();
			while (linhaUrl != null) {
				if(linhaUrl.contains(linha)) {
					writer.write(linhaUrl+"\n");
					readerUrl.close();
					url.close();
					linha = readerCsv.readLine();
					continue csv;
				}
				linhaUrl = readerUrl.readLine();
			}
			readerUrl.close();
			url.close();
			linha = readerCsv.readLine();
		}
		
		readerCsv.close();
		
		writer.close();
		output.close();
			
	}

}