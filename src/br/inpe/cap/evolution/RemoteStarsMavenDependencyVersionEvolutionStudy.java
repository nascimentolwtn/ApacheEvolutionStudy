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

import br.com.metricminer2.MetricMiner2;
import br.com.metricminer2.RepositoryMining;
import br.com.metricminer2.Study;
import br.com.metricminer2.filter.range.Commits;
import br.com.metricminer2.persistence.csv.CSVFile;
import br.com.metricminer2.scm.GitRemoteRepository;
import br.inpe.cap.auxiliar.JoinSummaryCSV;
import br.inpe.cap.auxiliar.MultipleCSVFile;

public class RemoteStarsMavenDependencyVersionEvolutionStudy implements Study {

	private static final int THREADS_FOR_REPOSITORIES = 10;
	private static final String FOUNTAIN_PATH = "fountain" + File.separator;
	private static final String STUDY_TEMP_PATH = "E:\\metricminer-evolution-stars"; // System.getenv("STUDY_TEMP_PATH");

	private static final String STUDY_LOG_PATH = "." + File.separator + "study" + File.separator + "dependency_stars_01";
	private static final String EVOLUTION_LOG_PATH = STUDY_LOG_PATH + File.separator + "evolutions";
	
	private static final String FILE_PREFIX = "dependency_evolution-HOME";
	private static final String EVOLUTION_SUMMARY_CSV = STUDY_LOG_PATH + File.separator + FILE_PREFIX + ".csv"; 

	private static final File GITHUB_URLS_FILE = new File(FOUNTAIN_PATH+"stars-maven.urls");
	private static final File GITHUB_DONE_FILE = new File(FOUNTAIN_PATH+"done-github_evolution-stars_maven_HOME.txt");
	private static final File EXCEPTION_FILE = new File(FOUNTAIN_PATH+"exceptions-evolution-stars_HOME.log");
	
	private static Logger log;
	
	public static void main(String[] args) throws Exception {
		System.setProperty("logfilename", FILE_PREFIX + "_checkout01");
		log = Logger.getLogger(RepositoryMining.class);
		DependencyEvolutionVisitor.setLogger(log);
		
		checkRequiredLogFilesAndDirectories();
		
		new MetricMiner2().start(new RemoteStarsMavenDependencyVersionEvolutionStudy());
		
		JoinSummaryCSV.joinSummaryCSV(EVOLUTION_LOG_PATH, new File(EVOLUTION_LOG_PATH + "_joined.csv"));
		
		System.out.println("Finish!");
	}
	
	public void execute() {
		try {
			
			List<String> gitRepoUrl = getRepositoryExceptDoneUrls();

			ExecutorService execRepos = Executors.newFixedThreadPool(THREADS_FOR_REPOSITORIES);
			for(String url : gitRepoUrl) {
				execRepos.submit(() -> 
					doMining(url, STUDY_TEMP_PATH));
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

	private void doMining(String gitUrl, String tempDir) {
		try {
			String gitReposLogSubDir = gitUrl.substring(gitUrl.lastIndexOf("/")+1, gitUrl.length());
			Thread.currentThread().setName("Checkout " + gitReposLogSubDir);
			new RepositoryMining()
				.in(GitRemoteRepository
						.hostedOn(gitUrl)
						.inTempDir(tempDir)
						.asBareRepos()
						.withMaxNumberOfFilesInACommit(2000)
						.buildAsSCMRepository())
				.startingFromTheBeginning()
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
		markDone(gitUrl);
		System.gc();
	}
	
	private List<String> getRepositoryExceptDoneUrls() throws IOException {
		List<String> urls = FileUtils.readLines(GITHUB_URLS_FILE);
		
		FileReader arquivo = new FileReader(GITHUB_DONE_FILE);
		BufferedReader reader = new BufferedReader(arquivo);

		String linha = reader.readLine();
		while (linha != null) {
			urls.remove(linha);
			linha = reader.readLine();
		}

		reader.close();
		arquivo.close();
		return urls;
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