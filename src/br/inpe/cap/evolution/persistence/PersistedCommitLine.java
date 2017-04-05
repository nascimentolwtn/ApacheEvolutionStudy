package br.inpe.cap.evolution.persistence;

import br.inpe.cap.evolution.maven.CommitLine;
import net.sf.esfinge.querybuilder.neo4j.oomapper.annotations.Id;
import net.sf.esfinge.querybuilder.neo4j.oomapper.annotations.Indexed;
import net.sf.esfinge.querybuilder.neo4j.oomapper.annotations.NodeEntity;

@NodeEntity
public class PersistedCommitLine {
	
	@Id
	private Integer id;
	
	@Indexed
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
