package br.inpe.cap.apache;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import br.com.metricminer2.domain.ChangeSet;
import br.com.metricminer2.domain.Commit;
import br.com.metricminer2.domain.Modification;
import br.com.metricminer2.persistence.PersistenceMechanism;
import br.com.metricminer2.scm.CommitVisitor;
import br.com.metricminer2.scm.SCMRepository;

public class ApacheEvolutionVisitor implements CommitVisitor {

	private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	private String repositoryName;
	private List<String> hashes;

	@Override
	public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {
		if(this.hashes == null) {
			repositoryName = repo.getLastDir();
			List<ChangeSet> changeSets = repo.getScm().getChangeSets();
			hashes = changeSets.stream().map((cs)->cs.getId()).collect(Collectors.toList());
		}
		
		if(commit.isMerge() || !commit.isInMasterBranch()) {
			System.err.println("SKIPPING Commit " + commit.getHash() + " - " +
							   "isMerge: " + commit.isMerge() +
							   " isInMasterBranch: " + commit.isInMasterBranch());
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
						String apacheLib = extractApacheLib(sourceCode);
						String apacheLibVersion = extractApacheLibVersion(sourceCode);
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
								commit.getMsg().replace("\n", "").replace(",","")
						);
						System.err.println("FOUND: " + repositoryName + " - " + apacheLib + " at version " + apacheLibVersion);
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

	public String extractApacheLib(String sourceCode) {
		int firstIndexOfApacheGroupId = sourceCode.indexOf("<groupId>org.apache");
		int indexOfEndGroupIdTag = sourceCode.indexOf("</groupId>", firstIndexOfApacheGroupId);
		int lengthGroupId = 9; // "<groupId>".length();
		String apacheLib = sourceCode.substring(firstIndexOfApacheGroupId+lengthGroupId, indexOfEndGroupIdTag);
		return apacheLib;
	}
	
	public String extractApacheLibVersion(String sourceCode) {
		int firstIndexOfApacheGroupId = sourceCode.indexOf("<groupId>org.apache");
		int indexOfGroupIdEndTag = sourceCode.indexOf("</groupId>", firstIndexOfApacheGroupId);

		int firstIndexOfVersionAfterApacheGroupId = sourceCode.indexOf("<version>", indexOfGroupIdEndTag);
		int indexOfVersionEndTag = sourceCode.indexOf("</version>", firstIndexOfVersionAfterApacheGroupId);
		int lengthVersion = 9; // "<version>".length();
		String apacheLibVersion = sourceCode.substring(firstIndexOfVersionAfterApacheGroupId+lengthVersion, indexOfVersionEndTag);
		return apacheLibVersion;
	}

	@Override
	public String name() {
		return "apache-evolution_" + this.repositoryName;
	}

}