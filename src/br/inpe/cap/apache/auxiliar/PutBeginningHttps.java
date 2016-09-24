package br.inpe.cap.apache.auxiliar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;

public class PutBeginningHttps {

	public static void main(String[] args) throws Exception {
		System.out.println("Starting...");
		execute();
		System.out.println("Finish!");
	}
	
	public static void execute() throws Exception {
			
		FileReader arquivo = new FileReader("github_urls_top40_early_import_mean.csv");
		BufferedReader reader = new BufferedReader(arquivo);
		
		FileWriter output = new FileWriter("github_urls_top40_early_import_mean.url");
		Writer writer = new BufferedWriter(output);

		String linha = reader.readLine();
		while (linha != null) {
			writer.write("https://github.com/" + linha + "\n");
			linha = reader.readLine();
		}
		
		reader.close();
		arquivo.close();
		
		writer.close();
		output.close();
			
	}

}