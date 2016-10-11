package br.inpe.cap.apache;

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
import br.com.metricminer2.persistence.csv.CSVFile;
import br.com.metricminer2.scm.GitRemoteRepository;
import br.com.metricminer2.scm.commitrange.Commits;
import br.inpe.cap.auxiliar.MultipleCSVFile;

public class RemoteApacheProjectsVersionEvolutionStudy implements Study {

	private static final int THREADS_FOR_REPOSITORIES = 10;
	private static final String STUDY_TEMP_PATH = "E:\\metricminer-apache-evolution";
	private static final String FOUNTAIN_PATH = "fountain" + File.separator;
	private static final String APACHE_FILE_PREFIX = "apache_evolution-REMOTE";
	private static final String STUDY_LOG_PATH = "." + File.separator + "study" + File.separator + APACHE_FILE_PREFIX;
	private static final String EVOLUTION_LOG_PATH = STUDY_LOG_PATH + File.separator + "evolutions";
	private static final String APACHE_EVOLUTION_SUMMARY_CSV = STUDY_LOG_PATH + File.separator + APACHE_FILE_PREFIX	+ ".csv"; 
	
	private static final File GITHUB_DONE_FILE = new File(FOUNTAIN_PATH+"done-github_evolution-REMOTE.txt");
	private static final File GITHUB_URLS_FILE = new File(FOUNTAIN_PATH+"github_urls_top10_early_import_mean.TXT");
	private static final File EXCEPTION_FILE = new File(FOUNTAIN_PATH+"exceptions-evolution-REMOTE.log");
	
	private static Logger log;

	public static void main(String[] args) throws IOException {
		System.setProperty("logfilename", APACHE_FILE_PREFIX + "_checkout01");
		log = Logger.getLogger(RepositoryMining.class);
		ApacheEvolutionVisitor.setLogger(log);
		
		checkRequiredLogFilesAndDirectories();
		
		new MetricMiner2().start(new RemoteApacheProjectsVersionEvolutionStudy());
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
				.process(new ApacheEvolutionVisitor(), new MultipleCSVFile(
						new CSVFile(APACHE_EVOLUTION_SUMMARY_CSV, true)
						,
						new CSVFile(EVOLUTION_LOG_PATH
							+ File.separator
							+ "apache-evolution-'"
							+ gitUrl.substring(gitUrl.lastIndexOf("/")+1, gitUrl.length())
							+ "'.csv")
						))
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