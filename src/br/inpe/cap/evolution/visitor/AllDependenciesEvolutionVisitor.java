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

import br.inpe.cap.evolution.processor.EffectivePomSynchronousCheckoutProcessor;
import br.inpe.cap.evolution.processor.LoggerCheckoutObserver;

public class AllDependenciesEvolutionVisitor implements CommitVisitor {
	
	private static Logger logger;

	private String repositoryName;
	private List<String> hashes;
	private int totalCommits; 
	
	private EffectivePomSynchronousCheckoutProcessor effectivePomProcessor;
	
	@Override
	public void initialize(SCMRepository repo, PersistenceMechanism writer) {
		this.repositoryName = repo.getLastDir();
		final List<ChangeSet> changeSets = repo.getScm().getChangeSets();
		this.hashes = changeSets.stream().map((cs)->cs.getId()).collect(Collectors.toList());
		this.totalCommits = hashes.size();
		this.effectivePomProcessor = new EffectivePomSynchronousCheckoutProcessor(new LoggerCheckoutObserver(logger), writer, totalCommits, logger);
	}

	@Override
	public void process(final SCMRepository repo, final Commit commit, final PersistenceMechanism writer) {
		
		try {
			
			final int currentHashPosition = totalCommits - hashes.indexOf(commit.getHash());
			final float percent = ((currentHashPosition*100)/(float)totalCommits);
			
			effectivePomProcessor.setCurrentHashPosition(currentHashPosition);
			effectivePomProcessor.setPercent(percent);
			effectivePomProcessor.processCommit(repo, commit, percentageMessage(currentHashPosition, percent));
			
		} catch (final IOException | InterruptedException e) {
			logger.error(e.getMessage());
		}
		
	}

	private String percentageMessage(final int currentHashPosition, final float percent) {
		final StringBuilder percentMessage = new StringBuilder(); 
		percentMessage.append("progress: #");
		percentMessage.append(currentHashPosition);
		percentMessage.append("/");
		percentMessage.append(totalCommits);
		percentMessage.append(" - ");
		percentMessage.append(percent);
		percentMessage.append("%");
		Thread.currentThread().setName("Visitor " + this.repositoryName);
		return percentMessage.toString();
	}

	@Override
	public String name() {
		return "all-dependency_" + this.repositoryName;
	}

	public static void setLogger(final Logger logger) {
		AllDependenciesEvolutionVisitor.logger = logger;
	}
	
	@Override
	public void finalize(SCMRepository repo, PersistenceMechanism writer) {
		this.hashes = null;
		this.repositoryName = null;
		this.effectivePomProcessor = null;
	}
}