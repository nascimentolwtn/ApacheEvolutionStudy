package br.inpe.cap.evolution.processor;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.repodriller.domain.Commit;
import org.repodriller.scm.RepositoryFile;
import org.repodriller.scm.SCMRepository;

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
			
			ExecutorService exec = Executors.newFixedThreadPool(10);
			// CommitFilter processa apenas commits que possuem MODIFICAÇÕES com esse FileType.
			// Porém, aqui a ideia é fazer CHECKOUT todos arquivos mesmo, e então filtrar novamente e abrir threads somente para processar os poms.xml.
			final List<RepositoryFile> pomFiles = repo.getScm().files().stream().filter((f)->f.fileNameEndsWith("pom.xml")).collect(Collectors.toList());
			for (RepositoryFile repositoryFile : pomFiles) {
				exec.submit(() -> 
					processFile(repo, commit, repositoryFile)
				);
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