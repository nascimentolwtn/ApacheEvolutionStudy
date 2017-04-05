package br.inpe.cap.evolution.persistence;

import static br.inpe.cap.evolution.maven.CommitLine.CommitLineType.OUTPUT;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import br.inpe.cap.evolution.maven.CommitLine;

public class PersistedCommitLineTest {

	private String outputLine = "c425502f5c82f49535f9e11a7038cf3df11c29b1,31/05/2014 15:22:45,disconf,E:\\metricminer-evolution-stars\\disconf\\pom.xml,2,1154,0.17331,org.reflections,reflections,0.9.9-RC1,initial_version,false,false,first pack";
	
	@Test
	public void basicCommitLineCreation() {
		final CommitLine commitLine = CommitLine.parseCommitLine(outputLine, OUTPUT);
		
		PersistedCommitLine persistedCommitLine = PersistedCommitLine.create(commitLine);
		
		assertEquals(commitLine.getHash(), persistedCommitLine.getHash());
		
	}
	
}
