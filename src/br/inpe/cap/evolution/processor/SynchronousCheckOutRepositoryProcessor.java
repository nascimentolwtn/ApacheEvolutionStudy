package br.inpe.cap.evolution.processor;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.repodriller.domain.Commit;
import org.repodriller.scm.RepositoryFile;
import org.repodriller.scm.SCMRepository;

public abstract class SynchronousCheckOutRepositoryProcessor {
	
	private static final int MINUTES_TO_CHECK_THREADS = 3 * 60 * 1000;

	private final CheckoutObserver observer;
	private static long lastThreadByCommitCheck;
	private static int threadsByCommit = 10;
	
	public SynchronousCheckOutRepositoryProcessor() {
		this(new CheckoutObserver(){});
	}

	public SynchronousCheckOutRepositoryProcessor(final CheckoutObserver observer) {
		this.observer = observer;
		readThreadsByCommits();
	}

	public void processCommit(final SCMRepository repo, final Commit commit) throws IOException, InterruptedException {
		
		final ReentrantLock checkoutLock = new ReentrantLock();
		checkoutLock.lock();
		try {
			
			this.observer.beforeCheckout(repo, commit);
			repo.getScm().checkout(commit.getHash());
			
			if((System.currentTimeMillis() - lastThreadByCommitCheck) > MINUTES_TO_CHECK_THREADS) {
				readThreadsByCommits();
			}
			ExecutorService exec = Executors.newFixedThreadPool(threadsByCommit);
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

	private void readThreadsByCommits() {
		try {
			final String readFileToString = FileUtils.readFileToString(
				new File("fountain" + File.separator + "thread-by-commit.txt"));
			threadsByCommit = Integer.parseInt(readFileToString);
			lastThreadByCommitCheck = System.currentTimeMillis();
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}

	protected abstract void processFile(final SCMRepository repo, final Commit commit, final RepositoryFile repositoryFile);

}