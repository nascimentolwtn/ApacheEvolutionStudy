package br.inpe.cap.evolution;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import br.com.metricminer2.domain.ChangeSet;
import br.com.metricminer2.domain.Commit;
import br.com.metricminer2.persistence.PersistenceMechanism;
import br.com.metricminer2.scm.CommitVisitor;
import br.com.metricminer2.scm.SCMRepository;
import br.inpe.cap.evolution.domain.MavenDependency;
import br.inpe.cap.evolution.domain.MavenProject;
import br.inpe.cap.evolution.parser.XmlMavenParser;

public class DependencyEvolutionVisitor implements CommitVisitor {
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	/**
	 * Formato da porcentagem com "." para não confundir com a "," do CSV
	 */
	private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.00000", DecimalFormatSymbols.getInstance(Locale.US));
	
	@SuppressWarnings("unused")
	private static Logger logger;

	private String repositoryName;
	private List<String> hashes;
	private File pomDir;
	
	public XmlMavenParser parser = new XmlMavenParser();

	public DependencyEvolutionVisitor(String evolutionLogPath, String gitReposLogSubDir) {
//		evolutionLogPath = "..\\..\\..\\..\\..\\" + evolutionLogPath; // comente esta linha para manter pom's dentro do diretório do estudo
		this.pomDir = new File(evolutionLogPath + File.separator + gitReposLogSubDir);
		this.pomDir.mkdir();
	}

	@Override
	public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {
		this.initHashes(repo);
		
		if(isntProcessableCommit(commit)) {
			return;
		}
		
		int currentHashPosition = hashes.indexOf(commit.getHash());
		int totalCommits = hashes.size();
		
		float percent = ((currentHashPosition*100)/(float)totalCommits);
		
		commit.getModifications().stream()
			.filter(
				(m) -> m.fileNameEndsWith("pom.xml"))
			.forEach( 
				(m) ->	{
					String sourceCode = m.getSourceCode();
					MavenProject pom = parser.readPOM(sourceCode);
					pom.getDependencies().forEach(
						(dependency) -> 
							writeCsvLine(writer, commit, currentHashPosition, totalCommits, percent, m.getFileName(), dependency)
						);

					try {
						String pomFileName = pomDir.getAbsolutePath() + File.separator + m.getFileName();
						FileUtils.writeStringToFile(new File(pomFileName  + "." + currentHashPosition), sourceCode);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
		);
	
		String percentMessage = repositoryName
				+ " progress: commit #"
				+ currentHashPosition
				+ "/"
				+ totalCommits
				+ " - "
				+ percent
				+ "%";
		System.err.println(percentMessage);
	}

	private void writeCsvLine(PersistenceMechanism writer, Commit commit, int currentHashPosition, int totalCommits,
			float percent, String fileName, MavenDependency mavenDependency) {
		writer.write(
				commit.getHash(),
				DATE_FORMAT.format(commit.getDate().getTime()),
				repositoryName,
				fileName,
				currentHashPosition,
				totalCommits,
				PERCENT_FORMAT.format(percent),
				mavenDependency.getGroupId(),
				mavenDependency.getArtifactId(),
				mavenDependency.getVersion(),
				XmlMavenParser.replaceLineFeedAndComma(commit.getMsg())
		);
	}

	private boolean isntProcessableCommit(Commit commit) {
		boolean isntProcessableCommit = (commit.isMerge() || !commit.isInMainBranch());
//		if(isntProcessableCommit) {
//			String message = "SKIPPED Commit " + commit.getHash() + " by " + this.name() + " - " +
//					   "isMerge=" + commit.isMerge() +
//					   "/isInMainBranch=" + commit.isInMainBranch();
//			logger.info(message);
//		}
		return isntProcessableCommit;
	}

	private void initHashes(SCMRepository repo) {
		if(this.hashes == null) {
			repositoryName = repo.getLastDir();
			List<ChangeSet> changeSets = repo.getScm().getChangeSets();
			hashes = changeSets.stream().map((cs)->cs.getId()).collect(Collectors.toList());
			Thread.currentThread().setName("Visitor " + this.repositoryName);
		}
	}

	@Override
	public String name() {
		return "dependency-evolution_" + this.repositoryName;
	}

	public static void setLogger(Logger logger) {
		DependencyEvolutionVisitor.logger = logger;
	}

}