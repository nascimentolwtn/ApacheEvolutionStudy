package br.inpe.cap.evolution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.repodriller.RepoDriller;
import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.commit.OnlyInMainBranch;
import org.repodriller.filter.commit.OnlyModificationsWithFileTypes;
import org.repodriller.filter.commit.OnlyNoMerge;
import org.repodriller.filter.range.CommitRange;
import org.repodriller.filter.range.Commits;
import org.repodriller.filter.range.OnlyInHead;
import org.repodriller.persistence.csv.CSVFile;
import org.repodriller.scm.GitRemoteRepository;

import br.inpe.cap.evolution.maven.ConfigurableMavenEffectivePom.OS;
import br.inpe.cap.evolution.visitor.AllDependenciesEvolutionVisitor;

public class AllDependencyOnlyHeadOneVersionsEvolutionStudy implements Study {

	private static final String STUDY_TEMP_PATH = "E:\\metricminer-evolution-stars"; // System.getenv("STUDY_TEMP_PATH");
	private static final String FOUNTAIN_PATH = "fountain" + File.separator;

	private static final String STUDY_LOG_PATH = "." + File.separator + "study" + File.separator + "all_dependency_detector" + File.separator;
	private static final String EVOLUTION_LOG_PATH = STUDY_LOG_PATH + "evolutions-headOnly-HOME";
	
	private static final String FILE_PREFIX = "all_dependency_detector_blueprints-headOnly-HOME";

	private static final File GITHUB_URLS_FILE = new File(FOUNTAIN_PATH+"stars-maven_one.urls");
	private static final File GITHUB_DONE_FILE = new File(STUDY_LOG_PATH+"done-github_" + FILE_PREFIX + ".txt");
	private static final File CONTINUE_COMMIT_FILE = new File(STUDY_LOG_PATH + FILE_PREFIX + "-continue.properties");
	private static final File EXCEPTION_FILE = new File(STUDY_LOG_PATH + "exceptions-all_dependency-HOME.log");
	private static final Properties properties = new Properties();
	
	private static Logger log;
	
	public static void main(final String[] args) throws Exception {
		System.setProperty("git.maxfiles", "2000");
		System.setProperty("logfilename", FILE_PREFIX + "_run03");
		log = Logger.getLogger(RepositoryMining.class);
		AllDependenciesEvolutionVisitor.setLogger(log);
		Thread.currentThread().setName(FILE_PREFIX);
		
		checkRequiredLogFilesAndDirectories();
		AllDependenciesEvolutionVisitor.setContinueProperties(CONTINUE_COMMIT_FILE);
		
		new RepoDriller().start(new AllDependencyOnlyHeadOneVersionsEvolutionStudy());
		
		final String joinCSV = "_01_joined.csv";
//		new JoinSummaryCSVPostProcessor(true).process(EVOLUTION_LOG_PATH, new File(EVOLUTION_LOG_PATH + joinCSV));
		log.info("CSV joined: " + EVOLUTION_LOG_PATH + joinCSV);

		System.out.println("Finish!");
	}
	
	@Override
	public void execute() {
		try {
			
			final List<String> gitRepoUrl = getRepositoryExceptDoneUrls();

			final List<String> fileExtensions = Arrays.asList("");
			for(final String url : gitRepoUrl) {
				doMining(url, STUDY_TEMP_PATH, fileExtensions);
			}
		
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
			final GitRemoteRepository gitRepository = GitRemoteRepository
					.hostedOn(gitUrl)
					.inTempDir(tempDir)
					.build();
//			final CommitRange range = startOrContinueCommits(gitReposLogSubDir, gitRepository);
			final CommitRange range = new OnlyInHead(); 
			new RepositoryMining()
				.in(gitRepository.info())
				.through(range)
				.filters(new OnlyInMainBranch(),
						 new OnlyNoMerge(),
						 new OnlyModificationsWithFileTypes(fileExtensions))
				.process(new AllDependenciesEvolutionVisitor(OS.WIN8),
						new CSVFile(EVOLUTION_LOG_PATH
								+ File.separator
								+ "all-dependency-detector-'"
								+ gitReposLogSubDir
								+ "'.csv"))
				.mine();
			markDone(gitUrl);
		} catch (final Exception re) {
			log.error(re.getMessage());
		}
		System.gc();
	}

	@SuppressWarnings("unused")
	private CommitRange startOrContinueCommits(final String gitReposLogSubDir, final GitRemoteRepository gitRepository) throws IOException {
		final String startCommit = properties.getProperty(gitReposLogSubDir);
		if(startCommit == null) {
			return Commits.all();
		} else {
			return Commits.range(startCommit, gitRepository.getHead().getId());
		}
	}
	
	private static void checkRequiredLogFilesAndDirectories() throws IOException {
		if(!CONTINUE_COMMIT_FILE.exists()) {
			new File(STUDY_LOG_PATH).mkdir();
			new File(STUDY_LOG_PATH + FILE_PREFIX).mkdir();
			CONTINUE_COMMIT_FILE.createNewFile();
		}
		properties.load(new FileInputStream(CONTINUE_COMMIT_FILE));
		
		if(!GITHUB_DONE_FILE.exists()) {
			GITHUB_DONE_FILE.createNewFile();
		}
		
		final File studyPathDir = new File(STUDY_LOG_PATH);
		if(!studyPathDir.exists()) {
			studyPathDir.mkdir();
		}
		
		final File evolutionPathDir = new File(EVOLUTION_LOG_PATH);
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