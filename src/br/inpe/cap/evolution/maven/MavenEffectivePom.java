package br.inpe.cap.evolution.maven;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.repodriller.util.SimpleCommandExecutor;

public class MavenEffectivePom {

	// Poderia ser parametrizado, ou utilizado plugin hierarchy maven plugin.
	// Mas com a forma 'hard-coded' dessa classe, procuro garantir maior perfomance na execução multi-thread. 
	
	// Trocar esta linha de comando para cada máquina
	private static final String MAVEN_PATH = "C:\\Progra~2\\apache-maven-3.1.1\\bin\\mvn.bat";
	private static final String MAVEN_EFFECTIVE_POM_ONLINE = "help:effective-pom";
	private static final String MAVEN_EFFECTIVE_POM_OFFLINE = "help:effective-pom -o";
	private static final String MAVEN_EFFECTIVE_POM_EXTRACT_COMMAND_ONLINE = 
								MAVEN_PATH + " " + MAVEN_EFFECTIVE_POM_ONLINE;
	@SuppressWarnings("unused")
	private static final String MAVEN_EFFECTIVE_POM_EXTRACT_COMMAND_OFFLINE = 
								MAVEN_PATH + " " + MAVEN_EFFECTIVE_POM_OFFLINE;

	private static final String MAVEN_PROJECT_START_TOKEN = "<project ";
	private static final String END_PROJECT_TAG = "</project>";
	private static final int END_PROJECT_TAG_LENGTH = END_PROJECT_TAG.length();

	private final SimpleCommandExecutor executor;
	
	public MavenEffectivePom() {
		executor = new SimpleCommandExecutor();
		executor.setEnvironmentVar("JAVA_HOME", "C:\\Program Files\\Java\\jdk1.8.0_111");
		executor.setEnvironmentVar("M2_HOME", "C:\\Program Files (x86)\\apache-maven-3.1.1");
	}
	
	public String resolveEffectivePom(final File file) throws UnparsableEffectivePomException {
		final String basePath = file.toPath().getParent().toString();
		final String executeResult = this.executor.execute(MAVEN_EFFECTIVE_POM_EXTRACT_COMMAND_ONLINE, basePath);
		return this.extractPom(executeResult);
	}
	
	private String extractPom(final String execultResult) throws UnparsableEffectivePomException {
		try {
			return execultResult.substring(
					   execultResult.indexOf(MAVEN_PROJECT_START_TOKEN),
					   execultResult.indexOf(END_PROJECT_TAG)+END_PROJECT_TAG_LENGTH);
		} catch (StringIndexOutOfBoundsException e) {
			throw new UnparsableEffectivePomException(execultResult);
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		MavenEffectivePom mep = new MavenEffectivePom();
		
		String root = mep.executor.execute(MAVEN_EFFECTIVE_POM_EXTRACT_COMMAND_ONLINE,
"C:\\Users\\Borges\\Google Drive\\INPE CAP-389 Projeto Ágil\\ApacheEvolutionStudy");
//				"C:\\Users\\Borges\\git\\repodriller");
//		String root = executor.execute(MAVEN_EFFECTIVE_POM_EXTRACT_COMMAND_ONLINE,
//	"C:\\Users\\Borges\\Google~1\\INPEca~1\\");
//		FileUtils.writeStringToFile(new File("root.xml"), mep.extractPom(root));
		System.out.println("result:");
//		System.out.println(root);
		System.out.println(mep.extractPom(root));
		
		System.exit(0);
		
		String client = mep.executor.execute(MAVEN_EFFECTIVE_POM_EXTRACT_COMMAND_ONLINE, 
				"G:\\HD-Games\\GitRepos\\disconf\\disconf-client");
		FileUtils.writeStringToFile(new File("client.xml"), mep.extractPom(client));
//		System.out.println(extractPom(client));
		
		String core = mep.executor.execute(MAVEN_EFFECTIVE_POM_EXTRACT_COMMAND_ONLINE, 
				"G:\\HD-Games\\GitRepos\\disconf\\disconf-core");
		FileUtils.writeStringToFile(new File("core.xml"), mep.extractPom(core));
//		System.out.println(extractPom(core));
		
		String web = mep.executor.execute(MAVEN_EFFECTIVE_POM_EXTRACT_COMMAND_ONLINE, 
				"G:\\HD-Games\\GitRepos\\disconf\\disconf-web");
		FileUtils.writeStringToFile(new File("web.xml"), mep.extractPom(web));
//		System.out.println(extractPom(web));

	}

}
