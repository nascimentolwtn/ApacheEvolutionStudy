package br.inpe.cap.apache;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.repodriller.domain.ChangeSet;
import org.repodriller.domain.Commit;
import org.repodriller.domain.Modification;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;

import br.inpe.cap.evolution.parser.StringMavenParser;

public class ApacheEvolutionVisitor implements CommitVisitor {
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	private static Logger logger;

	private String repositoryName;
	private List<String> hashes;
	
	public StringMavenParser parser = new StringMavenParser();

	@Override
	public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {
		if(this.hashes == null) {
			repositoryName = repo.getLastDir();
			List<ChangeSet> changeSets = repo.getScm().getChangeSets();
			hashes = changeSets.stream().map((cs)->cs.getId()).collect(Collectors.toList());
		}
		
		if(commit.isMerge() || !commit.isInMainBranch()) {
			String message = "SKIPPED Commit " + commit.getHash() + " by " + this.name() + " - " +
							   "isMerge=" + commit.isMerge() +
							   "/isInMainBranch=" + commit.isInMainBranch();
			logger.info(message);
			return;
		}
		
		int currentHashPosition = hashes.indexOf(commit.getHash()) + 1;
		int totalCommits = hashes.size();
		
		float percent = 100 - ((currentHashPosition*100)/(float)totalCommits);
		
		List<Modification> modifications = commit.getModifications();
		modifications.parallelStream()
			.filter(
				(m)-> m.fileNameEndsWith("pom.xml"))
			.forEach( 
				(m) ->	{
					String sourceCode = m.getSourceCode();
					if(sourceCode.contains("<groupId>org.apache")) {
						String apacheLib = parser.extractApacheLib(sourceCode);
						String apacheLibVersion = parser.extractApacheLibVersion(sourceCode);
						writer.write(
								commit.getHash(),
								DATE_FORMAT.format(commit.getDate().getTime()),
								repositoryName,
								m.getFileName(),
								currentHashPosition,
								totalCommits,
								percent,
								apacheLib,
								apacheLibVersion,
								commit.getMsg().replace("\n", "").replace(",","").replace("\r", "")
						);
						String foundMessage = "FOUND: " + repositoryName + " - " + apacheLib + " at version " + apacheLibVersion;
						System.err.println(foundMessage);
						logger.info(foundMessage);
						System.gc();
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

	@Override
	public String name() {
		return "apache-evolution_" + this.repositoryName;
	}

	public static void setLogger(Logger logger) {
		ApacheEvolutionVisitor.logger = logger;
	}

}