package br.inpe.cap.evolution.persistence;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import br.inpe.cap.evolution.maven.CommitLine;

@NodeEntity
public class PersistedCommitLine {
	
	@Id
	private Integer id;
	
	private String hash;

	public static PersistedCommitLine create(CommitLine commitLine) {
		PersistedCommitLine persistedCommitLine = new PersistedCommitLine();
		persistedCommitLine.hash = commitLine.getHash();

		return persistedCommitLine;
	}

	private PersistedCommitLine() {}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}
	
}
