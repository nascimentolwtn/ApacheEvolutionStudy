package br.inpe.cap.evolution.processor;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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

	public void processCommit(final SCMRepository repo, final Commit commit) throws IOException, InterruptedException {
		
		final ReentrantLock checkoutLock = new ReentrantLock();
		checkoutLock.lock();
		try {
			
			this.observer.beforeCheckout(repo, commit);
			repo.getScm().checkout(commit.getHash());
			
			ExecutorService exec = Executors.newFixedThreadPool(5);
			for (RepositoryFile repositoryFile : repo.getScm().files()) {
				exec.submit(() -> {
					processFile(repo, commit, repositoryFile);
				});
			}
			
			exec.shutdown();
			exec.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

		} finally {
			repo.getScm().reset();
			this.observer.afterReset(repo, commit);
			checkoutLock.unlock();
			System.gc();
		}
	}

	protected abstract void processFile(final SCMRepository repo, final Commit commit, final RepositoryFile repositoryFile);

}