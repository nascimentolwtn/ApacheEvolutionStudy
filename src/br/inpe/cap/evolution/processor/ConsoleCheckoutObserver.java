package br.inpe.cap.evolution.processor;

import org.repodriller.domain.Commit;
import org.repodriller.scm.SCMRepository;

public class ConsoleCheckoutObserver implements CheckoutObserver {
	
	@Override
	public void beforeCheckout(SCMRepository repo, Commit commit, String message) {
		System.out.println("Checking out commit " + commit.getHash() + "@" + repo.getLastDir());
	}
	
	@Override
	public void afterReset(SCMRepository repo, Commit commit) {
		System.out.println("Repository " + repo.getLastDir() + " reset.");
	}

}
