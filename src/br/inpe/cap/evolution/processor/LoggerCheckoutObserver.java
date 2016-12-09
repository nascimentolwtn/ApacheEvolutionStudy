package br.inpe.cap.evolution.processor;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.repodriller.domain.Commit;
import org.repodriller.scm.SCMRepository;

public class LoggerCheckoutObserver implements CheckoutObserver {
	
	private Logger logger;
	private Level level;

	public LoggerCheckoutObserver(Logger logger) {
		this(logger, Level.INFO);
	}
	
	public LoggerCheckoutObserver(Logger logger, Level level) {
		this.logger = logger;
		this.level = level;
	}
	
	@Override
	public void beforeCheckout(SCMRepository repo, Commit commit, String message) {
		logMessage("Checking out commit " + commit.getHash() + "@" + repo.getLastDir() + " - " + message);
	}

	@Override
	public void afterReset(SCMRepository repo, Commit commit) {
		logMessage("Repository " + repo.getLastDir() + " reset.");
	}

	private void logMessage(String message) {
		if(this.level == Level.INFO) {
			this.logger.info(message);
		} else if(this.level == Level.DEBUG) {
			this.logger.debug(message);
		} else if(this.level == Level.WARN) {
			this.logger.warn(message);
		} else if(this.level == Level.FATAL) {
			this.logger.fatal(message);
		} else if(this.level == Level.TRACE) {
			this.logger.trace(message);
		}
	}
	
}
