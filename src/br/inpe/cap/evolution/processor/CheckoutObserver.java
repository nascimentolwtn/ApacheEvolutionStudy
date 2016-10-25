package br.inpe.cap.evolution.processor;

import br.com.metricminer2.domain.Commit;
import br.com.metricminer2.scm.SCMRepository;

public interface CheckoutObserver {

	default void beforeCheckout(final SCMRepository repo, final Commit commit) {
	}

	default void afterReset(final SCMRepository repo, final Commit commit) {
	}

}
