package br.inpe.cap.evolution.processor;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import br.com.metricminer2.domain.Commit;
import br.com.metricminer2.scm.RepositoryFile;
import br.com.metricminer2.scm.SCMRepository;

public abstract class SynchronousCheckOutRepositoryProcessor {
	
	private final CheckoutObserver observer;
	
	public SynchronousCheckOutRepositoryProcessor() {
		this.observer = new CheckoutObserver() {};
	}

	public SynchronousCheckOutRepositoryProcessor(final CheckoutObserver observer) {
		this.observer = observer;
	}

	public void processCommit(final SCMRepository repo, final Commit commit) throws IOException {
		
		final ReentrantLock checkoutLock = new ReentrantLock();
		checkoutLock.lock();
		try {
			
			this.observer.beforeCheckout(repo, commit);
			repo.getScm().checkout(commit.getHash());
			
			for (RepositoryFile repositoryFile : repo.getScm().files()) {
				processFile(repo, commit, repositoryFile);
				repositoryFile = null;
			}

		} finally {
			repo.getScm().reset();
			this.observer.afterReset(repo, commit);
			checkoutLock.unlock();
			System.gc();
		}
	}

	protected abstract void processFile(final SCMRepository repo, final Commit commit, final RepositoryFile repositoryFile);

}