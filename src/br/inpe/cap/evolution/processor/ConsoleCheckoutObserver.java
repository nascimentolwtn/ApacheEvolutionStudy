package br.inpe.cap.evolution.processor;

import br.com.metricminer2.domain.Commit;
import br.com.metricminer2.scm.SCMRepository;

public class ConsoleCheckoutObserver implements CheckoutObserver {
	
	@Override
	public void beforeCheckout(SCMRepository repo, Commit commit) {
		System.out.println("Checking out commit " + commit.getHash() + "@" + repo.getLastDir());
	}
	
	@Override
	public void afterReset(SCMRepository repo, Commit commit) {
		System.out.println("Repository " + repo.getLastDir() + " reset.");
	}

}
