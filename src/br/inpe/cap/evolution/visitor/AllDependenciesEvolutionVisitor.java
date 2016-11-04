package br.inpe.cap.evolution.visitor;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.repodriller.domain.ChangeSet;
import org.repodriller.domain.Commit;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;

import br.inpe.cap.evolution.maven.CommitLine;
import br.inpe.cap.evolution.processor.EffectivePomSynchronousCheckoutProcessor;
import br.inpe.cap.evolution.processor.LoggerCheckoutObserver;

public class AllDependenciesEvolutionVisitor implements CommitVisitor {
	
	private static Logger logger;

	private String repositoryName;
	private List<String> hashes;
	private int totalCommits; 
	
	private EffectivePomSynchronousCheckoutProcessor effectivePomProcessor;

	@Override
	public void process(final SCMRepository repo, final Commit commit, final PersistenceMechanism writer) {
		this.initVisitor(repo, writer);
		
		try {
			
			final int currentHashPosition = totalCommits - hashes.indexOf(commit.getHash());
			final float percent = ((currentHashPosition*100)/(float)totalCommits);
			
			effectivePomProcessor.setCurrentHashPosition(currentHashPosition);
			effectivePomProcessor.setPercent(percent);
			effectivePomProcessor.processCommit(repo, commit);
			printPercentageMessage(currentHashPosition, percent);
			
		} catch (final IOException | InterruptedException e) {
			logger.error(e.getMessage());
		}
		
	}

	private void printPercentageMessage(final int currentHashPosition, final float percent) {
		final StringBuilder percentMessage = new StringBuilder(); 
		percentMessage.append(repositoryName);
		percentMessage.append(" progress: commit #");
		percentMessage.append(currentHashPosition);
		percentMessage.append("/");
		percentMessage.append(totalCommits);
		percentMessage.append(" - ");
		percentMessage.append(percent);
		percentMessage.append("%");
		System.err.println(percentMessage.toString());
	}

	private void initVisitor(final SCMRepository repo, final PersistenceMechanism writer) {
		if(this.hashes == null) {
			this.repositoryName = repo.getLastDir();
			Thread.currentThread().setName("Visitor " + this.repositoryName);
			final List<ChangeSet> changeSets = repo.getScm().getChangeSets();
			this.hashes = changeSets.stream().map((cs)->cs.getId()).collect(Collectors.toList());
			this.totalCommits = hashes.size();
			this.effectivePomProcessor = new EffectivePomSynchronousCheckoutProcessor(new LoggerCheckoutObserver(logger), writer, totalCommits, logger);
			this.writeCsvHeader(writer);
		}
	}

	private void writeCsvHeader(final PersistenceMechanism writer) {
		writer.write(CommitLine.HEADER);
	}
	
	@Override
	public String name() {
		return "all-dependency_" + this.repositoryName;
	}

	public static void setLogger(final Logger logger) {
		AllDependenciesEvolutionVisitor.logger = logger;
	}
	
}