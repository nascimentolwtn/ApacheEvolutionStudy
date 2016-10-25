package br.inpe.cap.evolution.maven;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;

import br.com.metricminer2.util.SimpleCommandExecutor;

public class MavenEffectivePom {

	// Poderia ser customizado, ou utilizado plugin hierarchy maven plugin.
	// Mas da forma 'hard-coded' dessa classe, procuro garantir maior perfomance na execução multi-thread. 
	
	// Trocar esta linha de comando para cada máquina
	private static final String MAVEN_PATH = "C:\\Progra~2\\apache-maven-3.3.9\\bin\\mvn.cmd";
	private static final String MAVEN_EFFECTIVE_POM = "help:effective-pom -o";
	private static final String MAVEN_EFFECTIVE_POM_EXTRACT_COMMAND = 
								MAVEN_PATH + " " + MAVEN_EFFECTIVE_POM;
	
	private static final String MAVEN_PROJECT_START_TOKEN = "<project ";
	private static final String END_PROJECT_TAG = "</project>";
	private static final int END_PROJECT_TAG_LENGTH = END_PROJECT_TAG.length();

	private SimpleCommandExecutor executor = new SimpleCommandExecutor();
	
	public String resolveEffectivePom(final File file) {
		final String basePath = file.getAbsolutePath();
		final String executeResult = this.executor.execute(MAVEN_EFFECTIVE_POM_EXTRACT_COMMAND, basePath);
		return this.extractPom(executeResult);
	}
	
	private String extractPom(final String execultResult) {
		return execultResult.substring(
				   execultResult.indexOf(MAVEN_PROJECT_START_TOKEN),
				   execultResult.indexOf(END_PROJECT_TAG)+END_PROJECT_TAG_LENGTH);
	}
	
	public static void main(String[] args) throws IOException, URISyntaxException {
		
		MavenEffectivePom mep = new MavenEffectivePom();
		
		SimpleCommandExecutor executor = new SimpleCommandExecutor();
		String root = executor.execute(MAVEN_EFFECTIVE_POM_EXTRACT_COMMAND, 
				"G:\\HD-Games\\GitRepos\\disconf");
		FileUtils.writeStringToFile(new File("root.xml"), mep.extractPom(root));
//		System.out.println(extractPom(root));
		
		String client = executor.execute(MAVEN_EFFECTIVE_POM_EXTRACT_COMMAND, 
				"G:\\HD-Games\\GitRepos\\disconf\\disconf-client");
		FileUtils.writeStringToFile(new File("client.xml"), mep.extractPom(client));
//		System.out.println(extractPom(client));
		
		String core = executor.execute(MAVEN_EFFECTIVE_POM_EXTRACT_COMMAND, 
				"G:\\HD-Games\\GitRepos\\disconf\\disconf-core");
		FileUtils.writeStringToFile(new File("core.xml"), mep.extractPom(core));
//		System.out.println(extractPom(core));
		
		String web = executor.execute(MAVEN_EFFECTIVE_POM_EXTRACT_COMMAND, 
				"G:\\HD-Games\\GitRepos\\disconf\\disconf-web");
		FileUtils.writeStringToFile(new File("web.xml"), mep.extractPom(web));
//		System.out.println(extractPom(web));

	}

}
