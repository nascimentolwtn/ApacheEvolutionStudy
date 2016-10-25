package br.inpe.cap.evolution.processor;

import static org.apache.commons.io.FileUtils.readFileToByteArray;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import br.com.metricminer2.domain.Commit;
import br.com.metricminer2.scm.RepositoryFile;
import br.com.metricminer2.scm.SCMRepository;

public abstract class SynchronizedCheckOutFileRepositoryProcessor {
	
	private final CheckoutObserver observer;
	
	public SynchronizedCheckOutFileRepositoryProcessor() {
		this.observer = new CheckoutObserver() {};
	}

	public SynchronizedCheckOutFileRepositoryProcessor(final CheckoutObserver observer) {
		this.observer = observer;
	}

	public void process(final SCMRepository repo, final Commit commit) throws IOException {
		
		
		Map<String, ByteArrayInputStream> files;
		try {
			
			files = checkoutRepositoryFiles(repo, commit);

			for(final Map.Entry<String, ByteArrayInputStream> entry : files.entrySet()) {
				processFile(repo, commit, entry.getKey(), entry.getValue());
				entry.setValue(null);
			}
		
		} finally {
			files=null;
			System.gc();
		}

	}

	public Map<String, ByteArrayInputStream> checkoutRepositoryFiles(final SCMRepository repo, final Commit commit) throws IOException {
		
		final ReentrantLock checkoutLock = new ReentrantLock();
		checkoutLock.lock();
		
		try {
		
			this.observer.beforeCheckout(repo, commit);
			repo.getScm().checkout(commit.getHash());
			
			
			final List<RepositoryFile> repositoryFiles = repo.getScm().files();
			final Map<String, ByteArrayInputStream> mapa = new ConcurrentHashMap<>(repositoryFiles.size());
			
			for (final RepositoryFile repositoryFile : repositoryFiles) {
				mapa.put(
						repositoryFile.getFullName(), 
						new ByteArrayInputStream(readFileToByteArray(repositoryFile.getFile())));				
			}
			
			return mapa;
			
		} finally {
			repo.getScm().reset();
			this.observer.afterReset(repo, commit);
			checkoutLock.unlock();

		}
	}
	
	protected abstract void processFile(final SCMRepository repo, final Commit commit, String fileName, final ByteArrayInputStream file);

}