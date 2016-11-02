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
import org.repodriller.RepoDriller;
import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.range.Commits;
import org.repodriller.persistence.csv.CSVFile;
import org.repodriller.scm.GitRepository;

import br.inpe.cap.auxiliar.MultipleCSVFile;

public class ApacheProjectsVersionEvolutionStudy implements Study {

	private static final int THREADS_FOR_REPOSITORIES = 10;
	private static final File GITHUB_DONE_FILE = new File("done-github_evolution-HOME.txt");
	private static File EXCEPTION_FILE = new File("exceptions-evolution-HOME.log");

	private static final String STUDY_LOG_PATH = "." + File.separator + "study_eval";
	private static final String EVOLUTION_LOG_PATH = STUDY_LOG_PATH + File.separator + "evolutions";
	
	private static final String APACHE_FILE_PREFIX = "apache_evolution-HOME";
	private static final String APACHE_EVOLUTION_SUMMARY_CSV = STUDY_LOG_PATH + File.separator + APACHE_FILE_PREFIX	+ ".csv"; 
	
	private static Logger log;
	
	public static void main(String[] args) throws IOException {
		System.setProperty("logfilename", APACHE_FILE_PREFIX + "_run01");
		log = Logger.getLogger(RepositoryMining.class);
		ApacheEvolutionVisitor.setLogger(log);
		
		checkRequiredLogFilesAndDirectories();
		
		new RepoDriller().start(new ApacheProjectsVersionEvolutionStudy());
		System.out.println("Finish!");
	}
	
	public void execute() {
		try {
			
			String rootApacheStudiesPath = "E:\\metricminer2_studies\\";
			List<String> gitRepoDirs = getRepositoryExceptDoneDirs(rootApacheStudiesPath);

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

	private void doMining(String gitUrl) {
		try {
			new RepositoryMining()
				.in(GitRepository.singleProject(gitUrl, 2000))
				.through(Commits.all())
//			.withThreads(3)
				.process(new ApacheEvolutionVisitor(), new MultipleCSVFile(
						new CSVFile(APACHE_EVOLUTION_SUMMARY_CSV, true)
						,
						new CSVFile(EVOLUTION_LOG_PATH
							+ File.separator
							+ "apache-evolution-'"
							+ gitUrl.substring(gitUrl.lastIndexOf(File.separator)+1, gitUrl.length())
							+ "'.csv")))
				.mine();
		} catch (RuntimeException re) {
			log.error(re.getMessage());
		}
		markDone(gitUrl);
		System.gc();
	}
	
	private List<String> getRepositoryExceptDoneDirs(String rootApacheStudiesPath) throws IOException {
		List<String> allDirsIn = org.repodriller.util.FileUtils.getAllDirsIn(rootApacheStudiesPath);
		
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