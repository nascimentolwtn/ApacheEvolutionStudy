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
import org.repodriller.scm.GitRemoteRepository;

import br.inpe.cap.auxiliar.MultipleCSVFile;
import br.inpe.cap.evolution.processor.JoinSummaryCSVPostProcessor;
import br.inpe.cap.evolution.visitor.FilterMavenDependencyVisitor;

public class FilterMavenDependencyVersionEvolutionRemoteStarsStudy implements Study {

	private static final int THREADS_FOR_REPOSITORIES = 10;
	private static final String FOUNTAIN_PATH = "fountain" + File.separator;
	private static final String STUDY_TEMP_PATH = "E:\\metricminer-evolution-stars-NOBARE";

	private static final String STUDY_LOG_PATH = "." + File.separator + "study" + File.separator + "filter_stars_06";
	private static final String EVOLUTION_LOG_PATH = STUDY_LOG_PATH + File.separator + "evolutions";
	
	private static final String FILE_PREFIX = "dependency_filter-HOME";
	private static final String EVOLUTION_SUMMARY_CSV = STUDY_LOG_PATH + File.separator + FILE_PREFIX + ".csv"; 

	private static final File GITHUB_URLS_FILE = new File(FOUNTAIN_PATH+"java-stars.urls");
	private static final File GITHUB_DONE_FILE = new File(FOUNTAIN_PATH+"done-github_filter-stars_HOME.txt");
	private static final File EXCEPTION_FILE = new File(FOUNTAIN_PATH+"exceptions-filter-stars_HOME.log");
	
	private static Logger log;
	
	public static void main(String[] args) throws Exception {
		System.setProperty("git.maxfiles", "2000");
		System.setProperty("logfilename", FILE_PREFIX + "_filter06");
		log = Logger.getLogger(RepositoryMining.class);
		
		checkRequiredLogFilesAndDirectories();
		
		new RepoDriller().start(new FilterMavenDependencyVersionEvolutionRemoteStarsStudy());
		
		new JoinSummaryCSVPostProcessor().process(EVOLUTION_LOG_PATH, new File(EVOLUTION_LOG_PATH + "_joined.csv"));
		
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
			GitRemoteRepository gitRemoteRepository = GitRemoteRepository
					.hostedOn(gitUrl)
					.inTempDir(tempDir)
					.build();
			new RepositoryMining()
				.in(gitRemoteRepository.info())
				.through(Commits.onlyInHead())
				.process(new FilterMavenDependencyVisitor(EVOLUTION_LOG_PATH, gitReposLogSubDir), new MultipleCSVFile(
						new CSVFile(EVOLUTION_SUMMARY_CSV, true)
						,
						new CSVFile(EVOLUTION_LOG_PATH
								+ File.separator
								+ "filter-stars-'"
								+ gitReposLogSubDir
								+ "'.csv")))
				.mine();
			markDone(gitUrl);
			gitRemoteRepository.deleteTempGitPath();
		} catch (Exception re) {
			log.error(re.getMessage());
		} finally {
			System.gc();
		}
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