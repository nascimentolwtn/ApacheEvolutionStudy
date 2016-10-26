package br.inpe.cap.evolution.maven;

public class UnparsableEffectivePomException extends Exception {
	
	private static final long serialVersionUID = 6982333776870743583L;

	public UnparsableEffectivePomException(Exception e) {
		super(e);
	}

	public UnparsableEffectivePomException(String message) {
		super(message);
	}

}
