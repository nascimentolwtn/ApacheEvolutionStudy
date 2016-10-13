package br.inpe.cap.auxiliar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;

public class GithubApiCsvToHttpsUrls {

	public static void main(String[] args) throws Exception {
		System.out.println("Starting...");
		execute();
		System.out.println("Finish!");
	}
	
	public static void execute() throws Exception {
			
		FileReader arquivo = new FileReader("fountain"+File.separator+"results-java.csv");
		BufferedReader reader = new BufferedReader(arquivo);
		
		FileWriter output = new FileWriter("fountain"+File.separator+"java-stars.url");
		Writer writer = new BufferedWriter(output);
		
		String linha = reader.readLine();
		linha = reader.readLine(); // Read twice in order to skip the header line of the CSV
		while (linha != null) {
			String[] resultSplit = linha.split(",");
			if(reposHasMoreThan1000Stars(resultSplit)) {
				String apiUrl = resultSplit[1];
				String[] urlNameSpaces = apiUrl.split("/");
				writer.write("https://github.com/" + urlNameSpaces[4] + "/" + urlNameSpaces[5] + "\n");
			}
			linha = reader.readLine();
		}
		
		reader.close();
		arquivo.close();
		
		writer.close();
		output.close();
			
	}

	private static boolean reposHasMoreThan1000Stars(String[] resultSplit) {
		return Integer.valueOf(resultSplit[2]) >= 1000;
	}

}