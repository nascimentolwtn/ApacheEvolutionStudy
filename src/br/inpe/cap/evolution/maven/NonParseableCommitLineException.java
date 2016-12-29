package br.inpe.cap.evolution.maven;

public class NonParseableCommitLineException extends RuntimeException {

	private static final long serialVersionUID = 3438732339556132525L;

	public NonParseableCommitLineException(String message) {
		super(message);
	}

}
