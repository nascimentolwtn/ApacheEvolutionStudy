package br.inpe.cap.evolution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import br.com.metricminer2.MetricMiner2;
import br.com.metricminer2.RepositoryMining;
import br.com.metricminer2.Study;
import br.com.metricminer2.filter.commit.OnlyInMainBranch;
import br.com.metricminer2.filter.commit.OnlyModificationsWithFileTypes;
import br.com.metricminer2.filter.commit.OnlyNoMerge;
import br.com.metricminer2.filter.range.Commits;
import br.com.metricminer2.persistence.csv.CSVFile;
import br.com.metricminer2.scm.GitRemoteRepository;
import br.inpe.cap.auxiliar.JoinSummaryCSV;

public class AllDependencyVersionsEvolutionStudy implements Study {

	private static final int THREADS_FOR_REPOSITORIES = 5;
	private static final String STUDY_TEMP_PATH = "E:\\metricminer-evolution-stars"; // System.getenv("STUDY_TEMP_PATH");
	private static final String FOUNTAIN_PATH = "fountain" + File.separator;

	private static final String STUDY_LOG_PATH = "." + File.separator + "study" + File.separator + "all_dependency";
	private static final String EVOLUTION_LOG_PATH = STUDY_LOG_PATH + File.separator + "evolutions";
	
	private static final String FILE_PREFIX = "all_dependency_first35-HOME";

	private static final File GITHUB_URLS_FILE = new File(FOUNTAIN_PATH+"stars-maven_first35.urls");
	private static final File GITHUB_DONE_FILE = new File(FOUNTAIN_PATH+"done-github_evolution-stars_maven_first35_HOME.txt");
	private static final File EXCEPTION_FILE = new File("study" + File.separator + "exceptions-all_dependency-HOME.log");
	
	private static Logger log;
	
	public static void main(String[] args) throws Exception {
		System.setProperty("logfilename", FILE_PREFIX + "_run02");
		log = Logger.getLogger(RepositoryMining.class);
		AllDependenciesEvolutionVisitor.setLogger(log);
		Thread.currentThread().setName(FILE_PREFIX);
		
		checkRequiredLogFilesAndDirectories();
		
		new MetricMiner2().start(new AllDependencyVersionsEvolutionStudy());
		
		String joinCSV = "_joined.csv";
		JoinSummaryCSV.joinSummaryCSV(EVOLUTION_LOG_PATH, new File(EVOLUTION_LOG_PATH + joinCSV));
		log.info("CSV joined: " + EVOLUTION_LOG_PATH + joinCSV);

		System.out.println("Finish!");
	}
	
	public void execute() {
		try {
			
			final List<String> gitRepoUrl = getRepositoryExceptDoneUrls();

			final ExecutorService execRepos = Executors.newFixedThreadPool(THREADS_FOR_REPOSITORIES);

			final List<String> fileExtensions = Arrays.asList("pom.xml");
			for(final String url : gitRepoUrl) {
				execRepos.submit(() -> 
					doMining(url, STUDY_TEMP_PATH, fileExtensions));
			}
		
			execRepos.shutdown();
			execRepos.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		
		} catch (final Exception e) {
			try {
				String causeMessage = "";
				if(e.getCause() != null)
					causeMessage = e.getCause().getMessage();
				FileUtils.writeStringToFile(EXCEPTION_FILE, e.getMessage() + ". Cause: " + causeMessage +"\n", true);
				System.gc();
			} catch (final IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	private void doMining(final String gitUrl, final String tempDir, final List<String> fileExtensions) {
		try {
			final String gitReposLogSubDir = gitUrl.substring(gitUrl.lastIndexOf("/")+1, gitUrl.length());
			Thread.currentThread().setName("Checkout " + gitReposLogSubDir);
			new RepositoryMining()
				.in(GitRemoteRepository
						.hostedOn(gitUrl)
						.inTempDir(tempDir)
						.withMaxNumberOfFilesInACommit(2000)
						.buildAsSCMRepository())
				.startingFromTheBeginning()
				.through(Commits.all())
				.withCommits(new OnlyInMainBranch(
							 new OnlyNoMerge(
							 new OnlyModificationsWithFileTypes(fileExtensions))))
				.process(new AllDependenciesEvolutionVisitor(),
						new CSVFile(EVOLUTION_LOG_PATH
								+ File.separator
								+ "all-dependency-'"
								+ gitReposLogSubDir
								+ "'.csv"))
				.mine();
		} catch (final RuntimeException re) {
			log.error(re.getMessage());
		}
		markDone(gitUrl);
		System.gc();
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

	private List<String> getRepositoryExceptDoneUrls() throws IOException {
		final List<String> urls = FileUtils.readLines(GITHUB_URLS_FILE);
		
		final FileReader arquivo = new FileReader(GITHUB_DONE_FILE);
		final BufferedReader reader = new BufferedReader(arquivo);

		String linha = reader.readLine();
		while (linha != null) {
			urls.remove(linha);
			linha = reader.readLine();
		}

		reader.close();
		arquivo.close();
		return urls;
	}
		
	private void markDone(final String gitUrl) {
		try {
			FileUtils.writeStringToFile(GITHUB_DONE_FILE, gitUrl + "\n", true);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
		


}