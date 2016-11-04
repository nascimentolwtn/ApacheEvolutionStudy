package br.inpe.cap.auxiliar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

public class ReorderStarsUrls {

	public static void main(String[] args) throws Exception {
		System.out.println("Starting...");
		writeRepositoryExceptDoneUrls();
		System.out.println("Finish!");
	}
	
	public static void execute() throws Exception {
		
		List<String> inputUrls = FileUtils.readLines(new File("fountain" + File.separator + "stars-maven.urls"));
		List<String> inputRepositories = new ArrayList<>();
		Map<String, String> repositoriesMap = new HashMap<>();
		for (String gitUrl : inputUrls) {
			String repository = gitUrl.substring(gitUrl.lastIndexOf("/")+1, gitUrl.length());
			inputRepositories.add(repository);
			repositoriesMap.put(repository, gitUrl);
		}
		Collections.sort(inputRepositories);
		List<String> outputRepositories = new ArrayList<>();
		for (String input : inputRepositories) {
			outputRepositories.add(repositoriesMap.get(input));
		}
		
		FileUtils.writeLines(new File("fountain" + File.separator + "stars-maven_ordered.urls"), outputRepositories);
			
	}
	
	private static void writeRepositoryExceptDoneUrls() throws IOException {
		final List<String> urls = FileUtils.readLines(new File("fountain" + File.separator + "stars-maven.urls"));
		
		final FileReader arquivo = new FileReader(new File("fountain" + File.separator + "stars-maven_first35.urls"));
		final BufferedReader reader = new BufferedReader(arquivo);

		String linha = reader.readLine();
		while (linha != null) {
			urls.remove(linha);
			linha = reader.readLine();
		}

		reader.close();
		arquivo.close();
		
		FileUtils.writeLines(new File("fountain" + File.separator + "stars-maven_except35.urls"), urls);
		
	}
		


}