package br.inpe.cap.evolution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.repodriller.RepoDriller;
import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.range.Commits;
import org.repodriller.persistence.csv.CSVFile;
import org.repodriller.scm.GitRepository;

import br.inpe.cap.auxiliar.MultipleCSVFile;
import br.inpe.cap.evolution.processor.JoinSummaryCSVPostProcessor;
import br.inpe.cap.evolution.visitor.DependencyEvolutionVisitor;

public class MavenDependencyVersionEvolutionStudy implements Study {

	private static final int THREADS_FOR_REPOSITORIES = 10;
	private static final String FOUNTAIN_PATH = "fountain" + File.separator;

	private static final String STUDY_LOG_PATH = "." + File.separator + "study" + File.separator + "dependency_eval_02";
	private static final String EVOLUTION_LOG_PATH = STUDY_LOG_PATH + File.separator + "evolutions";
	
	private static final String FILE_PREFIX = "dependency_evolution-HOME";
	private static final String EVOLUTION_SUMMARY_CSV = STUDY_LOG_PATH + File.separator + FILE_PREFIX + ".csv"; 
	
	private static final File GITHUB_DONE_FILE = new File(FOUNTAIN_PATH+"done-github_evolution-HOME.txt");
	private static File EXCEPTION_FILE = new File(FOUNTAIN_PATH+"exceptions-evolution-HOME.log");
	
	private static Logger log;
	
	public static void main(String[] args) throws Exception {
		System.setProperty("git.maxfiles", "2000");
		System.setProperty("logfilename", FILE_PREFIX + "_run01");
		log = Logger.getLogger(RepositoryMining.class);
		DependencyEvolutionVisitor.setLogger(log);
		
		checkRequiredLogFilesAndDirectories();
		
		new RepoDriller().start(new MavenDependencyVersionEvolutionStudy());
		
		new JoinSummaryCSVPostProcessor().process(EVOLUTION_LOG_PATH, new File(EVOLUTION_LOG_PATH + "_joined.csv"));
		
		System.out.println("Finish!");
	}
	
	public void execute() {
		try {
			
			String rootRepositoriesPath = "E:\\metricminer2_studies_top20";
			List<String> gitRepoDirs = getRepositoryExceptDoneDirs(rootRepositoriesPath);

			ExecutorService execRepos = Executors.newFixedThreadPool(THREADS_FOR_REPOSITORIES);
			for(String repo : gitRepoDirs) {
				execRepos.submit(() -> 
					doMining(repo));
			}
		
			execRepos.shutdown();
			execRepos.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
			
		} catch (Exception e) {
			try {
				String causeMessage = "";
				if(e.getCause() != null)
					causeMessage = e.getCause().getMessage();
				FileUtils.writeStringToFile(EXCEPTION_FILE, e.getMessage() + ". Cause: " + causeMessage +"\n", true);
				System.gc();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	private void doMining(String gitReposDir) {
		try {
			String gitReposLogSubDir = gitReposDir.substring(gitReposDir.lastIndexOf(File.separator)+1, gitReposDir.length());
			new RepositoryMining()
				.in(GitRepository.singleProject(gitReposDir))
				.through(Commits.all())
//			.withThreads(3)
				.process(new DependencyEvolutionVisitor(EVOLUTION_LOG_PATH, gitReposLogSubDir), new MultipleCSVFile(
						new CSVFile(EVOLUTION_SUMMARY_CSV, true)
						,
						new CSVFile(EVOLUTION_LOG_PATH
							+ File.separator
							+ "dependency-evolution-'"
							+ gitReposLogSubDir
							+ "'.csv")))
				.mine();
		} catch (RuntimeException re) {
			log.error(re.getMessage());
		}
		markDone(gitReposDir);
		System.gc();
	}
	
	private List<String> getRepositoryExceptDoneDirs(String rootRepositoriesPath) throws IOException {
		List<String> allDirsIn = org.repodriller.util.FileUtils.getAllDirsIn(rootRepositoriesPath);
		
		FileReader arquivo = new FileReader(GITHUB_DONE_FILE);
		BufferedReader reader = new BufferedReader(arquivo);

		String linha = reader.readLine();
		while (linha != null) {
			allDirsIn.remove(linha);
			linha = reader.readLine();
		}

		reader.close();
		arquivo.close();
		return allDirsIn;
	}
		
	private void markDone(String gitUrl) {
		try {
			FileUtils.writeStringToFile(GITHUB_DONE_FILE, gitUrl + "\n", true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	private static void checkRequiredLogFilesAndDirectories() throws IOException {
		if(!GITHUB_DONE_FILE.exists()) {
			GITHUB_DONE_FILE.createNewFile();
		}
		
		File studyPathDir = new File(STUDY_LOG_PATH);
		if(!studyPathDir.exists()) {
			studyPathDir.mkdir();
		}
		
		File evolutionPathDir = new File(EVOLUTION_LOG_PATH);
		if(!evolutionPathDir.exists()) {
			evolutionPathDir.mkdir();
		}
	}

}