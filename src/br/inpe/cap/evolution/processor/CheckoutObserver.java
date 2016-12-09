package br.inpe.cap.evolution.processor;

import org.repodriller.domain.Commit;
import org.repodriller.scm.SCMRepository;

public interface CheckoutObserver {

	default void beforeCheckout(final SCMRepository repo, final Commit commit, String message) {
	}

	default void afterReset(final SCMRepository repo, final Commit commit) {
	}

}
